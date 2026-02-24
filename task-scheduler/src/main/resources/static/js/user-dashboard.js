/* ===============================
   INIT
================================ */

document.addEventListener("DOMContentLoaded", () => {
    loadProfile();
    loadSchedule();
});

let currentUser = null;
let editMode = false;


/* ===============================
   PROFILE
================================ */

async function loadProfile() {

    const container = document.getElementById("profileContainer");

    try {

        const res = await secureFetch("/api/user/me");
        if (!res.ok) throw new Error();

        currentUser = await res.json();
        renderProfile();

    } catch (err) {
        container.innerHTML =
            `<p class="text-danger">Error al cargar perfil</p>`;
    }
}


function renderProfile() {

    const container = document.getElementById("profileContainer");

    if (!editMode) {

        container.innerHTML = `
            <div class="row align-items-center">

                <div class="col-md-3 text-center mb-3">
                    <img src="${currentUser.profileImageUrl || '/avatar-example.png'}"
                         class="img-fluid rounded-circle shadow-sm"
                         style="max-width:150px;">
                </div>

                <div class="col-md-9">
                    <h4>${currentUser.fullName}</h4>
                    <p><strong>Email:</strong> ${currentUser.email}</p>
                    <p><strong>Ingreso:</strong> ${formatDate(currentUser.entryDate)}</p>
                    <p><strong>Estado:</strong>
                        <span class="badge ${currentUser.active ? 'bg-success' : 'bg-secondary'}">
                            ${currentUser.active ? 'Activo' : 'Inactivo'}
                        </span>
                    </p>
                </div>
            </div>
        `;

    } else {

        container.innerHTML = `
            <form id="profileForm">

                <div class="mb-3">
                    <label class="form-label">Nombre completo</label>
                    <input type="text" class="form-control"
                           id="fullName"
                           value="${currentUser.fullName}">
                </div>

                <div class="mb-3">
                    <label class="form-label">Nickname</label>
                    <input type="text" class="form-control"
                           id="nickName"
                           value="${currentUser.nickName || ''}">
                </div>

                <div class="mb-3">
                    <label class="form-label">Fecha de nacimiento</label>
                    <input type="date" class="form-control"
                           id="birthDate"
                           value="${currentUser.birthDate || ''}">
                </div>

                <button type="submit" class="btn btn-primary me-2">
                    Guardar
                </button>

                <button type="button"
                        class="btn btn-secondary"
                        onclick="cancelEdit()">
                    Cancelar
                </button>

            </form>
        `;

        document
            .getElementById("profileForm")
            .addEventListener("submit", saveProfile);
    }
}


function enableEdit() {
    editMode = true;
    renderProfile();
}

function cancelEdit() {
    editMode = false;
    renderProfile();
}


async function saveProfile(e) {

    e.preventDefault();

    const payload = {
        fullName: document.getElementById("fullName").value,
        nickName: document.getElementById("nickName").value,
        birthDate: document.getElementById("birthDate").value
    };

    const res = await secureFetch("/api/user/me", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        alert("Error al actualizar perfil");
        return;
    }

    editMode = false;
    loadProfile();
}


/* ===============================
   SCHEDULE
================================ */

async function loadSchedule() {

    const container = document.getElementById("assignmentsContainer");
    const weekLabel = document.getElementById("weekLabel");

    try {

        const res = await secureFetch("/api/user/my-schedule");
        if (!res.ok) throw new Error();

        const data = await res.json();

        weekLabel.textContent = data.weekLabel || "Mi Semana";
        container.innerHTML = "";

        if (!data.assignments || data.assignments.length === 0) {
            container.innerHTML =
                `<p class="text-muted">No tenés asignaciones esta semana.</p>`;
            return;
        }

        data.assignments.forEach(a => {

            const card = document.createElement("div");
            card.className = "card shadow-sm";
            card.style.width = "16rem";

            card.innerHTML = `
                <div class="card-header text-center fw-bold">
                    ${a.day}
                </div>
                <div class="card-body text-center">
                    <span class="badge bg-primary">
                        ${a.functionName}
                    </span>
                </div>
            `;

            container.appendChild(card);
        });

    } catch (err) {

        container.innerHTML =
            `<p class="text-danger">Error al cargar agenda</p>`;
    }
}


/* ===============================
   HELPERS
================================ */

function formatDate(dateString) {
    if (!dateString) return "-";
    return new Date(dateString).toLocaleDateString("es-AR");
}