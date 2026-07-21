import { permitAccess, getAllFilliere, getAllProfessors, getAllStudents, logout } from "./utile.js";

permitAccess("Director");

/* ---------- Sidebar navigation ---------- */

const navButtons = document.querySelectorAll(".app-nav-btn");
const sections = document.querySelectorAll(".app-section");
const pageTitle = document.getElementById("pageTitle");

navButtons.forEach(btn => {
    btn.addEventListener("click", () => {
        navButtons.forEach(b => b.classList.remove("active"));
        sections.forEach(s => s.classList.remove("active"));

        btn.classList.add("active");
        const target = document.getElementById(btn.dataset.target);
        target.classList.add("active");
        pageTitle.textContent = btn.textContent.trim();

        if (btn.dataset.target === "filieres") loadFilieres();
        if (btn.dataset.target === "professors") loadProfessors();
        if (btn.dataset.target === "students") loadStudents();
    });
});

/* ---------- Burger / sidebar collapse ---------- */

const sidebar = document.getElementById("sidebar");
const burgerBtn = document.getElementById("burgerMenuBtn");

burgerBtn.addEventListener("click", () => {
    sidebar.classList.toggle("collapsed");
});

/* ---------- Logout ---------- */

document.getElementById("logoutBtn").addEventListener("click", logout);

/* ---------- Data loading ---------- */

async function loadOverviewStats() {
    try {
        const filieres = await getAllFilliere();
        document.getElementById("statFilieres").textContent = filieres.length;

        const totalStudents = filieres.reduce((sum, f) => sum + (f.students?.length || 0), 0);
        document.getElementById("statStudents").textContent = totalStudents;
    } catch (err) {
        console.error("Could not load overview stats:", err);
    }

    try {
        const professors = await getAllProfessors();
        document.getElementById("statProfessors").textContent = professors.length;
    } catch (err) {
        // Endpoint not built yet on the backend - fail quietly on the dashboard
        document.getElementById("statProfessors").textContent = "—";
    }
}

async function loadFilieres() {
    const tbody = document.getElementById("filiereTableBody");
    tbody.innerHTML = `<tr><td colspan="5" class="empty-state">Chargement…</td></tr>`;

    try {
        const filieres = await getAllFilliere();

        if (filieres.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="empty-state">Aucune filière pour l'instant</td></tr>`;
            return;
        }

        tbody.innerHTML = filieres.map(f => `
            <tr>
                <td><strong>${f.name}</strong></td>
                <td>${f.description || "—"}</td>
                <td>${f.modules?.length || 0}</td>
                <td>${f.students?.length || 0}</td>
                <td><button class="icon-btn" title="Supprimer">✕</button></td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state">Erreur de chargement</td></tr>`;
        console.error(err);
    }
}

async function loadProfessors() {
    const tbody = document.getElementById("professorTableBody");
    tbody.innerHTML = `<tr><td colspan="4" class="empty-state">Chargement…</td></tr>`;

    try {
        const professors = await getAllProfessors();

        if (professors.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="empty-state">Aucun professeur pour l'instant</td></tr>`;
            return;
        }

        tbody.innerHTML = professors.map(p => `
            <tr>
                <td><strong>${p.name}</strong></td>
                <td>${p.email || "—"}</td>
                <td>${p.modules?.length || 0}</td>
                <td><button class="icon-btn" title="Supprimer">✕</button></td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="4" class="empty-state">Endpoint backend pas encore créé (/admin/professor)</td></tr>`;
    }
}

async function loadStudents() {
    const tbody = document.getElementById("studentTableBody");
    tbody.innerHTML = `<tr><td colspan="5" class="empty-state">Chargement…</td></tr>`;

    try {
        const students = await getAllStudents();

        if (students.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="empty-state">Aucun étudiant pour l'instant</td></tr>`;
            return;
        }

        tbody.innerHTML = students.map(s => `
            <tr>
                <td><strong>${s.name}</strong></td>
                <td>${s.email || "—"}</td>
                <td>${s.filiere?.name || "—"}</td>
                <td><span class="badge badge-success">Actif</span></td>
                <td><button class="icon-btn" title="Supprimer">✕</button></td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state">Endpoint backend pas encore créé (/admin/student)</td></tr>`;
    }
}

document.getElementById("refreshFilieresBtn").addEventListener("click", loadFilieres);

loadOverviewStats();
