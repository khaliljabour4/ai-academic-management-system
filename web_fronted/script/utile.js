const API_BASE = "http://localhost:8080/api/v1";

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

/**
 * Wraps fetch() with the Authorization header, JSON parsing, and the
 * 401/403 -> redirect-to-login handling every page needs. Use this instead
 * of calling fetch() directly for anything that talks to the backend.
 */
export async function authorizedFetch(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem("jwt"),
            ...(options.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        window.location.href = "login.html";
        throw new Error("Unauthorized");
    }

    if (!response.ok) {
        throw new Error(`Request failed: ${response.status}`);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

/* ---------- Admin / Director shared data calls ---------- */
/* Some of these endpoints don't exist on the backend yet - see the
   README notes at the end of the project for what's still to build. */

export async function getAllFilliere() {
    return authorizedFetch("/admin/filiere");
}

export async function getAllProfessors() {
    return authorizedFetch("/admin/professor"); // TODO: backend endpoint not built yet
}

export async function getAllStudents() {
    return authorizedFetch("/admin/student"); // TODO: backend endpoint not built yet
}

/* ---------- Student-side data calls (placeholders) ---------- */

export async function getMySchedule(filiereId) {
    return authorizedFetch(`/student/schedule/${filiereId}`); // TODO: backend endpoint not built yet
}

export async function getMyModules() {
    return authorizedFetch("/student/modules"); // TODO: backend endpoint not built yet
}

export async function getMyConversations(moduleId) {
    return authorizedFetch(`/student/conversations/${moduleId}`); // TODO: backend endpoint not built yet
}

export async function sendAiQuestion(moduleId, conversationTitle, question) {
    return authorizedFetch(`/student/conversations/${moduleId}/ask`, {
        method: "POST",
        body: JSON.stringify({ conversationTitle, question })
    }); // TODO: backend endpoint + FastAPI/LLM wiring not built yet
}
