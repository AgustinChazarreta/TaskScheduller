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
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-5 text-muted">Sin funciones</td></tr>`;
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
                        <span class="badge ${f.sequential ? "bg-warning text-dark" : "bg-secondary"} me-1">
                            ${f.sequential ? "Recurrente" : "Una vez"}
                        </span>
                    </td>
                    <td>${formatDays(f.assignedDays || [])}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-secondary rounded-pill me-1" onclick="editFunction('${f.id || f.name}')">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger rounded-pill" onclick="deleteFunction('${f.id || f.name}')">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `);
        });
}

/* ===========================================
    LOAD (DB)
=========================================== */
async function loadFunctions() {
    const res = await fetch("/api/functions");
    if (!res.ok) return;

    const data = await res.json();
    Object.keys(functionCache).forEach(k => delete functionCache[k]);
    data.forEach(t => functionCache[t.id] = t);

    render({ ...functionCache, ...draftFunctions });
}

document.addEventListener("DOMContentLoaded", loadFunctions);

/* ===========================================
    LOAD WORD
=========================================== */
async function loadFunctionsFromWord() {
    const file = document.getElementById("wordFile").files[0];
    if (!file) return alert("Seleccioná un Word");

    const fd = new FormData();
    fd.append("file", file);

    const res = await secureFetch("/api/functions/from-word", {
        method: "POST",
        body: fd
    });

    if (!res.ok) return alert("Error leyendo el Word");

    const data = await res.json();
    Object.entries(data).forEach(([name, days]) => {
        if (!functionCache[name] && !draftFunctions[name]) {
            draftFunctions[name] = { name, assignedDays: days, sequential: true };
        }
    });

    render({ ...functionCache, ...draftFunctions });
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
    modalTitle.textContent = "Editar función";

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

    if (!days.length) return alert("Seleccioná al menos un día");

    const functionData = { name, assignedDays: days, sequential };

    if (editingFunctionId) {
        if (draftFunctions[editingFunctionId]) draftFunctions[editingFunctionId] = functionData;
        else if (functionCache[editingFunctionId]) {
            try {
                await secureFetch(`/api/functions/${editingFunctionId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(functionData)
                });
                functionCache[editingFunctionId] = { ...functionCache[editingFunctionId], ...functionData, dirty: true };
            } catch {
                return alert("Error actualizando la función");
            }
        }
    } else {
        draftFunctions[name] = functionData;
    }

    bootstrap.Modal.getInstance(modalEl).hide();
    resetForm();
    render({ ...functionCache, ...draftFunctions });
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
async function deleteFunction(key) {
    const functionData = draftFunctions[key] || functionCache[key];
    if (!functionData) return;

    if (!confirm(`¿Eliminar la función "${functionData.name}"?`)) return;

    if (draftFunctions[key]) delete draftFunctions[key];
    if (functionCache[key]) {
        delete functionCache[key];
        await secureFetch(`/api/functions/${key}`, { method: "DELETE" });
    }

    render({ ...functionCache, ...draftFunctions });
}

/* ===========================================
    SAVE
=========================================== */
async function saveFunctions() {
    const fdraft = Object.values(draftFunctions);
    const dirtyPersisted = Object.values(functionCache).some(t => t.dirty);

    if (!fdraft.length && !dirtyPersisted) return alert("No hay funciones nuevas ni cambios pendientes para guardar.");

    if (fdraft.length) {
        const res = await secureFetch("/api/functions", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(fdraft)
        });
        if (!res.ok) { const text = await res.text(); console.error("Error backend:", text); return alert("Error guardando funciones"); }
        Object.keys(draftFunctions).forEach(k => delete draftFunctions[k]);
    }

    Object.values(functionCache).forEach(t => t.dirty = false);
    await loadFunctions();
    alert("Funciones guardadas correctamente");
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