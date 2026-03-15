/* ===========================================
    VARIABLES PRINCIPALES
=========================================== */
const tbody = document.getElementById("functionsBody");
const form = document.getElementById("functionForm");
const modalEl = document.getElementById("functionModal");
const modalTitle = document.getElementById("modalTitle");

let editingFunctionId = null;
const functionCache = {};
const draftFunctions = {};

let allFunctions = {}; // FILTRO

/* ===========================================
    FORMATEO
=========================================== */
function formatDays(days) {
    const order = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
    const labels = { MONDAY: "Lunes", TUESDAY: "Martes", WEDNESDAY: "Miércoles", THURSDAY: "Jueves", FRIDAY: "Viernes", SATURDAY: "Sábado", SUNDAY: "Domingo" };
    return order.filter(d => days.includes(d)).map(d => `<span class="badge bg-primary-subtle text-primary me-1">${labels[d]}</span>`).join("");
}

/* ===========================================
    RENDER
=========================================== */
function render(tasks) {
    tbody.innerHTML = "";

    if (!Object.keys(tasks).length) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-5 text-muted">
        <i class="bi bi-exclamation-triangle me-2"></i>
        No hay funciones ingresadas</td></tr>`;
        return;
    }

    Object.values(tasks)
        .sort((a, b) => (a.name || "").localeCompare(b.name || ""))
        .forEach((f, index) => {
            tbody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td class="text-center fw-semibold">${index + 1}</td>
                    <td class="fw-semibold">${f.name}</td>
                    <td>
                        <span class="badge ${f.sequential ? "bg-info-subtle text-info"
                    : "bg-warning-subtle text-warning"} me-1">
                            ${f.sequential ? "Secuencial" : "Aleatoria"}
                        </span>
                    </td>
                    <td>${formatDays(f.assignedDays || [])}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-secondary rounded-pill me-1" onclick="editFunction('${f.id || f.name}')">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger rounded-pill" onclick="openDeleteFunctionModal('${f.id || f.name}')">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `);
        });
}

/* ===========================================
    FILTRO POR TIPO
=========================================== */
function applyFunctionFilter() {

    const filter = document.getElementById("filterType");

    if (!filter) {
        render(allFunctions);
        return;
    }

    const value = filter.value;

    if (value === "ALL") {
        render(allFunctions);
        return;
    }

    const filtered = Object.fromEntries(
        Object.entries(allFunctions).filter(([k, f]) => {

            if (value === "COMPATIBLE") return f.sequential === true;
            if (value === "INCOMPATIBLE") return f.sequential === false;
            if (value === "SOFT_INCOMPATIBLE") return false;

            return true;
        })
    );

    render(filtered);
}

/* ===========================================
    LOAD (DB)
=========================================== */
async function loadFunctions() {
    const res = await fetch("/api/functions");
    if (!res.ok) {
        showAlert(`
        <div class="d-flex align-items-start gap-2">
            <i class="bi bi-x-circle-fill text-danger fs-5 mt-1"></i>
            <div>
                <div class="fw-semibold">Error cargando funciones</div>
                <small class="text-muted">No se pudieron obtener los datos del servidor.</small>
            </div>
        </div>
    `, "danger");
        return;
    }

    const data = await res.json();
    Object.keys(functionCache).forEach(k => delete functionCache[k]);
    data.forEach(t => functionCache[t.id] = t);

    allFunctions = { ...functionCache, ...draftFunctions };
    applyFunctionFilter();
}

document.addEventListener("DOMContentLoaded", () => {
    loadFunctions();

    const filter = document.getElementById("filterType");
    if (filter) filter.addEventListener("change", applyFunctionFilter);
});

