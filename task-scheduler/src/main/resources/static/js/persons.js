/* ===============================
   REFERENCES & STATE
================================ */

const tbody = document.getElementById("personsBody");
const form = document.getElementById("personForm");
const modalEl = document.getElementById("personModal");
const modalTitle = document.getElementById("modalTitle");
const saveBtn = document.getElementById("savePersonBtn");

const photoInput = document.getElementById("personPhoto");
const photoPreview = document.getElementById("personPhotoPreview");

let editingPersonId = null;
let personIdToDelete = null;
let personNameToDelete = null;
let pendingAlert = null;
const personsCache = {};
let selectedExternalPerson = null;

let allPersons = []; // 🔹 para el filtro


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

function setLoadingButton(button, loading = true) {
    if (loading) {
        button.disabled = true;
        button.dataset.originalText = button.innerHTML;
        button.innerHTML = `
            <span class="spinner-border spinner-border-sm me-2"></span>
            Guardando...
        `;
    } else {
        button.disabled = false;
        button.innerHTML = button.dataset.originalText;
    }
}

function getToday() {
    const today = new Date();
    today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
    return today.toISOString().split("T")[0];
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

    allPersons = data; // 🔹 guardamos todas

    renderPersons(data);
}


// ===============================
// RENDER PERSONS
// ===============================
function renderPersons(data) {

    tbody.innerHTML = "";

    if (!data.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center py-5 text-muted">
                <i class="bi bi-exclamation-triangle me-2"></i>
                    No hay personas registradas
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

                    <td class="text-center fw-semibold pe-3">
                        ${index + 1}
                    </td>

                    <td class="text-center fw-semibold">
                        ${p.fullName}
                        ${p.nickName ? `<br><small class="text-muted">${p.nickName}</small>` : ""}
                    </td>

                    <td class="text-center">
                        <div class="fw-semibold">
                            ${new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: 'long', year: 'numeric' })
                    .format(new Date(p.birthDate + 'T00:00'))}
                        </div>
                    </td>

                    <td class="text-center">
                        ${p.email
                    ? `<span class="badge bg-secondary-subtle text-secondary">${p.email}</span>`
                    : `<span class="text-muted">-</span>`}
                    </td>

                    <td class="text-center">
                        ${p.groupName
                    ? `<span class="badge bg-warning-subtle text-warning">${p.groupName}</span>`
                    : `<span class="text-muted">-</span>`}
                    </td>

                    <!-- DÍAS DISPONIBLES CENTRADOS -->
                    <td class="text-center">
                        <div class="d-flex justify-content-center flex-wrap gap-1">
                            ${formatDays(p.workingDays)}
                        </div>
                    </td>

                    <td class="text-center">
                        <span class="badge ${p.active
                    ? "bg-success-subtle text-success"
                    : "bg-danger-subtle text-danger"}">
                            ${p.active ? "Activo" : "Inactivo"}
                        </span>
                    </td>

                    <!-- ACCIONES SEPARADAS -->
                    <td class="text-end actions-col ps-5">
                        <button class="btn btn-sm btn-outline-secondary rounded-circle me-1"
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
   GROUP FILTER
================================ */

document
    .getElementById("groupFilter")
    ?.addEventListener("change", function () {

        const groupId = this.value;

        if (groupId === "ALL") {
            renderPersons(allPersons);
            return;
        }

        const filtered = allPersons.filter(p =>
            String(p.groupId) === groupId
        );

        renderPersons(filtered);
    });


/* ===============================
   UNAVAILABILITIES STATE
================================ */

const unavailabilityStart = document.getElementById("unavailabilityStart");
const unavailabilityEnd = document.getElementById("unavailabilityEnd");
const unavailabilityReason = document.getElementById("unavailabilityReason");
const addUnavailabilityBtn = document.getElementById("addUnavailabilityBtn");
const unavailabilitiesList = document.getElementById("unavailabilitiesList");

let currentUnavailabilities = [];

