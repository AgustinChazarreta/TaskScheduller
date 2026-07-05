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
   DATA LOAD
===================================================== */

async function loadPersons() {
    try {
        const response = await fetch('/api/persons');
        if (!response.ok) throw new Error();

        const data = await response.json();
        const persons = Array.isArray(data) ? data : Object.values(data);

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
            <table class="table table-hover align-middle mb-0 persons-table">

                <thead>
                    <tr>
                        <th></th>
                        <th>Persona</th>
                        <th>Grupo</th>
                        <th>Disponibilidad</th>
                        <th>Estado</th>
                    </tr>
                </thead>

                <tbody>
                    ${persons.map(p => `
                        <tr>

                            <!-- FOTO -->
                            <td class="text-center">
                                <img
                                    src="${p.profileImageUrl || "/person-circle.svg"}"
                                    class="person-avatar-sm"
                                    alt="${p.fullName}"
                                >
                            </td>

                            <!-- NOMBRE + EDAD -->
                            <td>
                                <div class="fw-semibold">
                                    ${p.fullName}
                                </div>

                                <div class="text-muted small">
                                    ${calculateAge(p.birthDate)} años
                                </div>

                                ${p.nickName
            ? `<div class="text-muted small fst-italic">${p.nickName}</div>`
            : ""
        }
                            </td>

                            <!-- GRUPO -->
                            <td class="text-center">
                                ${p.groupName
            ? `<span class="badge bg-warning-subtle text-warning px-3 py-2">
                                        ${p.groupName}
                                       </span>`
            : `<span class="text-muted">-</span>`
        }
                            </td>

                            <!-- DISPONIBILIDAD -->
                            <td class="text-center">
                                ${formatDays(p.workingDays)}
                            </td>

                            <!-- ESTADO (con punto) -->
                            <td class="text-center">
                                <span class="status-badge ${p.active ? "status-active" : "status-inactive"}">
                                    <span class="dot"></span>
                                    ${p.active ? "Activo" : "Inactivo"}
                                </span>
                            </td>

                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

/* =====================================================
   UTILS
===================================================== */

function formatDays(days = []) {

    const order = [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
        "SUNDAY"
    ];

    const labels = {
        MONDAY: "Lun",
        TUESDAY: "Mar",
        WEDNESDAY: "Mié",
        THURSDAY: "Jue",
        FRIDAY: "Vie",
        SATURDAY: "Sáb",
        SUNDAY: "Dom"
    };

    if (!days || !days.length) {
        return `<span class="text-muted">-</span>`;
    }

    const mondayToFriday = [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY"
    ];

    const allDays = [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
        "SUNDAY"
    ];

    if (
        mondayToFriday.every(d => days.includes(d)) &&
        days.length === 5
    ) {
        return `<span class="day-badge">Lun - Vie</span>`;
    }

    if (
        allDays.every(d => days.includes(d)) &&
        days.length === 7
    ) {
        return `<span class="day-badge">Todos los días</span>`;
    }

    return order
        .filter(d => days.includes(d))
        .map(d =>
            `<span class="day-badge">
                ${labels[d]}
            </span>`
        )
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

function calculateAge(birthDate) {
    if (!birthDate) return "-";

    const today = new Date();
    const birth = new Date(birthDate);

    let age = today.getFullYear() - birth.getFullYear();

    const monthDiff = today.getMonth() - birth.getMonth();
    const dayDiff = today.getDate() - birth.getDate();

    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
        age--;
    }

    return age;
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