/* ===========================================
    LOAD WORD
=========================================== */
async function loadFunctionsFromWord() {
    const file = document.getElementById("wordFile").files[0];
    if (!file) {
        showAlert(`
        <div class="d-flex align-items-start gap-2">
            <i class="bi bi-exclamation-triangle-fill text-warning fs-5 mt-1"></i>
            <div>
                <div class="fw-semibold">Seleccioná un archivo Word</div>
                <small class="text-muted">Debés subir un archivo .docx para importar funciones.</small>
            </div>
        </div>
    `, "warning");
        return;
    }

    const fd = new FormData();
    fd.append("file", file);

    const res = await secureFetch("/api/functions/from-word", {
        method: "POST",
        body: fd
    });

    if (!res.ok) {
        showAlert(`
        <div class="d-flex align-items-start gap-2">
            <i class="bi bi-x-circle-fill text-danger fs-5 mt-1"></i>
            <div>
                <div class="fw-semibold">Error leyendo el archivo Word</div>
                <small class="text-muted">No se pudieron importar las funciones.</small>
            </div>
        </div>
    `, "danger");
        return;
    }

    const data = await res.json();
    Object.entries(data).forEach(([name, days]) => {
        if (!functionCache[name] && !draftFunctions[name]) {
            draftFunctions[name] = { name, assignedDays: days, sequential: true };
        }
    });

    allFunctions = { ...functionCache, ...draftFunctions };
    applyFunctionFilter();
}

/* ===========================================
    MODAL
=========================================== */
function addFunction() {
    editingFunctionId = null;
    resetForm();
    new bootstrap.Modal(modalEl).show();
}

function editFunction(key) {
    const functionData = functionCache[key] || draftFunctions[key];
    if (!functionData) return;

    editingFunctionId = key;

    modalTitle.innerHTML = `
        <i class="bi bi-pencil me-1"></i>
        Editar función
    `;

    document.getElementById("functionName").value = functionData.name;
    document.getElementById("sequential").value = String(functionData.sequential);
    $("#personDays").val(functionData.assignedDays).trigger("change");

    new bootstrap.Modal(modalEl).show();
}

