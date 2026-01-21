/* ===========================================
   VARIABLES PRINCIPALES
=========================================== */
const tbody = document.getElementById("tasksBody");
const form = document.getElementById("taskForm");
const modalEl = document.getElementById("taskModal");
const modalTitle = document.getElementById("modalTitle");

let editingTaskName = null;   // nombre de la tarea que estamos editando
const tasksCache = {};        // tareas persistidas
const draftTasks = {};        // tareas cargadas de Word pero aún no guardadas
const categories = ["CATEGORY_1", "CATEGORY_2", "CATEGORY_3", "CATEGORY_4"];

/* ===========================================
   FORMATEO
=========================================== */
function formatDays(days) {
    const order = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
    const labels = {
        MONDAY: "Lunes", TUESDAY: "Martes", WEDNESDAY: "Miércoles",
        THURSDAY: "Jueves", FRIDAY: "Viernes", SATURDAY: "Sábado", SUNDAY: "Domingo"
    };
    return order.filter(d => days.includes(d))
        .map(d => `<span class="badge bg-danger bg-gradient me-1">${labels[d]}</span>`).join("");
}

function formatCategory(cat) {
    const mapping = {
        CATEGORY_1: "Categoría 1",
        CATEGORY_2: "Categoría 2",
        CATEGORY_3: "Categoría 3",
        CATEGORY_4: "Categoría 4"
    };
    return mapping[cat] || cat;
}

/* ===========================================
   CARGA DE TAREAS EXISTENTES
=========================================== */
async function loadStoredTasks() {
    const res = await fetch("/api/tasks");
    if (!res.ok) return;

    const data = await res.json();
    Object.keys(tasksCache).forEach(k => delete tasksCache[k]);
    Object.values(data).forEach(t => tasksCache[t.name] = t);

    render({ ...tasksCache, ...draftTasks });
}

document.addEventListener("DOMContentLoaded", loadStoredTasks);

/* ===========================================
   CARGA DESDE WORD
=========================================== */
async function loadTasksFromWord() {
    const file = document.getElementById("wordFile").files[0];
    if (!file) return alert("Seleccioná un Word");

    const fd = new FormData();
    fd.append("file", file);

    const res = await fetch("/api/tasks/from-word", { method: "POST", body: fd });
    if (!res.ok) return alert("Error leyendo el Word");

    const data = await res.json();
    Object.keys(draftTasks).forEach(k => delete draftTasks[k]);

    Object.entries(data).forEach(([name, days]) => {
        if (!tasksCache[name]) {
            draftTasks[name] = { name, assignedDays: days, allowedCategories: [] };
        }
    });

    render({ ...tasksCache, ...draftTasks });
}

/* ===========================================
   RENDER TABLA
=========================================== */
function render(source) {
    tbody.innerHTML = "";
    const tasks = Object.values(source);

    if (!tasks.length) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">Sin tareas</td></tr>`;
        return;
    }

    tasks.sort((a, b) => a.name.localeCompare(b.name)).forEach(t => {
        const categoriesStr = (t.allowedCategories || []).map(cat => `<span class="badge bg-warning bg-gradient text-dark me-1">${formatCategory(cat)}</span>`).join("");

        tbody.insertAdjacentHTML("beforeend", `
            <tr>
                <td>${t.name}</td>
                <td>${formatDays(t.assignedDays)}</td>
                <td>${categoriesStr}</td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editTask('${t.name}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteTask('${t.name}')">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `);
    });
}

/* ===========================================
   MODAL: AGREGAR / EDITAR
=========================================== */
// Nueva función para agregar tarea (abre modal limpio)
function addTask() {
    editingTaskName = null;
    resetForm();
    new bootstrap.Modal(modalEl).show();
}

// Modificada: ahora maneja agregar nueva o editar
function editTask(name) {
    editingTaskName = name;
    const task = draftTasks[name] || tasksCache[name];

    modalTitle.textContent = "Editar tarea";
    document.getElementById("taskName").value = task.name;
    
    // Marca los checkboxes de categorías basados en allowedCategories
    document.querySelectorAll("#taskModal .category").forEach(cb => {
        cb.checked = task.allowedCategories.includes(cb.value);
    });
    
    document.querySelectorAll("#taskModal .day").forEach(cb => {
        cb.checked = task.assignedDays.includes(cb.value);
    });

    new bootstrap.Modal(modalEl).show();
}

