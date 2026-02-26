/* ===============================
   REFERENCES & STATE
================================ */

const tbody = document.getElementById("personsBody");
const form = document.getElementById("personForm");
const modalEl = document.getElementById("personModal");
const modalTitle = document.getElementById("modalTitle");

const photoInput = document.getElementById("personPhoto");
const photoPreview = document.getElementById("personPhotoPreview");

let editingPersonId = null;
let personIdToDelete = null;
let personNameToDelete = null;
const personsCache = {};


/* ===============================
   HELPERS
================================ */

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
        MONDAY: "Lunes",
        TUESDAY: "Martes",
        WEDNESDAY: "Miércoles",
        THURSDAY: "Jueves",
        FRIDAY: "Viernes",
        SATURDAY: "Sábado",
        SUNDAY: "Domingo"
    };

    return order
        .filter(d => days?.includes(d))
        .map(d =>
            `<span class="badge bg-primary-subtle text-primary me-1">
                ${labels[d]}
            </span>`
        )
        .join("");
}

/* ===============================
   BOOTSTRAP ALERT
================================ */

function showAlert(message, type = "success", insideModal = false) {

    const container = insideModal
        ? document.getElementById("modalAlertContainer")
        : document.getElementById("alertContainer");

    if (!container) return;

    const alert = document.createElement("div");
    alert.className = `alert alert-${type} alert-dismissible fade show rounded-3 shadow-sm`;

    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    container.appendChild(alert);

    if (insideModal) {
        const modalBody = modalEl.querySelector(".modal-body");
        modalBody.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    setTimeout(() => {
        alert.classList.remove("show");
        alert.remove();
    }, 8000);
}


/* ===============================
   LOAD PERSONS
================================ */

async function loadPersons() {
    const res = await fetch("/api/persons");
    const data = await res.json();

    tbody.innerHTML = "";

    if (!data.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center py-5 text-muted">
                    No hay personas registradas.
                </td>
            </tr>`;
        return;
    }

    data
        .sort((a, b) => a.fullName.localeCompare(b.fullName))
        .forEach((p, index) => {

            personsCache[p.id] = p;

            tbody.insertAdjacentHTML("beforeend", `
                <tr>

                    <td class="ps-4 text-center fw-semibold">
                        ${index + 1}
                    </td>

                    <td class="fw-semibold">
                        ${p.fullName}
                        ${p.nickName ? `<br><small class="text-muted">${p.nickName}</small>` : ""}
                    </td>

                    <td>
    <div class="fw-semibold">
        ${new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: 'long', year: 'numeric' }).format(new Date(p.birthDate + 'T00:00'))}
    </div>
</td>

                    <td>
                        ${p.email
                    ? `<span class="badge bg-secondary-subtle text-secondary">
                                      ${p.email}
                                   </span>`
                    : `<span class="text-muted">-</span>`
                }
                    </td>

                    <td>
                        <span class="badge ${p.active
                    ? "bg-success-subtle text-success"
                    : "bg-secondary-subtle text-secondary"
                }">
                            ${p.active ? "Activo" : "Inactivo"}
                        </span>
                    </td>

                    <td>
                        ${formatDays(p.workingDays)}
                    </td>

                    <td class="text-end pe-4">

                        <button class="btn btn-sm btn-outline-secondary rounded-circle me-2"
                                onclick="editPerson('${p.id}')">
                            <i class="bi bi-pencil"></i>
                        </button>

                        <button class="btn btn-sm btn-outline-danger rounded-circle"
                                onclick="openDeletePersonModal('${p.id}', '${p.fullName}')">
                            <i class="bi bi-trash"></i>
                        </button>

                    </td>

                </tr>
            `);
        });
}


/* ===============================
   CREATE / UPDATE
================================ */

form.addEventListener("submit", async e => {
    e.preventDefault();

    const payload = {
        fullName: document.getElementById("personName").value.trim(),
        nickName: document.getElementById("personNickname").value.trim() || null,
        birthDate: document.getElementById("personBirthDate").value,
        active: document.getElementById("personStatus").checked,
        email: document.getElementById("personEmail").value.trim(),
        emailNotificationsEnabled: document.getElementById("mailStatus").checked,
        entryDate: document.getElementById("personEntryDate").value,
        exitDate: document.getElementById("personExitDate").value || null,
        workingDays: $('#personDays').val(),
        functionIds: $("#personFunctions").val().map(Number)
    };

    const url = editingPersonId
        ? `/api/persons/${editingPersonId}`
        : "/api/persons";

    const method = editingPersonId ? "PUT" : "POST";

    // 🔥 GUARDAMOS EL RESPONSE
    const res = await secureFetch(url, {
        method,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {

        showAlert(`
        <div class="d-flex align-items-start gap-2">
            <i class="bi bi-x-circle-fill text-danger fs-5 mt-1"></i>
            <div>
                <div class="fw-semibold">No se pudo guardar la persona</div>
                <small class="text-muted">
                    Ya existe una persona con ese email.
                </small>
            </div>
        </div>
    `, "danger", true);   // 👈 true = dentro del modal

        return;
    }

    let personId = editingPersonId;

    if (!editingPersonId) {
        const result = await res.json();
        personId = result.personId;

        // 🔥 Mostrar password temporal al admin
        showAlert(`
            <i class="bi bi-check2-circle text-success fs-5"></i>
            <span class="fw-bold">Persona creada correctamente</span><br>
            Email: <strong>${payload.email}</strong><br>
            Password temporal: <strong>${result.temporaryPassword}</strong><br>
            El usuario deberá cambiarla al ingresar.
            `, "success");
    }

    // Subida de imagen
    const file = photoInput.files[0];
    if (file) {
        await uploadProfileImage(personId, file);
    }

    bootstrap.Modal.getInstance(modalEl).hide();
    resetForm();
    loadPersons();

});




/* ===============================
   EDIT
================================ */

function editPerson(id) {
    const p = personsCache[id];
    if (!p) return;

    editingPersonId = id;
    modalTitle.innerHTML = `
        <i class="bi bi-person-check me-2"></i>Editar persona
    `;

    personName.value = p.fullName;
    personNickname.value = p.nickName;
    personBirthDate.value = p.birthDate;
    personEmail.value = p.email;
    mailStatus.checked = p.emailNotificationsEnabled;
    personStatus.checked = p.active;
    personEntryDate.value = p.entryDate;
    personExitDate.value = p.exitDate || "";

    $("#personFunctions").val(p.functions.map(f => f.id)).trigger("change");
    $("#personDays").val(p.workingDays).trigger("change");

    photoPreview.src = p.profileImageUrl
        ? p.profileImageUrl
        : "/person-circle.svg";


    new bootstrap.Modal(modalEl).show();
}


/* ===============================
   DELETE
================================ */

// Abrir modal
function openDeletePersonModal(id, name) {
    personIdToDelete = id;
    personNameToDelete = name; // 👈 guardamos el string
    document.getElementById("deletePersonName").textContent = name;

    const modal = new bootstrap.Modal(
        document.getElementById("deletePersonModal")
    );
    modal.show();
}

// Confirmar eliminación
document
    .getElementById("confirmDeletePersonBtn")
    .addEventListener("click", async function () {

        if (!personIdToDelete) return;

        const res = await secureFetch(
            `/api/persons/${personIdToDelete}`,
            { method: "DELETE" }
        );

        const modal = bootstrap.Modal.getInstance(
            document.getElementById("deletePersonModal")
        );

        modal.hide();

        if (!res.ok) {
            showAlert(`
                <div class="d-flex align-items-start gap-2">
                    <i class="bi bi-x-circle-fill text-danger fs-5 mt-1"></i>
                    <div>
                        <div class="fw-semibold">
                            No se pudo eliminar la persona
                        </div>
                    </div>
                </div>
            `, "danger");

            personIdToDelete = null;
            return;
        }

        showAlert(`
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-trash-fill text-success fs-5 mt-1"></i>
        <div>
            <div class="fw-semibold">
                Persona eliminada c
            </div>
            <small class="text-muted">
                ${personNameToDelete} fue eliminado del sistema.
            </small>
        </div>
    </div>
