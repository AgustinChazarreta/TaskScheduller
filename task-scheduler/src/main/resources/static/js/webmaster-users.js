document.addEventListener("DOMContentLoaded", function () {

    const tableBody = document.getElementById("usersTable");

    const usersCache = {};

    // ================= CARGAR USUARIOS =================
    async function loadUsers() {
        tableBody.innerHTML = "";
        const res = await fetch("/api/webmaster/users");
        const users = await res.json();

        if (users.length === 0) {
            tableBody.innerHTML = `
            <tr>
            <td colspan="6" class="text-center py-5 text-muted">
            No hay usuarios registrados.
            </td>
            </tr>`;
            return;
        }

        console.log(users)


        users.forEach(user => {
            usersCache[user.id] = user;
            const adminData = user.role === "ADMIN" ? user.adminData : null;

            const row = document.createElement("tr");
            row.setAttribute("data-role", user.role);

            // Nombre: si es USER, usamos fullName o username; si es ADMIN, usamos adminData nombre
            const name = user.role === "USER"
                ? (user.fullName ?? user.username)
                : (adminData?.nombre ?? user.username);

            const roleBadge =
                user.role === "ADMIN"
                    ? `<span class="badge bg-primary-subtle text-primary">Admin</span>`
                    : `<span class="badge bg-warning-subtle text-warning">User</span>`;

            const isActive = user.active;
            const activeBadge = isActive
                ? `<span class="badge bg-success-subtle text-success">Activo</span>`
                : `<span class="badge bg-danger-subtle text-danger">Inactivo</span>`;

            const house = user.houseName ? user.houseName : "-";

            const date = new Date(user.createdAt);
            const created = date.toLocaleDateString("es-AR", { day: "2-digit", month: "long", year: "numeric" });
            const time = date.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });

            // Mostramos Orden solo si es Admin
            const orden = adminData ? `<span class="badge bg-secondary-subtle text-dark">${formatOrden(adminData.orden)}</span>` : "-";

            row.innerHTML = `
<td class="ps-4 fw-semibold">
    ${name}
    ${user.nickName ? `<br><small class="text-muted">${user.nickName}</small>` : ""}
</td>
<td>${roleBadge}</td>
<td>${activeBadge}</td>
<td><span class="badge bg-info-subtle text-dark">${house}</span></td>
<td>${orden}</td>
<td><div class="fw-semibold">${created}</div><small class="text-muted">${time} hs</small></td>
<td class="text-end pe-4">
<button class="btn btn-sm btn-outline-secondary rounded-circle me-2"
onclick="openEditModal(${user.id}, '${user.role}', '${name}', '${user.username}', ${isActive}, ${user.houseId ?? null})">
<i class="bi bi-pencil"></i>
</button>
<button class="btn btn-sm btn-outline-danger rounded-circle"
onclick="openDeleteModal(${user.id}, '${name}')">
<i class="bi bi-trash"></i>
</button>
</td>`;

            tableBody.appendChild(row);
        });
    }

    loadUsers();

    // ================= FILTRO POR ROL =================
    document.getElementById("roleFilter").addEventListener("change", function () {
        const role = this.value;
        document.querySelectorAll("#usersTable tr").forEach(row => {
            const r = row.getAttribute("data-role");
            if (!r) return;
            row.style.display = role === "ALL" || r === role ? "" : "none";
        });
    });

    // ================= MODALES =================
    const roleChoiceModal = new bootstrap.Modal(document.getElementById('roleChoiceModal'));
    const createUserModal = new bootstrap.Modal(document.getElementById('createUserModal'));
    const createAdminModal = new bootstrap.Modal(document.getElementById('createAdminModal'));

    document.getElementById('btnOpenRoleChoice').addEventListener('click', () => roleChoiceModal.show());

    document.getElementById('chooseAdmin').addEventListener('click', async () => {
        roleChoiceModal.hide();
        await loadHousesForCreate();
        createAdminModal.show();
    });

    document.getElementById('chooseUser').addEventListener('click', async () => {
        roleChoiceModal.hide();
        await loadHousesForCreateUser();
        createUserModal.show();
    });

    // ================= ELIMINAR USUARIO =================
    let userIdToDelete = null;
    let userNameToDelete = "";

    window.openDeleteModal = function (id, name) {
        userIdToDelete = id;
        userNameToDelete = name;
        document.getElementById("deleteUserMessage").innerHTML =
            `¿Seguro que desea eliminar <strong>${name}</strong>?`;
        new bootstrap.Modal(document.getElementById("deleteUserModal")).show();
    }

    document.getElementById("confirmDeleteUserBtn").addEventListener("click", async () => {
        if (!userIdToDelete) return;
        try {
            const res = await fetch(`/api/webmaster/users/${userIdToDelete}`, { method: "DELETE" });
            if (!res.ok) throw new Error("No se puede eliminar el último Admin");

            bootstrap.Modal.getInstance(document.getElementById("deleteUserModal")).hide();
            loadUsers();
            showAlert(`Usuario <strong>${userNameToDelete}</strong> eliminado correctamente`, "success");
        } catch (err) {
            showAlert(err.message, "danger");
        }
    });

    // ================= EDITAR USUARIO/ADMIN =================
    window.openEditModal = async function (id, role, name, username, active, houseId) {
        if (role === "ADMIN") {
            document.getElementById("editAdminId").value = id;
            document.getElementById("editAdminUsername").value = username;
            document.getElementById("editAdminActive").value = active;
            await loadHouses(houseId);
            new bootstrap.Modal(document.getElementById("editAdminModal")).show();
        } else {
            const user = usersCache[id];

            if (!user) {
                showAlert("No se pudo cargar el usuario", "danger");
                return;
            }

            // ================= CAMPOS BÁSICOS =================
            document.getElementById("editUserId").value = user.id;
            document.getElementById("editUserFullName").value = user.fullName || "";
            document.getElementById("editUserNickname").value = user.nickName || "";
            document.getElementById("editUserBirthDate").value = user.birthDate || "";
            document.getElementById("editUserEmail").value = user.email || "";
            document.getElementById("editUserMailStatus").checked = user.emailNotificationsEnabled;
            document.getElementById("editUserStatus").checked = active;
            document.getElementById("editUserEntryDate").value = user.entryDate || "";
            document.getElementById("editUserExitDate").value = user.exitDate || "";
            document.getElementById("editUserPhotoPreview").src = user.profileImageUrl || "/person-circle.svg";

            // ================= HOUSES =================
            await loadHousesForEdit(user.houseId);

            // ================= GROUPS =================
            if (user.houseId) await loadGroupsForHouseEdit(user.houseId, user.groupId);

            // ================= FUNCTIONS =================
            if (user.houseId) await loadFunctionsForHouseEdit(user.houseId, user.functionIds || []);

            // ================= WORKING DAYS =================
            if (user.workingDays) $('#editUserDays').val(user.workingDays).trigger('change');

            // ================= UNAVAILABILITIES =================
            const list = document.getElementById("editUserUnavailabilitiesList");
            list.innerHTML = "";
            if (user.unavailabilities && user.unavailabilities.length > 0) {
                document.getElementById("noEditUnavailabilitiesMsg").style.display = "none";
                user.unavailabilities.forEach(u => {
                    const div = document.createElement("div");
                    div.className = "border rounded-3 p-3 bg-white shadow-sm mb-2 d-flex justify-content-between align-items-start";
                    div.dataset.start = u.startDate;
                    div.dataset.end = u.endDate;
                    div.dataset.reason = u.reason || "";

                    div.innerHTML = `
                    <div class="d-flex flex-column">
                        <div class="d-flex align-items-center fw-semibold mb-1">
                            <i class="bi bi-calendar-event me-2 text-warning"></i>
                            <span>${u.startDate}</span>
                            <i class="bi bi-arrow-right-circle mx-2"></i>
                            <span>${u.endDate}</span>
                        </div>
                        ${u.reason ? `<div class="small text-muted d-flex align-items-center">
                            <i class="bi bi-chat-left-text me-1"></i>
                            <span>${u.reason}</span>
                        </div>` : ""}
                    </div>
                    <button type="button" class="btn btn-sm btn-outline-danger rounded-circle ms-3">
                        <i class="bi bi-x-lg"></i>
                    </button>
                `;
                    div.querySelector("button").addEventListener("click", () => {
                        div.remove();
                        if (list.children.length === 0) {
                            document.getElementById("noEditUnavailabilitiesMsg").style.display = "block";
                        }
                    });

                    list.appendChild(div);
                });
            } else {
                document.getElementById("noEditUnavailabilitiesMsg").style.display = "block";
            }

            // ================= ABRIR MODAL =================
            new bootstrap.Modal(document.getElementById("editUserModal")).show();
        }
    };

    // ================= SUBMIT EDIT USER =================
    document.getElementById("editUserForm").addEventListener("submit", async e => {
        e.preventDefault();

        const id = document.getElementById("editUserId").value;
        const fullName = document.getElementById("editUserFullName").value;
        const nickName = document.getElementById("editUserNickname").value;
        const birthDate = document.getElementById("editUserBirthDate").value;
        const email = document.getElementById("editUserEmail").value;
        const emailNotificationsEnabled = document.getElementById("editUserMailStatus").checked;
        const active = document.getElementById("editUserStatus").checked;
        const entryDate = document.getElementById("editUserEntryDate").value;
        const exitDate = document.getElementById("editUserExitDate").value || null;
        const houseId = Number(document.getElementById("editUserHouse").value);
        const groupId = document.getElementById("editUserGroup").value || null;
        const functionIds = ($('#editUserFunctions').val() || []).map(id => Number(id));
        const workingDays = $('#editUserDays').val() || [];

        const unavailabilities = Array.from(
            document.querySelectorAll("#editUserUnavailabilitiesList > div")
        ).map(div => ({
            startDate: div.dataset.start,
            endDate: div.dataset.end,
            reason: div.dataset.reason || null
        }));

        const dto = {
            fullName,
            nickName,
            birthDate,
            email,
            emailNotificationsEnabled,
            active,
            entryDate,
            exitDate,
            houseId,
            groupId,
            functionIds,
            workingDays,
            unavailabilities
        };

        try {
            // ================= EDIT USER =================
            const res = await fetch(`/api/webmaster/users/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || "No se pudo editar el usuario");
            }

            // ================= OBTENER PERSON ID =================
            const data = await res.json();
            const personId = data.personId;

            // ================= SUBIR FOTO =================
            const photo = document.getElementById("editUserPhoto").files[0];
            if (photo && personId) {
                const formData = new FormData();
                formData.append("file", photo);
                const photoRes = await fetch(`/api/persons/${personId}/profile-image`, {
                    method: "POST",
                    body: formData
                });

                if (!photoRes.ok) console.warn("La foto no se pudo subir");
            }

            // ================= CERRAR MODAL Y RECARGAR =================
            bootstrap.Modal.getInstance(document.getElementById("editUserModal")).hide();
            loadUsers();
            showAlert(`Usuario <strong>${fullName}</strong> editado correctamente`, "success");

        } catch (err) {
            showAlert(err.message, "danger");
        }
    });

    // ================= FORM EDIT ADMIN =================
    document.getElementById("editAdminForm").addEventListener("submit", async e => {
        e.preventDefault();
        const id = document.getElementById("editAdminId").value;
        const username = document.getElementById("editAdminUsername").value;
        const active = document.getElementById("editAdminActive").value;
        const houseId = document.getElementById("editAdminHouse").value;

        try {
            const res = await fetch(
                `/api/webmaster/admins/${id}?username=${encodeURIComponent(username)}&active=${active}&houseId=${houseId}`,
                { method: "PUT" }
            );
            if (!res.ok) throw new Error("No se pudo editar el admin");

            bootstrap.Modal.getInstance(document.getElementById("editAdminModal")).hide();
            loadUsers();
            showAlert(`Admin <strong>${username}</strong> editado correctamente`, "success");
        } catch (err) {
            showAlert(err.message, "danger");
        }
    });

    // ================= FORM CREAR ADMIN =================
    async function loadHousesForCreate() {
        const res = await fetch("/api/webmaster/houses");
        const houses = await res.json();
        const select = document.getElementById("newAdminHouse");
        select.innerHTML = "";
        houses.forEach(h => {
            const option = document.createElement("option");
            option.value = h.id;
            option.textContent = h.name;
            select.appendChild(option);
        });
    }

    document.getElementById("createAdminForm").addEventListener("submit", async e => {
        e.preventDefault();
        const username = document.getElementById("newAdminUsername").value;
        const houseId = document.getElementById("newAdminHouse").value;

        try {
            const res = await fetch(`/api/webmaster/admins/houses/${houseId}?username=${encodeURIComponent(username)}`, {
                method: "POST"
            });
            if (!res.ok) throw new Error("No se pudo crear el admin");

            const data = await res.json();
            bootstrap.Modal.getInstance(document.getElementById("createAdminModal")).hide();
            loadUsers();

            showAlert(`
<div class="d-flex flex-column">
<div><i class="bi bi-check-circle-fill me-2"></i>Admin <strong>${data.username}</strong> creado correctamente.</div>
<div class="mt-3 ms-4">Contraseña temporal: <strong>${data.temporaryPassword}</strong>
<button class="btn btn-sm btn-outline-secondary ms-2 copyPasswordBtn" data-password="${data.temporaryPassword}">
<i class="bi bi-clipboard"></i>
</button>
</div>
</div>
`, "success", false, 5000);
        } catch (err) {
            showAlert(err.message, "danger");
        }
    });

    // ================= FORM CREAR USER =================
    async function loadHousesForCreateUser() {
        const res = await fetch("/api/webmaster/houses");
        const houses = await res.json();

        const select = document.getElementById("newUserHouse");
        select.innerHTML = "";

        houses.forEach(h => {
            const option = document.createElement("option");
            option.value = h.id;
            option.textContent = h.name;
            select.appendChild(option);
        });

        // cargar grupos y funciones de la primera casa
        if (houses.length > 0) {
            await loadGroupsForHouse(houses[0].id);
            await loadFunctionsForHouse(houses[0].id);
        }
    }

    document.getElementById("newUserHouse").addEventListener("change", async function () {
        const houseId = this.value;

        if (!houseId) return;

        await loadGroupsForHouse(houseId);
        await loadFunctionsForHouse(houseId);
    });

    document.getElementById("editUserHouse").addEventListener("change", async function () {
        const houseId = this.value;
        if (!houseId) return;
        await loadGroupsForHouseEdit(houseId);
        await loadFunctionsForHouseEdit(houseId);
    });

    async function loadGroupsForHouse(houseId) {

        const res = await fetch(`/api/webmaster/houses/${houseId}/groups`);
        const groups = await res.json();

        const select = document.getElementById("newUserGroup");

        select.innerHTML = `<option value="">Sin grupo</option>`;

        groups.forEach(g => {
            const option = document.createElement("option");
            option.value = g.id;
            option.textContent = g.name;
            select.appendChild(option);
        });

    }

    async function loadFunctionsForHouse(houseId) {

        const res = await fetch(`/api/webmaster/houses/${houseId}/functions`);
        const functions = await res.json();

        const select = document.getElementById("newUserFunctions");

        select.innerHTML = "";

        functions.forEach(f => {
            const option = document.createElement("option");
            option.value = f.id;
            option.textContent = f.name;
            select.appendChild(option);
        });

        $('#newUserFunctions').trigger('change.select2');
    }

    createUserModal.hide();

    document.getElementById("createUserForm").addEventListener("submit", async e => {
        e.preventDefault();

        // ================= DATOS BÁSICOS =================
        const fullName = document.getElementById("newUserFullName").value;
        const nickName = document.getElementById("newUserNickname").value;
        const birthDate = document.getElementById("newUserBirthDate").value;
        const email = document.getElementById("newUserEmail").value;
        const emailNotificationsEnabled = document.getElementById("newUserMailStatus").checked;
        const active = document.getElementById("newUserStatus").checked;
        const entryDate = document.getElementById("newUserEntryDate").value;
        const exitDate = document.getElementById("newUserExitDate").value || null;

        // ================= ASIGNACIONES =================
        const groupId = document.getElementById("newUserGroup").value || null;
        const functionIds = $('#newUserFunctions').val() || [];
        const houseId = Number(document.getElementById("newUserHouse").value);

        if (!houseId) {
            showAlert("Debe seleccionar una casa", "danger");
            return;
        }

        const workingDays = $('#newUserDays').val() || [];

        // ================= AUSENCIAS =================
        const unavailabilities = Array.from(
            document.querySelectorAll("#newUserUnavailabilitiesList > div")
        ).map(div => ({
            startDate: div.dataset.start,
            endDate: div.dataset.end,
            reason: div.dataset.reason || null
        }));

        console.log("Unavailabilities:", unavailabilities);

        // ================= DTO =================
        const dto = {
            fullName,
            nickName,
            birthDate,
            email,
            emailNotificationsEnabled,
            active,
            entryDate,
            exitDate,
            groupId,
            functionIds,
            workingDays,
            unavailabilities
        };

        try {

            // ================= CREAR USUARIO =================
            const res = await fetch(`/api/webmaster/users?houseId=${houseId}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || "No se pudo crear el usuario");
            }

            const data = await res.json();
            const personId = data.personId;

            // ================= SUBIR FOTO =================
            const photo = document.getElementById("newUserPhoto").files[0];

            if (photo) {

                const formData = new FormData();
                formData.append("file", photo);

                const photoRes = await fetch(`/api/persons/${personId}/profile-image`, {
                    method: "POST",
                    body: formData
                });

                if (!photoRes.ok) {
                    console.warn("La foto no se pudo subir");
                }
            }

            // ================= LIMPIAR =================
            createUserModal.hide();
            loadUsers();

            document.getElementById("createUserForm").reset();
            document.getElementById("newUserPhotoPreview").src = "/person-circle.svg";

            // ================= ALERT =================
            showAlert(`
<div class="d-flex flex-column">
<div>
<i class="bi bi-check-circle-fill me-2"></i>
Usuario <strong>${data.fullName}</strong> creado correctamente.
</div>

<div class="mt-3 ms-4">
Contraseña temporal: <strong>${data.temporaryPassword}</strong>

<button class="btn btn-sm btn-outline-secondary ms-2 copyPasswordBtn"
data-password="${data.temporaryPassword}">
<i class="bi bi-clipboard"></i>
</button>

</div>
</div>
`, "success", false, 5000);

        } catch (err) {
            showAlert(err.message, "danger");
        }
    });

    // ================= CARGAR HOUSES EDIT ADMIN =================
    async function loadHouses(houseIdActual = null) {
        const res = await fetch("/api/webmaster/houses");
        const houses = await res.json();
        const select = document.getElementById("editAdminHouse");
        select.innerHTML = "";
        houses.forEach(h => {
            const option = document.createElement("option");
            option.value = h.id;
            option.textContent = h.name;
            if (houseIdActual && h.id === houseIdActual) option.selected = true;
            select.appendChild(option);
        });
    }

    document.getElementById("addNewUserUnavailabilityBtn").addEventListener("click", () => {

        const start = document.getElementById("newUserUnavailabilityStart").value;
        const end = document.getElementById("newUserUnavailabilityEnd").value;
        const reason = document.getElementById("newUserUnavailabilityReason").value;

        if (!start || !end) {
            showAlert("Debe completar fecha inicio y fin", "danger");
            return;
        }

        const list = document.getElementById("newUserUnavailabilitiesList");

        const li = document.createElement("div");

        li.className = "border rounded-3 p-3 bg-white shadow-sm";

        li.dataset.start = start;
        li.dataset.end = end;
        li.dataset.reason = reason;

        li.innerHTML = `
    <div class="d-flex justify-content-between align-items-start">

        <!-- Fechas y motivo -->
        <div class="d-flex flex-column">

            <!-- Rango de fechas -->
            <div class="d-flex align-items-center mb-1 fw-semibold">
                <i class="bi bi-calendar-event me-2 text-warning"></i>
                <span>${start}</span>
                <i class="bi bi-arrow-right-circle mx-2"></i>
                <span>${end}</span>
            </div>

            <!-- Motivo -->
            ${reason
                ? `<div class="small text-muted d-flex align-items-center">
                        <i class="bi bi-chat-left-text me-1"></i>
                        <span>${reason}</span>
                    </div>`
                : ""
            }

        </div>      

        <!-- Botón eliminar -->
        <button type="button"
            class="btn btn-sm btn-outline-danger rounded-circle ms-3">
            <i class="bi bi-x-lg"></i>
        </button>

    </div>
    `;

        li.querySelector("button").addEventListener("click", () => {
            li.remove();

            const list = document.getElementById("newUserUnavailabilitiesList");
            if (list.children.length === 0) {
                document.getElementById("noUnavailabilitiesMsg").style.display = "block";
            }
        });

        list.appendChild(li);
        document.getElementById("noUnavailabilitiesMsg").style.display = "none";

        // limpiar campos
        document.getElementById("newUserUnavailabilityStart").value = "";
        document.getElementById("newUserUnavailabilityEnd").value = "";
        document.getElementById("newUserUnavailabilityReason").value = "";
    });

    async function loadHousesForEdit(selectedHouseId) {

        const res = await fetch("/api/webmaster/houses");
        const houses = await res.json();

        const select = document.getElementById("editUserHouse");
        select.innerHTML = "";

        houses.forEach(h => {

            const option = document.createElement("option");
            option.value = h.id;
            option.textContent = h.name;

            if (selectedHouseId && h.id === selectedHouseId)
                option.selected = true;

            select.appendChild(option);
        });

    }

    async function loadGroupsForHouseEdit(houseId, selectedGroupId = null) {

        const res = await fetch(`/api/webmaster/houses/${houseId}/groups`);
        const groups = await res.json();

        const select = document.getElementById("editUserGroup");

        select.innerHTML = `<option value="">Sin grupo</option>`;

        groups.forEach(g => {

            const option = document.createElement("option");
            option.value = g.id;
            option.textContent = g.name;

            if (selectedGroupId && g.id === selectedGroupId)
                option.selected = true;

            select.appendChild(option);

        });

    }

    async function loadFunctionsForHouseEdit(houseId, selectedFunctions = []) {

        const res = await fetch(`/api/webmaster/houses/${houseId}/functions`);
        const functions = await res.json();

        const select = document.getElementById("editUserFunctions");

        select.innerHTML = "";

        functions.forEach(f => {

            const option = document.createElement("option");
            option.value = f.id;
            option.textContent = f.name;

            select.appendChild(option);

        });

        $('#editUserFunctions').val(selectedFunctions).trigger('change');

    }

    // ================= ALERTS =================
    function showAlert(message, type = "success", showIcon = true, duration = 3000) {
        const container = document.getElementById("alertContainer");
        const alert = document.createElement("div");

        let iconHtml = '';
        if (showIcon) {
            if (type === "success") iconHtml = '<i class="bi bi-check-circle-fill me-2"></i>';
            else if (type === "danger") iconHtml = '<i class="bi bi-x-circle-fill me-2"></i>';
        }

        alert.className = `alert alert-${type} alert-dismissible fade show rounded-4 shadow`;
        alert.role = "alert";
        alert.innerHTML = `${iconHtml}${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
        container.appendChild(alert);

        setTimeout(() => {
            bootstrap.Alert.getOrCreateInstance(alert).close();
        }, duration);
    }

    // ================= COPIAR PASSWORD =================
    document.addEventListener("click", function (e) {
        if (e.target.closest(".copyPasswordBtn")) {
            const btn = e.target.closest(".copyPasswordBtn");
            const password = btn.getAttribute("data-password");
            navigator.clipboard.writeText(password);
            showAlert("Contraseña copiada al portapapeles", "success");
        }
    });

    // ===================== SELECT2 =====================
    const modalSelects = [
        {
            modalId: "#createUserModal",
            selects: [
                { id: "#newUserFunctions", placeholder: "Seleccioná funciones" },
                { id: "#newUserDays", placeholder: "Seleccioná días de trabajo" }
            ]
        },
        {
            modalId: "#editUserModal",
            selects: [
                { id: "#editUserFunctions", placeholder: "Seleccioná funciones" },
                { id: "#editUserDays", placeholder: "Seleccioná días de trabajo" }
            ]
        }
    ];

    modalSelects.forEach(cfg => {
        const modalEl = document.querySelector(cfg.modalId);
        if (!modalEl) return;

        modalEl.addEventListener("shown.bs.modal", () => {
            cfg.selects.forEach(s => {
                const $el = $(s.id);
                if ($el.hasClass("select2-hidden-accessible")) return;

                $el.select2({
                    placeholder: s.placeholder,
                    width: "100%",
                    dropdownAutoWidth: true,
                    dropdownParent: $(modalEl)
                });
            });
        });

        modalEl.addEventListener("hidden.bs.modal", () => {
            $(modalEl).find(".select2").val(null).trigger("change");
        });
    });

    // ================= PREVIEW FOTO NUEVO USER =================
    document.getElementById("newUserPhoto").addEventListener("change", function () {

        const file = this.files[0];
        if (!file) return;

        const preview = document.getElementById("newUserPhotoPreview");

        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
        };

        reader.readAsDataURL(file);
    });

    // ================= PREVIEW FOTO EDIT USER =================
    document.getElementById("editUserPhoto").addEventListener("change", function () {

        const file = this.files[0];
        if (!file) return;

        const preview = document.getElementById("editUserPhotoPreview");

        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
        };

        reader.readAsDataURL(file);
    });

    // función para formatear "ORDEN_I" → "Orden I"
    function formatOrden(orden) {
        if (!orden) return "-";
        // reemplazamos "_" por espacio
        const withSpaces = orden.replace(/_/g, " ");
        // separamos en palabras
        const words = withSpaces.split(" ");
        // primera palabra: primera letra mayúscula, resto minúscula
        words[0] = words[0].charAt(0).toUpperCase() + words[0].slice(1).toLowerCase();
        // segunda palabra (si existe), la dejamos tal cual (para I, II, III)
        if (words[1]) words[1] = words[1].toUpperCase();
        return words.join(" ");
    }


});