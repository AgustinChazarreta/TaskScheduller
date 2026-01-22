let selectedPersons = [];  // Array para almacenar IDs de personas seleccionadas
const personsCache = {};   // Cache para personas
const tasksCache = {};     // Cache para tareas

document.addEventListener('DOMContentLoaded', () => {
    loadPersons();
    loadTasks();  // Carga tareas
    bindForm();
    bindPersonSelection();  // Maneja cambios en checkboxes
});

// Función para formatear categorías (devuelve solo el texto)
function formatCategory(cat) {
    return (
        {
            CATEGORY_1: "Categoría 1",
            CATEGORY_2: "Categoría 2",
            CATEGORY_3: "Categoría 3",
            CATEGORY_4: "Categoría 4",
        }[cat] ?? cat
    );
}

// Función para formatear días disponibles (igual que en persons.js)
function formatDays(days) {
    const order = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

    const labels = {
        MONDAY: "Lunes",
        TUESDAY: "Martes",
        WEDNESDAY: "Miércoles",
        THURSDAY: "Jueves",
        FRIDAY: "Viernes",
        SATURDAY: "Sábado",
        SUNDAY: "Domingo",
    };

    return order
        .filter((d) => days.includes(d))
        .map((d) => `<span class="badge bg-danger me-1">${labels[d]}</span>`)
        .join("");
}

function bindForm() {
    const form = document.getElementById('scheduleForm');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        clearAlerts();  // Solo limpiar alertas

        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;

        if (!startDate || !endDate) {
            showAlert('Debe completar ambas fechas', 'warning');
            return;
        }

        if (selectedPersons.length === 0) {
            showAlert('Debe seleccionar al menos una persona', 'warning');
            return;
        }

        // Obtener personas seleccionadas completas desde el cache
        const selectedPersonsData = selectedPersons.map(id => personsCache[id]).filter(p => p);

        // Obtener todas las tareas desde el cache
        const tasksData = Object.values(tasksCache);

        try {
            const response = await fetch('/api/schedule/solve', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    startDate,
                    endDate,
                    selectedPersons: selectedPersonsData,  // Array de objetos personas
                    tasks: tasksData  // Array de objetos tareas
                })
            });

            if (!response.ok) {
                throw new Error('Error al resolver el schedule');
            }

            const result = await response.json();

            // Guardar resultados en localStorage para la nueva página
            sessionStorage.setItem('scheduleResults', JSON.stringify(result));

            // Redirigir a la página de resultados
            window.location.href = '/schedule-results';

        } catch (error) {
            showAlert(error.message, 'danger');
        }
    });
}

// Maneja cambios en los checkboxes de personas
function bindPersonSelection() {
    document.addEventListener('change', (e) => {
        if (e.target.classList.contains('person-checkbox')) {
            updateSelectedPersons();
        }
    });
}

// Actualiza el array de personas seleccionadas
function updateSelectedPersons() {
    selectedPersons = [...document.querySelectorAll('.person-checkbox:checked')].map(cb => cb.value);
    console.log('Personas seleccionadas:', selectedPersons);  // Para depurar
}

async function loadPersons() {
    try {
        const response = await fetch('/api/persons');

        if (!response.ok) {
            throw new Error();
        }

        const data = await response.json();
        const persons = Object.entries(data).map(([id, p]) => ({ id, ...p }));  // Agrega ID a cada persona
        // Actualiza cache
        persons.forEach(p => personsCache[p.id] = p);
        renderPersons(persons);

    } catch {
        showAlert('No se pudieron cargar las personas', 'danger');
    }
}

// Carga las tareas desde /api/tasks
async function loadTasks() {
    try {
        const response = await fetch('/api/tasks');

        if (!response.ok) {
            throw new Error();
        }

        const data = await response.json();
        // Asumiendo que data es un objeto {id: task}, conviértelo a array; si es array directo, usa data directamente
        const tasks = Array.isArray(data) ? data : Object.values(data);
        // Actualiza cache
        tasks.forEach(t => tasksCache[t.name] = t);  // Asumiendo que el ID es el nombre
        renderTasks(tasks);

    } catch {
        showAlert('No se pudieron cargar las tareas', 'danger');
    }
}

function renderPersons(persons) {
    const container = document.getElementById('personsContainer');

    if (!persons || persons.length === 0) {
        container.innerHTML = '<p class="text-muted mb-0">No hay personas cargadas</p>';
        return;
    }

    container.innerHTML = `
        <table class="table table-striped align-middle">
            <thead>
                <tr>
                    <th>Select</th>
                    <th style="width: 40px; text-align: center;"></th>
                    <th>Nombre</th>
                    <th>Categoría</th>
                    <th>Nacimiento</th>
                    <th>Días disponibles</th>
                </tr>
            </thead>
            <tbody>
                ${persons.map(p => `
                    <tr>
                        <td style="text-align: center;"><input type="checkbox" class="person-checkbox" value="${p.id}"></td>
                        <td style="text-align: center;"><i class="bi bi-person-fill"></i></td>
                        <td>${p.name}</td>
                        <td><span class="badge bg-warning text-dark">${formatCategory(p.category)}</span></td>
                        <td>${p.birthDate}</td>
                        <td>${formatDays(p.availableDays)}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

function renderTasks(tasks) {
    const container = document.getElementById('tasksContainer');

    if (!tasks || tasks.length === 0) {
        container.innerHTML = '<p class="text-muted mb-0">No hay tareas cargadas</p>';
        return;
    }

    container.innerHTML = `
        <table class="table table-striped align-middle">
            <thead>
                <tr>
                    <th>Tarea</th>
                    <th>Días asignados</th>
                    <th>Categorías</th>
                </tr>
            </thead>
            <tbody>
                ${tasks.map(t => {
        const categoriesStr = (t.allowedCategories || []).map(cat => `<span class="badge bg-warning text-dark me-1">${formatCategory(cat)}</span>`).join("");
        return `
                        <tr>
                            <td>${t.name}</td>
                            <td>${formatDays(t.assignedDays)}</td>
                            <td>${categoriesStr}</td>
                        </tr>
                    `;
    }).join('')}
            </tbody>
        </table>
    `;
}

function renderAssignments(assignments) {
    const tbody = document.getElementById('assignmentsBody');

    if (!assignments || assignments.length === 0) {
        tbody.innerHTML = `
                    <tr>
                        <td colspan="3" class="text-center text-muted">
                            No se generaron asignaciones
                        </td>
                    </tr>
                `;
        return;
    }

    tbody.innerHTML = assignments.map(a => `
                <tr>
                    <td>${a.day}</td>
                    <td>${a.taskName}</td>
                    <td>${a.personName}</td>
                </tr>
            `).join('');
}

function renderScore(score) {
    document.getElementById('scoreDisplay').textContent = `Score: ${score}`;
}

function showAlert(message, type) {
    document.getElementById('alertContainer').innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}

function clearAlerts() {
    document.getElementById('alertContainer').innerHTML = '';
}

function clearResults() {
    const tbody = document.getElementById('assignmentsBody');
    const scoreDisplay = document.getElementById('scoreDisplay');

    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="text-center text-muted">
                    Ejecutando solver...
                </td>
            </tr>
        `;
    }

    if (scoreDisplay) {
        scoreDisplay.textContent = '';
    }
}