
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




