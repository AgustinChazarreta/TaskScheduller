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
        // Mantiene tamaño y padding, solo cambia colores
        statusBadge.classList.remove(
            'bg-success-subtle',
            'text-success',
            'bg-danger-subtle',
            'text-danger'
        );

        if (data.invalidated) {
            statusBadge.classList.add('bg-danger-subtle', 'text-danger');
        } else {
            statusBadge.classList.add('bg-success-subtle', 'text-success');
        }

        const solvedText = data.lastSolvedAt
            ? new Date(data.lastSolvedAt).toLocaleString()
            : 'Nunca';

        lastSolved.innerHTML = `
            <span class="badge bg-secondary-subtle text-dark px-3 py-2 fs-6 fw-bold">
                ${solvedText}
            </span>
        `;

    } catch (error) {
        console.error('Error real cargando schedule:', error);
        setScheduleInvalid();
    }
}

function setScheduleInvalid() {
    const statusBadge = document.getElementById('scheduleStatus');
    const lastSolved = document.getElementById('lastSolvedAt');

    statusBadge.textContent = 'Inválido';
    statusBadge.classList.remove(
        'bg-success-subtle',
        'text-success',
        'bg-danger-subtle',
        'text-danger'
    );

    statusBadge.classList.add('bg-danger-subtle', 'text-danger');

    lastSolved.innerHTML = `
    <span class="badge bg-secondary-subtle text-dark px-3 py-2 fs-6 fw-bold">
        Nunca
    </span>
`;
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

            console.log(result)

            sessionStorage.setItem(
                'scheduleResults',
                JSON.stringify(result)
            );

            sessionStorage.setItem(
                'schedulePeriod',
                JSON.stringify({
                    startDate: startDate,
                    endDate: endDate
                })
            );

            sessionStorage.setItem(
                'lastSolvedAt',
                new Date().toLocaleString()
            );

            window.location.href = '/results';

        } catch (error) {
            hideSchedulerLoading();
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
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead class="table-light text-center">
                    <tr>
                        <th class="text-center"></th>
                        <th class="text-center">Nombre</th>
                        <th class="text-center">Cumpleaños</th>
                        <th class="text-center">Email</th>
                        <th class="text-center">Grupo</th>
                        <th class="text-center">Días disponibles</th>
                        <th class="text-center">Estado</th>
                    </tr>
                </thead>
                <tbody>
                    ${persons.map(p => `
                        <tr>

                            <!-- Ícono -->
                            <td class="text-center">
                                <i class="bi bi-person-circle text-secondary fs-5"></i>
                            </td>

                            <!-- Nombre -->
                            <td>
                                <span class="badge bg-primary-subtle text-primary px-3 py-2 fw-semibold fs-6">
                                    ${p.fullName}${p.nickName ? ` (${p.nickName})` : ""}
                                </span>
                            </td>

                            <!-- Cumpleaños -->
                            <td>
                                <span class="badge bg-light text-dark px-3 py-2 fw-semibold border">
                                    ${p.birthDate}
                                </span>
                            </td>

                            <!-- Email -->
                            <td>
                                <span class="badge bg-secondary-subtle text-secondary px-3 py-2 fw-semibold">
                                    ${p.email}
                                    </span>
                                    </td>
                                    
                                    <!-- Grupo -->
                                    <td>
                                        ${p.groupName
            ? `<span class="badge bg-warning-subtle text-warning px-3 py-2 fw-semibold">${p.groupName}</span>`
            : `<span class="text-muted">-</span>`}
                                    </td>
                                    
                                    <!-- Días -->
                                    <td class="text-center">
                                    ${formatDays(p.workingDays)}
                                    </td>
                                    
                            <!-- Estado -->
                            <td class="text-center">
                                <span class="badge ${p.active
            ? "bg-success-subtle text-success"
            : "bg-danger-subtle text-danger"
        } px-3 py-2 fw-bold">
                                    ${p.active ? "Activo" : "Inactivo"}
                                </span>
                            </td>

                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
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
        .map(d => `<span class="badge bg-primary-subtle text-primary me-1">${labels[d]}</span>`)
        .join("");
}

function showAlert(message, type) {
    document.getElementById('alertContainer').innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show rounded-3 shadow-sm">
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