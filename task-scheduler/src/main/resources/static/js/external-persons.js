function searchExternalPersons(name) {
    if (!name || name.length < 2) return [];

    return fetch(`/api/external-persons/search?name=${name}`)
        .then(res => {
            if (!res.ok) throw new Error("error");
            return res.json();
        });
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

    $("#personBirthDate").val(p.birthDate.replaceAll("/", "-") || "");

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