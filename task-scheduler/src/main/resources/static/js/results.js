let personsCache = {};
let functionsCache = {};

/* =====================================================
   INIT
===================================================== */

document.addEventListener('DOMContentLoaded', async () => {
    bindForm();
    await loadPersons();
    await loadFunctions();
    await checkScheduleStatus();
});

/* =====================================================
   FORM (GENERAR)
===================================================== */

function bindForm() {
    const form = document.getElementById('scheduleForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;

        if (!startDate || !endDate) {
            showAlert('Debe completar ambas fechas', 'warning');
            return;
        }

        const personIds = Object.keys(personsCache);
        const functionIds = Object.keys(functionsCache);

        if (personIds.length === 0 || functionIds.length === 0) {
            showAlert("Faltan personas o funciones", "warning");
            return;
        }

        try {
            showLoading();

            const response = await fetch('/api/schedule/solve', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    personIds,
                    functionIds,
                    period: { startDate, endDate }
                })
            });

            if (!response.ok) {
                const text = await response.text();
                console.error("BACKEND ERROR:", text);
                throw new Error(`Error ${response.status}: ${text}`);
            }

            showAlert('Schedule generado correctamente');
            enableExportButtons();
            goToWordEditor();

        } catch (error) {
            console.error(error);
            showAlert(error.message, 'danger');
        } finally {
            hideLoading();
        }
    });
}

/* =====================================================
   DATA
===================================================== */

async function loadPersons() {
    try {
        const res = await fetch('/api/persons');

        if (!res.ok) {
            const text = await res.text();
            console.error("BACKEND ERROR (persons):", text);
            throw new Error();
        }

        const data = await res.json();

        // 🔥 IMPORTANTE: usar el id real
        Object.values(data).forEach(p => {
            personsCache[p.id] = p;
        });

    } catch (e) {
        console.error("Error cargando personas", e);
        showAlert("No se pudieron cargar las personas", "danger");
    }
}

async function loadFunctions() {
    try {
        const res = await fetch('/api/functions');

        if (!res.ok) {
            const text = await res.text();
            console.error("BACKEND ERROR (functions):", text);
            throw new Error();
        }

        const data = await res.json();

        data.forEach(f => {
            functionsCache[f.id] = f;
        });

    } catch (e) {
        console.error("Error cargando funciones", e);
        showAlert("No se pudieron cargar las funciones", "danger");
    }
}

/* =====================================================
   UI
===================================================== */

function showLoading() {
    const btn = document.getElementById('resolveBtn');
    if (!btn) return;

    btn.disabled = true;
    document.getElementById('resolveText')?.classList.add('d-none');
    document.getElementById('resolveSpinner')?.classList.remove('d-none');
}

function hideLoading() {
    const btn = document.getElementById('resolveBtn');
    if (!btn) return;

    btn.disabled = false;
    document.getElementById('resolveSpinner')?.classList.add('d-none');
    document.getElementById('resolveText')?.classList.remove('d-none');
}

function showAlert(message, type = "success") {
    const container = document.getElementById("alertContainer");
    if (!container) return;

    const alert = document.createElement("div");
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button class="btn-close" data-bs-dismiss="alert"></button>
    `;

    container.appendChild(alert);
    setTimeout(() => alert.remove(), 5000);
}

/* =====================================================
   ACTIONS (STUBS)
===================================================== */

async function generatePDF() {
    showAlert("PDF generado (stub)", "info");
}

async function sendPdfByEmail() {
    showAlert("Mail enviado (stub)", "info");
}

function goToWordEditor() {
    window.open("/admin/schedule/word", "_blank");
}

async function checkScheduleStatus() {
    try {
        const res = await fetch('/api/admin/schedule/status');

        if (!res.ok) throw new Error();

        const { invalidated } = await res.json();

        if (invalidated) {
            disableExportButtons();
            showAlert("El schedule está desactualizado. Debe regenerarse.", "warning");
        }

    } catch (e) {
        console.error("Error verificando estado del schedule", e);
    }
}

function disableExportButtons() {
    ['generatePdfBtn', 'sendPdfEmailBtn', 'editWordBtn']
        .forEach(id => {
            const btn = document.getElementById(id);
            if (btn) {
                btn.disabled = true;
                btn.classList.add('disabled');
            }
        });
}

function enableExportButtons() {
    ['generatePdfBtn', 'sendPdfEmailBtn', 'editWordBtn']
        .forEach(id => {
            const btn = document.getElementById(id);
            if (btn) {
                btn.disabled = false;
                btn.classList.remove('disabled');
            }
        });
}