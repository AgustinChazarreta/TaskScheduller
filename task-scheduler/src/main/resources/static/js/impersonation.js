document.addEventListener("DOMContentLoaded", () => {

    const btn = document.getElementById("btnStopImpersonation");

    if (!btn) {
        return;
    }

    btn.addEventListener("click", async () => {

        try {

            const response = await secureFetch("/api/impersonation/stop", {
                method: "POST"
            });

            if (!response.ok) {
                throw new Error("No se pudo finalizar la impersonación.");
            }

            window.location.href = "/webmaster/dashboard";

        } catch (error) {

            console.error(error);
            alert("No se pudo volver al modo Webmaster.");

        }

    });

});