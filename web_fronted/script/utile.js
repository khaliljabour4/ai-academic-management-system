// Local dev (Live Server on :5500): send /api/... calls to the backend on :8080.
// Behind nginx (Docker/server) the same relative URLs are proxied, so nothing to change.
if (["5500", "5501"].includes(window.location.port)) {
    const originalFetch = window.fetch.bind(window);
    window.fetch = (url, options) =>
        originalFetch(typeof url === "string" && url.startsWith("/api/") ? "http://localhost:8080" + url : url, options);
}


export function permitAccess(accessPageRole) {
    const token = localStorage.getItem('jwt');
    const role = localStorage.getItem('role');

    if (token == null) {
        window.alert("ur not logged yet");
        window.location.href = "login.html";
        return;
    }

    if (role !== accessPageRole) {
        alert("You don't have access to this page.");
        window.location.href = "unauthorized.html";
        return;
    }
}

export function logout() {
    localStorage.removeItem("jwt");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}

export function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}
