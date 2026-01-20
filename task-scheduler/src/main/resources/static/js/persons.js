const tbody = document.getElementById("personsBody");
const form = document.getElementById("personForm");
const modalEl = document.getElementById("personModal");
const modalTitle = document.getElementById("modalTitle");

let editingPersonId = null;
const personsCache = {};

/* ========= FORMATOS ========= */

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

function formatDays(days) {
    const order = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"];

    const labels = {
        MONDAY: "Lunes",
        TUESDAY: "Martes",
        WEDNESDAY: "Miércoles",
        THURSDAY: "Jueves",
        FRIDAY: "Viernes",
    };

    return order
        .filter((d) => days.includes(d))
        .map((d) => `<span class="badge bg-secondary me-1">${labels[d]}</span>`)
        .join("");
}

/* ========= LOAD ========= */

async function loadPersons() {
    const res = await fetch("/api/persons");
    const data = await res.json();

    tbody.innerHTML = "";
    const entries = Object.entries(data);

    if (entries.length === 0) {
        tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted">Sin personas</td>
                </tr>`;
        return;
    }

    entries
        .sort((a, b) => a[1].name.localeCompare(b[1].name))
        .forEach(([id, p]) => {
            personsCache[id] = p;

            tbody.insertAdjacentHTML(
                "beforeend",
                `
                    <tr>
                        <td>${p.name}</td>
                        <td>${formatCategory(p.category)}</td>
                        <td>${p.birthDate}</td>
                        <td>${formatDays(p.availableDays)}</td>
                        <td class="text-end">
                            <button class="btn btn-sm btn-outline-primary me-1"
                                    onclick="editPerson('${id}')">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger"
                                    onclick="deletePerson('${id}', '${p.name}')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </td>
                    </tr>
                `,
            );
        });
}

/* ========= CREATE / UPDATE ========= */

form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const days = [...document.querySelectorAll(".day:checked")].map(
        (d) => d.value,
    );

    const payload = {
        name: document.getElementById("personName").value.trim(),
        category: document.getElementById("personCategory").value,
        birthDate: document.getElementById("personBirthDate").value,
        availableDays: days,
    };

    const url = editingPersonId
        ? `/api/persons/${editingPersonId}`
        : "/api/persons";
    const method = editingPersonId ? "PUT" : "POST";

    await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
    });

    bootstrap.Modal.getInstance(modalEl).hide();
    resetForm();
    loadPersons();
});

function editPerson(id) {
    const p = personsCache[id];
    if (!p) return;

    editingPersonId = id;
    modalTitle.textContent = "Editar persona";

    document.getElementById("personName").value = p.name;
    document.getElementById("personCategory").value = p.category;
    document.getElementById("personBirthDate").value = p.birthDate;

    document.querySelectorAll(".day").forEach((cb) => {
        cb.checked = p.availableDays.includes(cb.value);
    });

    new bootstrap.Modal(modalEl).show();
}

async function deletePerson(id, name) {
    if (!confirm(`¿Eliminar a ${name}?`)) return;

    const res = await fetch(`/api/persons/${id}`, { method: "DELETE" });
    if (res.ok) loadPersons();
}

function resetForm() {
    editingPersonId = null;
    modalTitle.textContent = "Agregar persona";
    form.reset();
}

modalEl.addEventListener("hidden.bs.modal", resetForm);

loadPersons();