// Listener para resetear validación al abrir el modal
modalEl.addEventListener('shown.bs.modal', () => {
    // Resetea validación nativa para campos requeridos
    const fields = form.querySelectorAll('input[required], select[required]');
    fields.forEach(field => {
        field.setCustomValidity('');
    });

    // Quita borde rojo del contenedor de días y oculta el mensaje (si existe)
    const daysContainer = document.getElementById('daysContainer');
    if (daysContainer) {
        daysContainer.classList.remove('is-invalid');
    }
    const daysError = document.getElementById('daysError');
    if (daysError) {
        daysError.style.display = 'none';
    }
});

// Listener para el botón Cancelar: previene submit y resetea
document.querySelector('#taskModal .btn-secondary').addEventListener('click', (e) => {
    e.preventDefault();  // Detiene cualquier submit accidental
    resetForm();  // Limpia campos
    bootstrap.Modal.getInstance(modalEl).hide();  // Cierra el modal
});

// Modificado: valida en submit con alert simple para días y categorías
form.addEventListener("submit", e => {
    e.preventDefault();

    // Verificación simple: al menos un día debe estar seleccionado
    const selectedDays = document.querySelectorAll("#taskModal .day:checked");
    if (selectedDays.length === 0) {
        alert('Debes seleccionar al menos un día.');
        return;
    }

    // Verificación simple: al menos una categoría debe estar seleccionada
    const selectedCategories = document.querySelectorAll("#taskModal .category:checked");
    if (selectedCategories.length === 0) {
        alert('Debes seleccionar al menos una categoría.');
        return;
    }

    // Verifica si el formulario es válido (solo validación nativa para nombre)
    if (!form.checkValidity()) {
        form.reportValidity();  // Muestra mensajes en rojo nativos
        return;
    }

    // Si es válido, continúa con la lógica
    const name = document.getElementById("taskName").value.trim();
    const categories = Array.from(selectedCategories).map(cb => cb.value);
    const days = Array.from(selectedDays).map(cb => cb.value);

    if (editingTaskName) {
        // Editar tarea existente
        const task = draftTasks[editingTaskName] || { ...tasksCache[editingTaskName] };
        task.name = name;
        task.allowedCategories = categories;
        task.assignedDays = days;
        draftTasks[task.name] = task;
    } else {
        // Agregar nueva tarea
        if (draftTasks[name] || tasksCache[name]) {
            alert(`La tarea "${name}" ya existe.`);
            return;
        }
        const newTask = {
            name,
            assignedDays: days,
            allowedCategories: categories
        };
        draftTasks[name] = newTask;
    }

    render({ ...tasksCache, ...draftTasks });
    bootstrap.Modal.getInstance(modalEl).hide();
});

function resetForm() {
    editingTaskName = null;
    modalTitle.textContent = "Agregar tarea";
    form.reset();
}

modalEl.addEventListener("hidden.bs.modal", resetForm);

/* ===========================================
   ELIMINAR
=========================================== */
async function deleteTask(name) {
    if (!confirm(`¿Desea eliminar la tarea "${name}"?`)) return;

    delete draftTasks[name];
    delete tasksCache[name];

    const res = await fetch(`/api/tasks/${name}`, { method: "DELETE" });
    if (res.ok) render({ ...tasksCache, ...draftTasks });
}

/* ===========================================
   GUARDAR TAREAS
=========================================== */
async function saveTasks() {
    const tasks = Object.values(draftTasks);
    if (!tasks.length) return alert("No hay tareas nuevas para guardar");

    for (const t of tasks) {
        if (!t.allowedCategories.length)
            return alert(`La tarea "${t.name}" no tiene categorías asignadas`);
    }

    const res = await fetch("/api/tasks", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(tasks)
    });

    if (!res.ok) return alert("Error guardando tareas");

    tasks.forEach(t => tasksCache[t.name] = t);
    Object.keys(draftTasks).forEach(k => delete draftTasks[k]);
    render(tasksCache);
    alert("Tareas guardadas correctamente");
}