function renderUnavailabilities() {
    unavailabilitiesList.innerHTML = "";
    currentUnavailabilities.forEach((u, i) => {
        const li = document.createElement("li");
        li.className = "list-group-item d-flex justify-content-between align-items-center rounded-3 mb-1";
        li.innerHTML = `
            <div>
                <strong>${u.startDate}</strong> - <strong>${u.endDate || "-"}</strong> : ${u.reason}
            </div>
            <button type="button" class="btn btn-sm btn-outline-danger rounded-circle" onclick="removeUnavailability(${i})">
                <i class="bi bi-trash"></i>
            </button>
        `;
        unavailabilitiesList.appendChild(li);
    });
}

addUnavailabilityBtn.addEventListener("click", () => {
    const start = unavailabilityStart.value;
    const end = unavailabilityEnd.value || null;
    const reason = unavailabilityReason.value.trim();

    if (!start || !reason) {
        showAlert("Fecha de inicio y motivo son obligatorios", "danger", true);
        return;
    }

    currentUnavailabilities.push({ startDate: start, endDate: end, reason });
    renderUnavailabilities();

    unavailabilityStart.value = "";
    unavailabilityEnd.value = "";
    unavailabilityReason.value = "";
});

function removeUnavailability(index) {
    currentUnavailabilities.splice(index, 1);
    renderUnavailabilities();
}


/* ===============================
   CREATE / UPDATE
================================ */

