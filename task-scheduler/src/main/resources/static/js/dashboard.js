/* =====================================================
   ESTADO GLOBAL
===================================================== */

let selectedPersons = [];
const personsCache = {};
const tasksCache = {};

/* =====================================================
   INIT
===================================================== */

document.addEventListener('DOMContentLoaded', () => {
    loadScheduleStatus();
    loadPersons();
    loadTasks();
    bindForm();
    bindPersonSelection();
    bindSelectionButtons();
});

/* =====================================================
   ESTADO DEL SCHEDULE
===================================================== */

async function loadScheduleStatus() {
    try {
        const response = await fetch('/api/admin/schedule/status');
        if (!response.ok) throw new Error();

        const data = await response.json();

        const statusBadge = document.getElementById('scheduleStatus');
        const lastSolved = document.getElementById('lastSolvedAt');

        statusBadge.textContent = data.invalidated ? 'Invalidado' : 'Válido';
        statusBadge.className = data.invalidated
            ? 'badge bg-danger'
            : 'badge bg-success';

        lastSolved.textContent =
            sessionStorage.getItem('lastSolvedAt') || 'Nunca';

    } catch {
        showAlert('No se pudo cargar el estado del schedule', 'danger');
    }
}

/* =====================================================
   FORMULARIO
===================================================== */

function bindForm() {
    const form = document.getElementById('scheduleForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlerts();

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

        const persons = selectedPersons
            .map(id => personsCache[id])
            .filter(Boolean);

        const tasks = Object.values(tasksCache);

        try {
            const response = await secureFetch('/api/schedule/solve', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    period: { startDate, endDate },
                    persons,
                    tasks
                })
            });

            if (!response.ok) {
                throw new Error('Error al resolver el schedule');
            }

            const result = await response.json();

            // 👇 igual que antes en index
            sessionStorage.setItem(
                'scheduleResults',
                JSON.stringify(result)
            );

            sessionStorage.setItem(
                'lastSolvedAt',
                new Date().toLocaleString()
            );

            // 👉 volvemos a results
            window.location.href = '/results';

        } catch (error) {
            showAlert(error.message, 'danger');
        }
    });
}


/* =====================================================
   PERSONAS
===================================================== */

function bindPersonSelection() {
    document.addEventListener('change', (e) => {
        if (e.target.classList.contains('person-checkbox')) {
            updateSelectedPersons();
        }
    });
}

function bindSelectionButtons() {
    document.getElementById('selectAllPersons')
        ?.addEventListener('click', () => {
            document
                .querySelectorAll('.person-checkbox')
                .forEach(cb => cb.checked = true);
            updateSelectedPersons();
        });

    document.getElementById('deselectAllPersons')
        ?.addEventListener('click', () => {
            document
                .querySelectorAll('.person-checkbox')
                .forEach(cb => cb.checked = false);
            updateSelectedPersons();
        }); 
}

function updateSelectedPersons() {
    selectedPersons = [...document.querySelectorAll('.person-checkbox:checked')]
        .map(cb => cb.value);
}

/* =====================================================
   DATA LOAD
===================================================== */

async function loadPersons() {
    try {
        const response = await fetch('/api/persons');
        if (!response.ok) throw new Error();

        const data = await response.json();
        const persons = Object.entries(data)
            .map(([id, p]) => ({ id, ...p }));

        persons.forEach(p => personsCache[p.id] = p);
        renderPersons(persons);

    } catch {
        showAlert('No se pudieron cargar las personas', 'danger');
    }
}

async function loadTasks() {
    try {
        const response = await fetch('/api/tasks');
        if (!response.ok) throw new Error();

        const data = await response.json();
        const tasks = Array.isArray(data) ? data : Object.values(data);

        tasks.forEach(t => tasksCache[t.name] = t);
        renderTasks(tasks);

    } catch {
        showAlert('No se pudieron cargar las tareas', 'danger');
    }
}

/* =====================================================
   RENDER
===================================================== */

function renderPersons(persons) {
    const container = document.getElementById('personsContainer');

    if (!persons.length) {
        container.innerHTML =
            '<p class="text-muted mb-0">No hay personas cargadas</p>';
        return;
    }

    container.innerHTML = `
        <table class="table table-striped align-middle">
            <thead>
                <tr>
                    <th></th>
                    <th>Nombre</th>
                    <th>Categoría</th>
                    <th>Nacimiento</th>
                    <th>Días disponibles</th>
                </tr>
            </thead>
            <tbody>
                ${persons.map(p => `
                    <tr>
                        <td>
                            <input type="checkbox"
                                   class="person-checkbox"
                                   value="${p.id}">
                        </td>
                        <td>${p.name}</td>
                        <td>
                            <span class="badge bg-warning text-dark">
                                ${formatCategory(p.category)}
                            </span>
                        </td>
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

    if (!tasks.length) {
        container.innerHTML =
            '<p class="text-muted mb-0">No hay tareas cargadas</p>';
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
                ${tasks.map(t => `
                    <tr>
                        <td>${t.name}</td>
                        <td>${formatDays(t.assignedDays)}</td>
                        <td>
                            ${(t.allowedCategories || [])
            .map(c =>
                `<span class="badge bg-warning text-dark me-1">
                                        ${formatCategory(c)}
                                    </span>`
            ).join('')}
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

/* =====================================================
   UTILS
===================================================== */

function formatCategory(cat) {
    return {
        CATEGORY_1: 'Categoría 1',
        CATEGORY_2: 'Categoría 2',
        CATEGORY_3: 'Categoría 3',
        CATEGORY_4: 'Categoría 4'
    }[cat] ?? cat;
}

function formatDays(days = []) {
    const labels = {
        MONDAY: 'Lunes',
        TUESDAY: 'Martes',
        WEDNESDAY: 'Miércoles',
        THURSDAY: 'Jueves',
        FRIDAY: 'Viernes',
        SATURDAY: 'Sábado',
        SUNDAY: 'Domingo'
    };

    return days.map(d =>
        `<span class="badge bg-danger me-1">${labels[d]}</span>`
    ).join('');
}

function showAlert(message, type) {
    document.getElementById('alertContainer').innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show">
            ${message}
            <button type="button"
                    class="btn-close"
                    data-bs-dismiss="alert"></button>
        </div>
    `;
}

function clearAlerts() {
    document.getElementById('alertContainer').innerHTML = '';
}
