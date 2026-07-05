let externalSearchTimeout = null;

function searchExternalPersons(name) {
    return new Promise((resolve) => {

        clearTimeout(externalSearchTimeout);

        externalSearchTimeout = setTimeout(() => {

            if (!name || name.trim().length < 3) {
                resolve([]);
                return;
            }

            fetch(`/api/external-persons/search?name=${encodeURIComponent(name)}`)
                .then(res => {
                    if (!res.ok) return [];
                    return res.json();
                })
                .then(data => resolve(data))
                .catch(() => resolve([]));

        }, 400);

    });
}

function toInputDateFormat(dateStr) {
    if (!dateStr) return "";

    if (dateStr.includes("-") && dateStr.length === 10) {
        return dateStr;
    }

    const [day, month, year] = dateStr.split("-");
    if (!day || !month || !year) return "";

    return `${year}-${month}-${day}`;
}

function useExternalPersonFromSelect(p) {

    selectedExternalPerson = p;

    // =========================
    // SELECT2 PERSON NAME
    // =========================
    $("#personName")
        .val(p.fullName)
        .trigger("change");

    // =========================
    // CAMPOS (coherente con submit jQuery)
    // =========================
    $("#personEmail").val(p.email || "");
    $("#personNickname").val(p.nickName || "");

    $("#personBirthDate").val(toInputDateFormat(p.birthDate));

    // =========================
    // FOTO PREVIEW
    // =========================
    const img = p.photo
        ? `data:image/jpeg;base64,${p.photo}`
        : "/person-circle.svg";

    $("#personPhotoPreview").attr("src", img);

    // =========================
    // FLAGS DEFAULT
    // =========================
    $("#personStatus").prop("checked", true);
    $("#mailStatus").prop("checked", true);

    // =========================
    // UX FEEDBACK
    // =========================
    showAlert(`
        <i class="bi bi-person-check text-success me-2"></i>
        Datos cargados desde sistema externo
    `, "success", true);
}