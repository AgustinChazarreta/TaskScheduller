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
        .filter(d => days.includes(d))
        .map(d => `<span class="badge bg-warning text-dark me-1">${labels[d]}</span>`)
        .join("");
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
                <td colspan="7" class="text-center text-muted">
                    Sin personas
                </td>
            </tr>`;
        return;
    }

    data
        .sort((a, b) => a.fullName.localeCompare(b.fullName))
        .forEach(p => {
            personsCache[p.id] = p;

            tbody.insertAdjacentHTML("beforeend", `
                <tr>
                    <td class="text-center">
                        <i class="bi bi-person-fill"></i>
                    </td>
                    <td>${p.fullName}${p.nickName ? ` (${p.nickName})` : ""}</td>
                    <td>${p.birthDate}</td>
                    <td>${p.email}</td>
                    <td class="text-center">
                        <span class="badge ${p.active ? "bg-success" : "bg-secondary"}">
                            ${p.active ? "Activo" : "Inactivo"}
                        </span>
                    </td>
                    <td class = "text-center">${formatDays(p.workingDays)}</td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-primary me-1"
                                onclick="editPerson('${p.id}')">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger"
                                onclick="deletePerson('${p.id}', '${p.fullName}')">
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
        workingDays: $('#personDays').val()
    };


    const url = editingPersonId
        ? `/api/persons/${editingPersonId}`
        : "/api/persons";

    const method = editingPersonId ? "PUT" : "POST";

    console.log("payload object:", payload);
    console.log("payload json:", JSON.stringify(payload, null, 2));


    await secureFetch(url, {
        method,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

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

    $("#personStates").val(p.states).trigger("change");
    $("#personDays").val(p.workingDays).trigger("change");

    if (p.photoUrl) {
        photoPreview.src = p.photoUrl;
    }

    new bootstrap.Modal(modalEl).show();
}


/* ===============================
   DELETE
================================ */

async function deletePerson(id, name) {
    if (!confirm(`¿Eliminar a ${name}?`)) return;

    const res = await secureFetch(`/api/persons/${id}`, {
        method: "DELETE"
    });

    if (res.ok) loadPersons();
}


/* ===============================
   RESET FORM
================================ */

function resetForm() {
    editingPersonId = null;

    modalTitle.innerHTML = `
        <i class="bi bi-person-plus me-2"></i>Agregar persona
    `;

    form.reset();

    $("#personStates").val(null).trigger("change");
    $("#personDays").val(null).trigger("change");

    photoPreview.src = "/user8-128x128.jpg";
    photoInput.value = "";
}


/* ===============================
   SELECT2 (MODAL SAFE)
================================ */

modalEl.addEventListener("shown.bs.modal", () => {
    [
        { id: "#personStates", placeholder: "Seleccioná funciones" },
        { id: "#personDays", placeholder: "Seleccioná días de trabajo" }
    ].forEach(cfg => {
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
        alert("Solo se permiten imágenes");
        this.value = "";
        return;
    }

    if (file.size > 2 * 1024 * 1024) {
        alert("La imagen no puede superar los 2MB");
        this.value = "";
        return;
    }

    const reader = new FileReader();
    reader.onload = e => {
        photoPreview.src = e.target.result;
    };
    reader.readAsDataURL(file);
});


/* ===============================
   INIT
================================ */

loadPersons();
