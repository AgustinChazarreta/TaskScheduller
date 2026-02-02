/* ===========================================
    VARIABLES PRINCIPALES
=========================================== */
const tbody = document.getElementById("tasksBody");
const form = document.getElementById("taskForm");
const modalEl = document.getElementById("taskModal");
const modalTitle = document.getElementById("modalTitle");

let editingTaskId = null;

// Tareas persistidas en DB
const tasksCache = {};

// Tareas preliminares (solo en front, antes de guardar)
const draftTasks = {};

/* ===========================================
    FORMATEO
=========================================== */
function formatDays(days) {
    const order = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
    const labels = { MONDAY: "Lunes", TUESDAY: "Martes", WEDNESDAY: "Miércoles", THURSDAY: "Jueves", FRIDAY: "Viernes", SATURDAY: "Sábado", SUNDAY: "Domingo" };
    return order.filter(d => days.includes(d)).map(d => `<span class="badge bg-danger bg-gradient me-1">${labels[d]}</span>`).join("");
}

function formatCategories(categories) {
    const order = ["CATEGORY_1", "CATEGORY_2", "CATEGORY_3", "CATEGORY_4"];
    const labels = { CATEGORY_1: "Categoría 1", CATEGORY_2: "Categoría 2", CATEGORY_3: "Categoría 3", CATEGORY_4: "Categoría 4" };
    return order.filter(c => categories.includes(c)).map(c => `<span class="badge bg-warning bg-gradient text-dark me-1">${labels[c]}</span>`).join("");
}


/* ===========================================
    RENDER (UNIFICADO)
=========================================== */
function render(tasks) {
    tbody.innerHTML = "";

    if (!Object.keys(tasks).length) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Sin tareas</td></tr>`;
        return;
    }

    Object.values(tasks)
        .sort((a, b) => (a.name || "").localeCompare(b.name || ""))
        .forEach(t => {
            const categoriesStr = formatCategories(t.allowedCategories || [])
            tbody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td>${t.name}</td>
                    <td>${formatDays(t.assignedDays || [])}</td>
                    <td>${categoriesStr}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-primary me-1" onclick="editTask('${t.id || t.name}')">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteTask('${t.id || t.name}')">
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
async function loadTasks() {
    const res = await fetch("/api/tasks");
    if (!res.ok) return;

    const data = await res.json();

    Object.keys(tasksCache).forEach(k => delete tasksCache[k]);
    data.forEach(t => tasksCache[t.id] = t);

    render({ ...tasksCache, ...draftTasks });
}

document.addEventListener("DOMContentLoaded", loadTasks);

/* ===========================================
    LOAD WORD (PREVIEW)
=========================================== */
async function loadTasksFromWord() {
    const file = document.getElementById("wordFile").files[0];
    if (!file) return alert("Seleccioná un Word");

    const fd = new FormData();
    fd.append("file", file);

    const res = await secureFetch("/api/tasks/from-word", {
        method: "POST",
        body: fd
    });

    if (!res.ok) return alert("Error leyendo el Word");

    const data = await res.json();

    Object.entries(data).forEach(([name, days]) => {
        if (!tasksCache[name] && !draftTasks[name]) {
            draftTasks[name] = { name, assignedDays: days, allowedCategories: [] };
        }
    });

    render({ ...tasksCache, ...draftTasks });
}

/* ===========================================
    MODAL
=========================================== */
function addTask() {
    editingTaskId = null;
    resetForm();
    new bootstrap.Modal(modalEl).show();
}

function editTask(key) {
    const task = tasksCache[key] || draftTasks[key];
    if (!task) return;

    editingTaskId = key;
    modalTitle.textContent = "Editar tarea";

    document.getElementById("taskName").value = task.name;

    document.querySelectorAll(".category").forEach(cb => cb.checked = task.allowedCategories.includes(cb.value));
    document.querySelectorAll(".day").forEach(cb => cb.checked = task.assignedDays.includes(cb.value));

    new bootstrap.Modal(modalEl).show();
}

/* ===========================================
    CREATE / UPDATE (FRONT ONLY)
=========================================== */
form.addEventListener("submit", async e => {
    e.preventDefault();

    const name = document.getElementById("taskName").value.trim();
    const days = [...document.querySelectorAll(".day:checked")].map(d => d.value);
    const selectedCategories = [...document.querySelectorAll(".category:checked")].map(c => c.value);

    if (!days.length) return alert("Seleccioná al menos un día");
    if (!selectedCategories.length) return alert("Seleccioná al menos una categoría");

    const taskData = { name, assignedDays: days, allowedCategories: selectedCategories };

    if (editingTaskId) {
        // Revisamos si es draft o persistida
        if (draftTasks[editingTaskId]) {
            // Si es draft, actualizamos draftTasks
            draftTasks[editingTaskId] = taskData;
        } else if (tasksCache[editingTaskId]) {
            // Si es persistida, hacemos PUT al backend
            try {
                await secureFetch(`/api/tasks/${editingTaskId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(taskData)
                });
                // Actualizamos front
                tasksCache[editingTaskId] = { ...tasksCache[editingTaskId], ...taskData, dirty: true };
            } catch {
                return alert("Error actualizando la tarea");
            }
        }
    } else {
        // Nueva tarea preliminar → draft
        draftTasks[name] = taskData;
    }

    bootstrap.Modal.getInstance(modalEl).hide();
    resetForm();
    render({ ...tasksCache, ...draftTasks });
});


function resetForm() {
    editingTaskId = null;
    modalTitle.textContent = "Agregar tarea";
    form.reset();
}

modalEl.addEventListener("hidden.bs.modal", resetForm);

/* ===========================================
    DELETE
=========================================== */
async function deleteTask(key) {
    const task = draftTasks[key] || tasksCache[key];
    if (!task) return;

    if (!confirm(`¿Eliminar la tarea "${task.name}"?`)) return;

    if (draftTasks[key]) delete draftTasks[key];
    if (tasksCache[key]) {
        delete tasksCache[key];
        await secureFetch(`/api/tasks/${key}`, { method: "DELETE" });
    }

    render({ ...tasksCache, ...draftTasks });
}

/* ===========================================
    SAVE (PERSISTIR DRAFTS)
=========================================== */
async function saveTasks() {
    const tdraft = Object.values(draftTasks);
    const dirtyPersisted = Object.values(tasksCache).some(t => t.dirty);

    if (!tdraft.length && !dirtyPersisted) {
        return alert("No hay tareas nuevas ni cambios pendientes para guardar.");
    }

    // Validar categorías solo de los drafts
    for (const t of tdraft) {
        if (!t.allowedCategories.length)
            return alert(`La tarea "${t.name}" no tiene categorías`);
    }

    // Guardar solo drafts
    if (tdraft.length) {
        const res = await secureFetch("/api/tasks", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(tdraft)
        });


        if (!res.ok) return alert("Error guardando tareas");

        // Limpiar draft
        Object.keys(draftTasks).forEach(k => delete draftTasks[k]);
    }

    // Limpiar flags dirty de persistidas
    Object.values(tasksCache).forEach(t => t.dirty = false);

    // Recargar todo
    await loadTasks();

    alert("Tareas guardadas correctamente");
}
