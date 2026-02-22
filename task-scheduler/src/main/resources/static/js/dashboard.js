/* =====================================================
   ESTADO GLOBAL
===================================================== */

const personsCache = {};
const functionsCache = {};

/* =====================================================
   INIT
===================================================== */

document.addEventListener('DOMContentLoaded', () => {
    loadScheduleStatus();
    loadPersons();
    loadFunctions();
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

        // Si el backend devuelve 404, lo tratamos como estado inicial
        if (response.status === 404) {
            setScheduleInvalid();
            return;
        }

        if (!response.ok) {
            throw new Error('Error HTTP ' + response.status);
        }

        const data = await response.json();

        const statusBadge = document.getElementById('scheduleStatus');
        const lastSolved = document.getElementById('lastSolvedAt');

        statusBadge.textContent = data.invalidated ? 'Inválido' : 'Válido';
        statusBadge.className = data.invalidated
            ? 'badge bg-danger bg-gradient fs-5 px-2 py-1'
            : 'badge bg-success bg-gradient fs-5 px-2 py-1';

        lastSolved.textContent =
            data.lastSolvedAt
                ? new Date(data.lastSolvedAt).toLocaleString()
                : 'Nunca';

    } catch (error) {
        console.error('Error real cargando schedule:', error);

        // 👇 En vez de mostrar alerta roja,
        // simplemente lo tratamos como estado inicial
        setScheduleInvalid();
    }
}

function setScheduleInvalid() {
    const statusBadge = document.getElementById('scheduleStatus');
    const lastSolved = document.getElementById('lastSolvedAt');

    statusBadge.textContent = 'Inválido';
    statusBadge.className =
        'badge bg-danger bg-gradient fs-5 px-2 py-1';

    lastSolved.textContent = 'Nunca';
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

        const activePersonIds = Object.values(personsCache)
            .filter(p => p.active)
            .map(p => p.id);

        try {
            // 👇 ACA MOSTRAMOS EL SPINNER
            showSchedulerLoading();

            const response = await secureFetch('/api/schedule/solve', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    period: {
                        startDate: startDate,
                        endDate: endDate
                    },
                    personIds: activePersonIds,
                    functionIds: Object.values(functionsCache).map(f => f.id)
                })

            });


            if (!response.ok) {
                throw new Error('Error al resolver el schedule');
            }

            const result = await response.json();

            sessionStorage.setItem(
                'scheduleResults',
                JSON.stringify(result)
            );

            sessionStorage.setItem(
                'lastSolvedAt',
                new Date().toLocaleString()
            );

            // ⛔ no hace falta ocultar spinner, redireccionás
            window.location.href = '/results';

        } catch (error) {
            hideSchedulerLoading(); // 👈 SOLO si falla
            showAlert(error.message, 'danger');
            console.error(error);
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

async function loadFunctions() {
    try {
        const response = await fetch('/api/functions');
        console.log('functions response status:', response.status);

        if (!response.ok) throw new Error('HTTP ' + response.status);

        const data = await response.json();
        console.log('functions raw data:', data);

        const functions = Array.isArray(data) ? data : Object.values(data);
        console.log('functions parsed:', functions);

        functions.forEach(f => functionsCache[f.id] = f);
        renderFunctions(functions);

    } catch (e) {
        console.error('loadFunctions error:', e);
        showAlert('No se pudieron cargar las funciones', 'danger');
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
                    <th>Nombre</th>
                    <th>Cumpleaños</th>
                    <th>Email</th>
                    <th class="text-center">Estado</th>
                    <th class="text-center">Días disponibles</th>
                </tr>
            </thead>
            <tbody>
                ${persons.map(p => `
                    <tr>
                        <td>${p.fullName}${p.nickName ? ` (${p.nickName})` : ""}</td>
                        <td>${p.birthDate}</td>
                        <td>${p.email}</td>
                        <td class="text-center">
                            <span class="badge ${p.active ? "bg-success" : "bg-secondary"}">
                                ${p.active ? "Activo" : "Inactivo"}
                            </span>
                        </td>
                        <td class = "text-center">${formatDays(p.workingDays)}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

function renderFunctions(functions) {
    const container = document.getElementById('functionsContainer');

    if (!functions.length) {
        container.innerHTML =
            '<p class="text-muted mb-0">No hay funciones cargadas</p>';
        return;
    }

    container.innerHTML = `
        <table class="table table-striped align-middle">
            <thead>
                <tr>
                    <th>Función</th>
                    <th>Tipo de Función</th>
                    <th>Días asignados</th>
                </tr>
            </thead>
            <tbody>
                ${functions.map(f => `
                    <tr>
                        <td>${f.name}</td>
                        <td>
                            <span class="badge ${f.sequential ? "bg-warning text-dark" : "bg-secondary"} bg-gradient me-1">
                            ${f.sequential ? "Recurrente" : "Una vez"}
                            </span>
                        </td>
                        <td>${formatDays(f.assignedDays)}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

/* =====================================================
   UTILS
===================================================== */
function formatDays(days = []) {
    const order = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
    const labels = {
        MONDAY: "Lunes",
        TUESDAY: "Martes",
        WEDNESDAY: "Miércoles",
        THURSDAY: "Jueves",
        FRIDAY: "Viernes",
        SATURDAY: "Sábado",
        SUNDAY: "Domingo"
    };

    return order
        .filter(d => days.includes(d))
        .map(d => `<span class="badge bg-danger bg-gradient me-1">${labels[d]}</span>`)
        .join("");
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

/* =====================================================
   OVERLAY
===================================================== */
function showSchedulerLoading() {
    document.getElementById('resolveBtn').disabled = true;
    document.getElementById('resolveText').classList.add('d-none');
    document.getElementById('resolveSpinner').classList.remove('d-none');
}

function hideSchedulerLoading() {
    document.getElementById('resolveBtn').disabled = false;
    document.getElementById('resolveSpinner').classList.add('d-none');
    document.getElementById('resolveText').classList.remove('d-none');
}
