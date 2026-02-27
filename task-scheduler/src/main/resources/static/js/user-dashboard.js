/* ===============================
   STATE & REFERENCES
================================ */

const profileContainer = document.getElementById("profileContainer");
const assignmentsContainer = document.getElementById("assignmentsContainer");

let profileEditMode = false;
let currentUser = null;


/* ===============================
   HELPERS
================================ */

function formatDate(dateString) {
    if (!dateString) return "-";
    return new Intl.DateTimeFormat("es-AR", { day: '2-digit', month: 'long', year: 'numeric' })
        .format(new Date(dateString + 'T00:00'));
}

function formatDays(days = []) {
    const order = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
    const labels = {
        MONDAY: "Lunes", TUESDAY: "Martes", WEDNESDAY: "Miércoles",
        THURSDAY: "Jueves", FRIDAY: "Viernes", SATURDAY: "Sábado", SUNDAY: "Domingo"
    };
    return order
        .filter(d => days.includes(d))
        .map(d => `<span class="badge bg-primary-subtle text-primary me-1">${labels[d]}</span>`)
        .join("");
}

function showAlert(message, type = "success") {
    const container = document.getElementById("alertContainer");
    if (!container) return;

    const alert = document.createElement("div");
    alert.className = `alert alert-${type} alert-dismissible fade show rounded-3 shadow-sm`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    container.appendChild(alert);

    setTimeout(() => {
        alert.classList.remove("show");
        alert.remove();
    }, 6000);
}


/* ===============================
   PROFILE
================================ */

async function loadProfile() {
    try {
        const res = await secureFetch("/api/user/me");
        if (!res.ok) throw new Error();
        currentUser = await res.json();
        renderProfile();
    } catch (err) {
        profileContainer.innerHTML = `<p class="text-danger">Error al cargar perfil</p>`;
    }
}

function renderProfile() {
    if (!currentUser) return;

    if (!profileEditMode) {
        profileContainer.innerHTML = `
            <div class="row align-items-center mb-4">
                <div class="col-md-3 text-center mb-3">
                    <img src="${currentUser.profileImageUrl || '/person-circle.svg'}" class="img-fluid rounded-circle shadow-sm" style="max-width:150px;">
                </div>
                <div class="col-md-9">
                    <h4>${currentUser.fullName} ${currentUser.nickName ? `(<small>${currentUser.nickName}</small>)` : ""}</h4>
                    <p><strong>Email:</strong> ${currentUser.email}</p>
                    <p><strong>Fecha de nacimiento:</strong> ${formatDate(currentUser.birthDate)}</p>
                    <p><strong>Ingreso:</strong> ${formatDate(currentUser.entryDate)}</p>
                    <p><strong>Estado:</strong> <span class="badge ${currentUser.active ? 'bg-success' : 'bg-secondary'}">${currentUser.active ? 'Activo' : 'Inactivo'}</span></p>
                    <p><strong>Notificaciones:</strong> <span class="badge ${currentUser.emailNotificationsEnabled ? 'bg-success' : 'bg-secondary'}">${currentUser.emailNotificationsEnabled ? 'Activadas' : 'Desactivadas'}</span></p>
                    <button class="btn btn-outline-primary mt-2" onclick="enableProfileEdit()">Editar</button>
                </div>
            </div>
        `;
    } else {
        profileContainer.innerHTML = `
            <form id="profileForm" class="row g-3">
                <div class="col-md-3 text-center mb-3">
                    <img src="${currentUser.profileImageUrl || '/person-circle.svg'}" id="profilePhotoPreview" class="img-fluid rounded-circle shadow-sm mb-2" style="max-width:150px;">
                    <input type="file" id="profilePhotoInput" class="form-control mt-2">
                </div>
                <div class="col-md-9">
                    <div class="mb-2">
                        <label class="form-label">Nombre completo</label>
                        <input type="text" id="profileName" class="form-control" value="${currentUser.fullName}">
                    </div>
                    <div class="mb-2">
                        <label class="form-label">Nickname</label>
                        <input type="text" id="profileNickname" class="form-control" value="${currentUser.nickName || ""}">
                    </div>
                    <div class="mb-2">
                        <label class="form-label">Email</label>
                        <input type="email" id="profileEmail" class="form-control" value="${currentUser.email}">
                    </div>
                    <div class="mb-2">
                        <label class="form-label">Fecha de nacimiento</label>
                        <input type="date" id="profileBirthDate" class="form-control" value="${currentUser.birthDate || ""}">
                    </div>
                    <div class="form-check mb-2">
                        <input class="form-check-input" type="checkbox" id="profileMailStatus" ${currentUser.emailNotificationsEnabled ? "checked" : ""}>
                        <label class="form-check-label">Notificaciones por email</label>
                    </div>
                    <button type="submit" class="btn btn-primary me-2">Guardar</button>
                    <button type="button" class="btn btn-secondary" onclick="cancelProfileEdit()">Cancelar</button>
                </div>
            </form>
        `;

        const photoInput = document.getElementById("profilePhotoInput");
        const photoPreview = document.getElementById("profilePhotoPreview");

        photoInput.addEventListener("change", function () {
            const file = this.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = e => photoPreview.src = e.target.result;
            reader.readAsDataURL(file);
        });

        document.getElementById("profileForm").addEventListener("submit", saveProfileChanges);
    }
}

function enableProfileEdit() {
    profileEditMode = true;
    renderProfile();
}

function cancelProfileEdit() {
    profileEditMode = false;
    renderProfile();
}

async function saveProfileChanges(e) {
    e.preventDefault();

    const payload = {
        fullName: document.getElementById("profileName").value.trim(),
        nickName: document.getElementById("profileNickname").value.trim() || null,
        birthDate: document.getElementById("profileBirthDate").value || null,
        email: document.getElementById("profileEmail").value.trim(),
        emailNotificationsEnabled: document.getElementById("profileMailStatus").checked
    };

    const res = await secureFetch("/api/user/me", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        showAlert("Error al actualizar perfil", "danger");
        return;
    }

    // Subida de foto
    const photoFile = document.getElementById("profilePhotoInput").files[0];
    if (photoFile) {
        await uploadProfileImage(currentUser.id, photoFile);
    }

    profileEditMode = false;
    loadProfile();
}


/* ===============================
   AGENDA
================================ */

async function loadMySchedule() {
    try {
        const res = await secureFetch("/api/user/my-schedule");
        if (!res.ok) throw new Error();
        const data = await res.json();

        assignmentsContainer.innerHTML = "";

        if (!data.assignments || !data.assignments.length) {
            assignmentsContainer.innerHTML = `<p class="text-muted text-center py-4">No tenés asignaciones esta semana.</p>`;
            return;
        }

        const table = document.createElement("table");
        table.className = "table table-hover table-bordered shadow-sm";

        table.innerHTML = `
            <thead class="table-light text-center">
                <tr>
                    <th>Día</th>
                    <th>Función</th>
                    <th>Detalle</th>
                </tr>
            </thead>
            <tbody>
                ${data.assignments.map(a => `
                    <tr>
                        <td class="text-center fw-semibold">${a.day}</td>
                        <td class="text-center">
                            <span class="badge bg-primary">${a.functionName}</span>
                        </td>
                        <td class="text-center">${a.details || "-"}</td>
                    </tr>
                `).join('')}
            </tbody>
        `;

        assignmentsContainer.appendChild(table);
    } catch (err) {
        assignmentsContainer.innerHTML = `<p class="text-danger text-center py-4">Error al cargar agenda</p>`;
    }
}


/* ===============================
   INIT
================================ */

loadProfile();
loadMySchedule();