/* ===========================================
    CREATE / UPDATE
=========================================== */
form.addEventListener("submit", async e => {
    e.preventDefault();

    const name = document.getElementById("functionName").value.trim();
    const days = $("#personDays").val() || [];
    const sequential = document.getElementById("sequential").value === "true";

    const functionData = { name, assignedDays: days, sequential };

    if (editingFunctionId) {
        if (draftFunctions[editingFunctionId]) {
            draftFunctions[editingFunctionId] = functionData;
        } else if (functionCache[editingFunctionId]) {
            try {
                await secureFetch(`/api/functions/${editingFunctionId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(functionData)
                });

                functionCache[editingFunctionId] = {
                    ...functionCache[editingFunctionId],
                    ...functionData,
                    dirty: true
                };

                showAlert(`
                <div class="d-flex align-items-start gap-2">
                    <i class="bi bi-pencil-fill text-success fs-5 mt-1"></i>
                    <div>
                        <div class="fw-semibold">Función actualizada</div>
                        <small class="text-muted">
                            Los cambios en <strong>"${functionData.name}"</strong> se aplicaron correctamente.
                        </small>
                    </div>
                </div>
                `, "success");

            } catch {
                showAlert(`
                    <div class="d-flex align-items-start gap-2">
                        <i class="bi bi-x-circle-fill text-danger fs-5 mt-1"></i>
                        <div>
                            <div class="fw-semibold">Error actualizando la función</div>
                        </div>
                    </div>
                `, "danger");
                return;
            }
        }
    } else {
        draftFunctions[name] = functionData;
    }

    bootstrap.Modal.getInstance(modalEl).hide();
    resetForm();

    allFunctions = { ...functionCache, ...draftFunctions };
    applyFunctionFilter();
});

function resetForm() {
    editingFunctionId = null;
    modalTitle.textContent = "Agregar función";
    form.reset();
    $("#personDays").val(null).trigger("change");
}

modalEl.addEventListener("hidden.bs.modal", resetForm);

/* ===========================================
    DELETE
=========================================== */
let functionIdToDelete = null;
let functionNameToDelete = null;

function openDeleteFunctionModal(key) {

    const functionData = draftFunctions[key] || functionCache[key];
    if (!functionData) return;

    functionIdToDelete = key;
    functionNameToDelete = functionData.name;

    document.getElementById("deleteFunctionName").textContent = functionData.name;

    const modal = new bootstrap.Modal(
        document.getElementById("deleteFunctionModal")
    );

    modal.show();
}

document
    .getElementById("confirmDeleteFunctionBtn")
    .addEventListener("click", async function () {

        if (!functionIdToDelete) return;

        if (draftFunctions[functionIdToDelete]) {
            delete draftFunctions[functionIdToDelete];
        }

        if (functionCache[functionIdToDelete]) {
            delete functionCache[functionIdToDelete];

            const res = await secureFetch(
                `/api/functions/${functionIdToDelete}`,
                { method: "DELETE" }
            );

            if (!res.ok) {
                showAlert(`
                    <div class="d-flex align-items-start gap-2">
                        <i class="bi bi-x-circle-fill text-danger fs-5 mt-1"></i>
                        <div>
                            <div class="fw-semibold">
                                No se pudo eliminar la función
                            </div>
                        </div>
                    </div>
                `, "danger");

                functionIdToDelete = null;
                return;
            }
        }

        bootstrap.Modal
            .getInstance(document.getElementById("deleteFunctionModal"))
            .hide();

        showAlert(`
            <div class="d-flex align-items-start gap-2">
                <i class="bi bi-trash-fill text-success fs-5 mt-1"></i>
                <div>
                    <div class="fw-semibold">
                        Función eliminada correctamente
                    </div>
                    <small class="text-muted">
                        <strong>"${functionNameToDelete}"</strong> fue eliminada del sistema.
                    </small>
                </div>
            </div>
        `, "success");

        functionIdToDelete = null;

        allFunctions = { ...functionCache, ...draftFunctions };
        applyFunctionFilter();
    });

/* ===========================================
    SAVE
=========================================== */
async function saveFunctions() {
    const fdraft = Object.values(draftFunctions);
    const dirtyPersisted = Object.values(functionCache).some(t => t.dirty);

    if (!fdraft.length && !dirtyPersisted) {
        showAlert(`
        <div class="d-flex align-items-start gap-2">
            <i class="bi bi-info-circle-fill text-primary fs-5 mt-1"></i>
            <div>
                <div class="fw-semibold">No hay cambios para guardar</div>
                <small class="text-muted">No se detectaron funciones nuevas ni modificaciones.</small>
            </div>
        </div>
    `, "info");
        return;
    }

    if (fdraft.length) {
        const res = await secureFetch("/api/functions", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(fdraft)
        });
        if (!res.ok) {
            const text = await res.text();
            console.error("Error backend:", text);

            showAlert(`
        <div class="d-flex align-items-start gap-2">
            <i class="bi bi-x-circle-fill text-danger fs-5 mt-1"></i>
            <div>
                <div class="fw-semibold">Error guardando funciones</div>
                <small class="text-muted">El servidor no pudo procesar los cambios.</small>
            </div>
        </div>
        `, "danger");

            return;
        }
        Object.keys(draftFunctions).forEach(k => delete draftFunctions[k]);
    }

    Object.values(functionCache).forEach(t => t.dirty = false);
    await loadFunctions();
    showAlert(`
        <div class="d-flex align-items-start gap-2">
            <i class="bi bi-check2-circle text-success fs-5"></i>
            <div>
                <div class="fw-semibold">Funciones guardadas correctamente</div>
            </div>
        </div>
    `, "success");
}

/* ===========================================
    SELECT2 (MODAL SAFE)
=========================================== */
modalEl.addEventListener("shown.bs.modal", () => {
    const $el = $("#personDays");
    if (!$el.hasClass("select2-hidden-accessible")) {
        $el.select2({ placeholder: "Seleccioná días de trabajo", width: "100%", dropdownAutoWidth: true });
    }
});

function showAlert(message, type = "success") {

    const container = document.getElementById("alertContainer");
    if (!container) return;

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });

    const alert = document.createElement("div");
    alert.className = `alert alert-${type} alert-dismissible fade show rounded-3 shadow-sm mb-3`;

    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    container.appendChild(alert);

    setTimeout(() => {
        alert.classList.remove("show");
        alert.remove();
    }, 8000);
}