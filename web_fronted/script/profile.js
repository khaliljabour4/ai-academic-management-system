import { logout } from "./utile.js";

const role = localStorage.getItem("role");

if (!localStorage.getItem("jwt")) {
    window.location.href = "login.html";
}

document.getElementById("logoutBtn").addEventListener("click", logout);

const dashboardByRole = {
    Administrator: "admin.html",
    Director: "director.html",
    Professor: "professor.html",
    Student: "student.html"
};

document.getElementById("backLink").addEventListener("click", (e) => {
    e.preventDefault();
    window.location.href = dashboardByRole[role] || "login.html";
});

const extraFieldByRole = {
    Professor: { label: "Numero", key: "number" },
    Student: { label: "Filiere", key: "filiereName" },
    Administrator: { label: "Departement", key: "department" }
};

async function loadProfile() {
    const response = await fetch("http://localhost:8080/api/v1/profile", {
        headers: {
            "Authorization": localStorage.getItem("jwt")
        }
    });

    if (response.status === 401 || response.status === 403) {
        window.location.href = "login.html";
        return;
    }

    if (!response.ok) {
        window.alert("impossible de charger le profil");
        return;
    }

    const profile = await response.json();

    document.getElementById("profileName").value = profile.name ?? "";
    document.getElementById("profilePrenom").value = profile.prenom ?? "";
    document.getElementById("profileEmail").value = profile.email ?? "";

    const extra = extraFieldByRole[role];
    if (extra && profile[extra.key]) {
        document.getElementById("extraFieldWrap").style.display = "block";
        document.getElementById("extraFieldLabel").textContent = extra.label;
        document.getElementById("extraFieldValue").value = profile[extra.key];
    }
}

document.getElementById("saveInfoBtn").addEventListener("click", async () => {
    const name = document.getElementById("profileName").value;
    const prenom = document.getElementById("profilePrenom").value;

    const response = await fetch("http://localhost:8080/api/v1/profile", {
        method: "PUT",
        headers: {
            "Authorization": localStorage.getItem("jwt"),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            name: name,
            prenom: prenom
        })
    });

    if (!response.ok) {
        window.alert("la mise a jour a echoue");
        return;
    }

    window.alert("informations mises a jour");
});

document.getElementById("savePasswordBtn").addEventListener("click", async () => {
    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;

    if (!currentPassword || !newPassword) {
        window.alert("remplis les deux champs de mot de passe");
        return;
    }

    const response = await fetch("http://localhost:8080/api/v1/profile", {
        method: "PUT",
        headers: {
            "Authorization": localStorage.getItem("jwt"),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            currentPassword: currentPassword,
            newPassword: newPassword
        })
    });

    if (response.status === 409) {
        window.alert("mot de passe actuel incorrect");
        return;
    }

    if (!response.ok) {
        window.alert("le changement de mot de passe a echoue");
        return;
    }

    document.getElementById("currentPassword").value = "";
    document.getElementById("newPassword").value = "";
    window.alert("mot de passe change avec succes");
});

loadProfile();