`, "success");

        personIdToDelete = null;

        loadPersons();
    });
    
/* ===============================
   RESET FORM
================================ */

function resetForm() {
    editingPersonId = null;

    modalTitle.innerHTML = `<i class="bi bi-person-plus me-2"></i>Agregar persona`;

    form.reset();

    $("#personFunctions").val(null).trigger("change");
    $("#personDays").val(null).trigger("change");

    photoPreview.src = "/person-circle.svg";
    if (photoInput) { photoInput.value = ""; }
}


/* ===============================
   SELECT2 (MODAL SAFE)
================================ */

modalEl.addEventListener("shown.bs.modal", () => {
    [
        { id: "#personFunctions", placeholder: "Seleccioná funciones" },
        { id: "#personDays", placeholder: "Seleccioná días de trabajo" }
    ]
        .forEach(cfg => {
            const $el = $(cfg.id);
            if ($el.hasClass("select2-hidden-accessible")) return;

            $el.select2({
                placeholder: cfg.placeholder,
                width: "100%",
                dropdownAutoWidth: true
            });
        });
});

modalEl.addEventListener("hidden.bs.modal", resetForm);


/* ===============================
   PHOTO PREVIEW
================================ */

photoInput.addEventListener("change", function () {
    const file = this.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
        showAlert("Solo se permiten imágenes", "danger");
        this.value = "";
        return;
    }

    if (file.size > 2 * 1024 * 1024) {
        showAlert("La imagen no puede superar los 2MB", "danger");
        this.value = "";
        return;
    }

    const reader = new FileReader();
    reader.onload = e => {
        photoPreview.src = e.target.result;
    };
    reader.readAsDataURL(file);
});


async function loadFunctions() {
    const res = await fetch("/api/functions");
    const functions = await res.json();

    const select = $("#personFunctions");
    select.empty();

    functions.forEach(f => {
        select.append(
            `<option value="${f.id}">${f.name}</option>`
        );
    });

    select.trigger("change");
}

/* ===============================
   UPLOAD PROFILE IMAGE
================================ */
async function uploadProfileImage(personId, file) {

    const formData = new FormData();
    formData.append("file", file);

    const res = await secureFetch(`/api/persons/${personId}/profile-image`, {
        method: "POST",
        body: formData
    });

    if (!res.ok) {
        showAlert("Error al subir la imagen", "danger");
    }
}


/* ===============================
   INIT
================================ */
loadFunctions();
loadPersons();
