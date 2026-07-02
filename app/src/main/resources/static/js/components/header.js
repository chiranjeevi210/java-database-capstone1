/**
 * Global application routing event loop hooks
 * Reusable layout engine handling role-specific navigation menus
 */

function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) return;

    // Route guard checking: Clear roles on application home route context
    if (window.location.pathname.endsWith("/") || window.location.pathname.endsWith("/index.html")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
    }

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    // Boundary Protection: Trap invalid active session context states
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    // Baseline structural brand scaffolding
    let headerContent = `
        <div class="header-brand">
            <a href="/" class="logo">🏥 Clinic Core</a>
        </div>
        <nav class="nav-links">
    `;

    // Dynamic Context Switching Evaluator Block
    if (role === "admin") {
        headerContent += `
            <button id="addDocBtn" class="adminBtn">Add Doctor</button>
            <a href="#" id="logoutBtn">Logout</a>
        `;
    } else if (role === "doctor") {
        headerContent += `
            <a href="/templates/doctor/doctorDashboard.html" class="nav-item">Home</a>
            <a href="#" id="logoutBtn">Logout</a>
        `;
    } else if (role === "patient") {
        headerContent += `
            <button id="loginBtn" class="dashboard-btn-sm">Login</button>
            <button id="signupBtn" class="dashboard-btn-sm">Sign Up</button>
        `;
    } else if (role === "loggedPatient") {
        headerContent += `
            <a href="/templates/patient/patientDashboard.html" class="nav-item">Home</a>
            <a href="#" id="appointmentsLink" class="nav-item">Appointments</a>
            <a href="#" id="logoutPatientBtn">Logout</a>
        `;
    } else {
        // Universal default fallback menu for unassigned sessions
        headerContent += `<a href="/" class="nav-item">Portal Home</a>`;
    }

    headerContent += `</nav>`;
    headerDiv.innerHTML = headerContent;

    // Attachment lifecycle phase for component events
    attachHeaderButtonListeners(role);
}

function attachHeaderButtonListeners(role) {
    if (role === "admin") {
        const addDocBtn = document.getElementById("addDocBtn");
        if (addDocBtn) {
            addDocBtn.addEventListener("click", () => {
                if (typeof window.openModal === "function") {
                    window.openModal("addDoctor");
                } else {
                    console.warn("openModal function handler context missing.");
                }
            });
        }
        const logoutBtn = document.getElementById("logoutBtn");
        if (logoutBtn) logoutBtn.addEventListener("click", (e) => { e.preventDefault(); logout(); });
    }

    if (role === "doctor") {
        const logoutBtn = document.getElementById("logoutBtn");
        if (logoutBtn) logoutBtn.addEventListener("click", (e) => { e.preventDefault(); logout(); });
    }

    if (role === "patient") {
        const loginBtn = document.getElementById("loginBtn");
        if (loginBtn) loginBtn.addEventListener("click", () => { if (window.openModal) window.openModal("login"); });

        const signupBtn = document.getElementById("signupBtn");
        if (signupBtn) signupBtn.addEventListener("click", () => { if (window.openModal) window.openModal("signup"); });
    }

    if (role === "loggedPatient") {
        const logoutPatientBtn = document.getElementById("logoutPatientBtn");
        if (logoutPatientBtn) logoutPatientBtn.addEventListener("click", (e) => { e.preventDefault(); logoutPatient(); });
    }
}

// Session Destruction Strategies
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
}

function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/";
}

// Run loop lifecycle initialiser entry point
document.addEventListener("DOMContentLoaded", renderHeader);
