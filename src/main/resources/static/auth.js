const alertBox = document.querySelector("#form-alert");

function showAlert(message, isSuccess = false) {
    alertBox.textContent = message;
    alertBox.hidden = false;
    alertBox.classList.toggle("success", isSuccess);
}

async function readCsrf() {
    const response = await fetch("/api/auth/csrf", { headers: { Accept: "application/json" } });
    if (!response.ok) throw new Error("Could not start a secure session. Refresh and try again.");
    return response.json();
}

async function initializeSignIn() {
    const params = new URLSearchParams(window.location.search);
    if (params.has("error")) showAlert("Email or password was not recognized.");
    if (params.has("logout")) showAlert("You have signed out.", true);
    if (params.has("registered")) showAlert("Your account is ready. Sign in to continue.", true);
    const csrf = await readCsrf();
    document.querySelector("#csrf-token").value = csrf.token;
}

async function initializeSignUp() {
    const form = document.querySelector("#signup-form");
    form.addEventListener("submit", async event => {
        event.preventDefault();
        const button = form.querySelector("button[type='submit']");
        button.disabled = true;
        alertBox.hidden = true;
        try {
            const csrf = await readCsrf();
            const values = new FormData(form);
            const response = await fetch("/api/auth/signup", {
                method: "POST",
                headers: { "Content-Type": "application/json", [csrf.headerName]: csrf.token },
                body: JSON.stringify({
                    name: values.get("name"),
                    email: values.get("email"),
                    phone: values.get("phone"),
                    password: values.get("password"),
                    role: values.get("role")
                })
            });
            const data = await response.json();
            if (!response.ok) throw new Error(data.error || "Could not create account.");
            showAlert("Account created. Taking you to sign in...", true);
            window.setTimeout(() => { window.location.href = "/signin?registered"; }, 800);
        } catch (error) {
            showAlert(error.message || "Could not create account. Please try again.");
        } finally {
            button.disabled = false;
        }
    });
}

if (document.body.dataset.page === "signin") initializeSignIn().catch(error => showAlert(error.message));
if (document.body.dataset.page === "signup") initializeSignUp();