form.addEventListener("submit", async e => {
    e.preventDefault();
    setLoadingButton(saveBtn, true);

    const payload = {
        fullName: $("#personName").val().trim(),
        nickName: personNickname.value.trim() || null,
        birthDate: personBirthDate.value,
        active: personStatus.checked,
        email: personEmail.value.trim(),
        emailNotificationsEnabled: mailStatus.checked,
        entryDate: personEntryDate.value,
        exitDate: personExitDate.value || null,
        groupId: $('#personGroup').val() || null,
        workingDays: $('#personDays').val(),
        functionIds: $("#personFunctions").val().map(Number),
        unavailabilities: currentUnavailabilities
    };

    const url = editingPersonId
        ? `/api/persons/${editingPersonId}`
        : "/api/persons";

    const method = editingPersonId ? "PUT" : "POST";

    const res = await secureFetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        setLoadingButton(saveBtn, false);
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
    `, "danger", true);
        return;
    }

    let personId = editingPersonId;

    if (!editingPersonId) {
        const result = await res.json();
        personId = result.personId;

        pendingAlert = {
            message: `
        <i class="bi bi-check2-circle text-success fs-5"></i>
        <span class="fw-bold">Usuario ${payload.fullName} creado correctamente</span><br>`,
            type: "success"
        };
    }

    // ===============================
    // PROFILE IMAGE (INPUT o EXTERNAL)
    // ===============================

    const fileFromInput = photoInput.files[0];

    let finalFile = fileFromInput;

    // si no hay archivo manual, usar imagen externa
    if (!finalFile && selectedExternalPerson?.photo) {
        finalFile = base64ToFile(selectedExternalPerson.photo);
    }

    if (finalFile) {
        await uploadProfileImage(personId, finalFile);
    }

    setLoadingButton(saveBtn, false);

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
    document.getElementById("personGroup").value = p.groupId || "";

    currentUnavailabilities = p.unavailabilities ? [...p.unavailabilities] : [];
    renderUnavailabilities();

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

function openDeletePersonModal(id, name) {
    personIdToDelete = id;
    personNameToDelete = name;

    document.getElementById("deletePersonName").textContent = name;

    const modal = new bootstrap.Modal(
        document.getElementById("deletePersonModal")
    );

    modal.show();
}

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
                Persona eliminada
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
    currentUnavailabilities = [];

    renderUnavailabilities();

    modalTitle.innerHTML = `<i class="bi bi-person-plus me-2"></i>Agregar persona`;

    // reset form HTML nativo
    form.reset();

    // ==========================
    // INPUTS normales (seguro)
    // ==========================
    $("#personNickname").val("");
    $("#personBirthDate").val("");
    $("#personEmail").val("");
    $("#personEntryDate").val(getToday());
    $("#personExitDate").val("");

    $("#personStatus").prop("checked", false);
    $("#mailStatus").prop("checked", false);

    document.getElementById("personGroup").value = "";

    // ==========================
    // SELECT2 múltiple
    // ==========================
    $("#personFunctions").val(null).trigger("change");
    $("#personDays").val(null).trigger("change");

    // ==========================
    // SELECT2 principal (nombre)
    // ==========================
    $("#personName")
        .val(null)
        .trigger("change");

    // ==========================
    // FOTO
    // ==========================
    photoPreview.src = "/person-circle.svg";

    if (photoInput) {
        photoInput.value = "";
    }
    selectedExternalPerson = null;

    // ==========================
    // unavailabilities inputs
    // ==========================
    unavailabilityStart.value = "";
    unavailabilityEnd.value = "";
    unavailabilityReason.value = "";
}


/* ===============================
   SELECT2
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

    // ===============================
    // SELECT2 - AUTOCOMPLETE EXTERNAL PERSON
    // ===============================
    $("#personName").select2({
        placeholder: "Buscar persona...",
        width: "100%",
        minimumInputLength: 3,
        allowClear: true,
        dropdownParent: $('#personModal'),

        ajax: {
            delay: 500,

            transport: async function (params, success, failure) {
                try {
                    const results = await searchExternalPersons(params.data.term);

                    success({
                        results: results.map((p) => ({
                            id: p.fullName,
                            text: p.fullName,
                            data: p
                        }))
                    });

                } catch (err) {
                    failure(err);
                }
            }
        },

        templateResult: function (item) {
            if (!item.id) return item.text;

            const p = item.data;

            const imgSrc = p.photo
                ? `data:image/jpeg;base64,${p.photo}`
                : "/person-circle.svg";

            return $(`
            <div style="display:flex;gap:10px;align-items:center;">
                <img src="${imgSrc}" style="width:32px;height:32px;border-radius:50%;">
                <div>
                    <div style="font-weight:600;">${p.fullName}</div>
                    <small>${p.email || "-"}</small>
                </div>
            </div>
        `);
        },

        templateSelection: item => item.text
    });


    // IMPORTANTE: evitar duplicar eventos
    $("#personName").off("select2:select");

    $("#personName").on("select2:select", function (e) {
        const data = e.params.data.data;
        selectedExternalPerson = data;
        useExternalPersonFromSelect(data);
    });
});

modalEl.addEventListener("hidden.bs.modal", () => {
    resetForm();
    if (pendingAlert) {
        showAlert(pendingAlert.message, pendingAlert.type);
        pendingAlert = null;
    }
});


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


async function loadGroups() {

    const res = await fetch("/api/groups");

    const groups = await res.json();

    const select = document.getElementById("personGroup");
    const filter = document.getElementById("groupFilter");

    select.innerHTML = `<option value="">Sin grupo</option>`;

    if (filter) {
        filter.innerHTML = `<option value="ALL">Todos los grupos</option>`;
    }

    groups.forEach(g => {

        const option = document.createElement("option");
        option.value = g.id;
        option.textContent = g.name;
        select.appendChild(option);

        if (filter) {
            const filterOption = document.createElement("option");
            filterOption.value = g.id;
            filterOption.textContent = g.name;
            filter.appendChild(filterOption);
        }

    });

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


function base64ToFile(base64, filename = "external.jpg") {
    const arr = base64.split(",");
    const mime = "image/jpeg";
    const bstr = atob(arr.length > 1 ? arr[1] : arr[0]);

    let n = bstr.length;
    const u8arr = new Uint8Array(n);

    while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
    }

    return new File([u8arr], filename, { type: mime });
}

function selectWeekdays() {
    const weekdays = [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY"
    ];

    $("#personDays").val(weekdays).trigger("change");
}

function selectAllDays() {
    const allDays = [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
        "SUNDAY"
    ];

    $("#personDays").val(allDays).trigger("change");
}

function clearWorkingDays() {
    $("#personDays").val(null).trigger("change");
}


/* ===============================
   INIT
================================ */

loadFunctions();
loadPersons();
loadGroups();