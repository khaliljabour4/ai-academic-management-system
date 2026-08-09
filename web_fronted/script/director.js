import { permitAccess, logout } from "./utile.js";

permitAccess("Director");

/* ---------- Sidebar navigation (tab switching) ---------- */

const navButtons = document.querySelectorAll(".app-nav-btn");
const sections = document.querySelectorAll(".app-section");
const pageTitle = document.getElementById("pageTitle");

navButtons.forEach(btn => {
    btn.addEventListener("click", () => {
        navButtons.forEach(b => b.classList.remove("active"));
        sections.forEach(s => s.classList.remove("active"));
        btn.classList.add("active");
        document.getElementById(btn.dataset.target).classList.add("active");
        pageTitle.textContent = btn.textContent.trim();

        // TODO: load data for the newly-active section here, e.g.:
        // if (btn.dataset.target === "modules") loadModules();
    });
});

/* ---------- Burger / sidebar collapse ---------- */

const sidebar = document.getElementById("sidebar");
document.getElementById("burgerMenuBtn").addEventListener("click", () => {
    sidebar.classList.toggle("collapsed");
});

/* ---------- Logout ---------- */

document.getElementById("logoutBtn").addEventListener("click", logout);

const API = "http://localhost:8080/api/v1";

function authHeaders(){
    return { "Authorization": localStorage.getItem("jwt") };
}

async function loadStats(){
    const response = await fetch(`${API}/director/stats`, { headers: authHeaders() });
    if(!response.ok) return;
    const stats = await response.json();

    document.getElementById("statFilieres").textContent = stats.filieresCount;
    document.getElementById("statProfessors").textContent = stats.professorsCount;
    document.getElementById("statStudents").textContent = stats.studentsCount;
    document.getElementById("statAverage").textContent = stats.averageGrade != null ? stats.averageGrade.toFixed(2) : "—";
}

async function loadFilieres(){
    const response = await fetch(`${API}/admin/filiere`, { headers: authHeaders() });
    if(!response.ok) return [];
    return response.json();
}

async function fillFiliereSelect(){
    const filieres = await loadFilieres();
    const select = document.getElementById("filiereOverviewSelect");

    let optionsHtml = "";
    for(const f of filieres){
        optionsHtml += `<option value="${f.id}">${f.name}</option>`;
    }
    select.innerHTML = optionsHtml;

    if(filieres.length > 0){
        await loadFiliereOverview(filieres[0].id);
    }
}

async function loadFiliereOverview(filiereId){
    const response = await fetch(`${API}/director/filiere/${filiereId}`, { headers: authHeaders() });
    if(!response.ok) return;
    const data = await response.json();

    const studentsBody = document.getElementById("filiereStudentsTableBody");
    let studentsHtml = "";
    for(const s of data.students){
        studentsHtml += `<tr>
                    <td>${s.name}</td>
                    <td>${s.prenom}</td>
                    <td>${s.cne ?? "—"}</td>
                    <td>${s.moyenneGenerale != null ? s.moyenneGenerale.toFixed(2) : "—"}</td>
                </tr>`;
    }
    studentsBody.innerHTML = studentsHtml || `<tr><td colspan="4">Aucun étudiant</td></tr>`;

    const modulesBody = document.getElementById("filiereModulesTableBody");
    let modulesHtml = "";
    for(const m of data.modules){
        modulesHtml += `<tr>
                    <td>${m.name}</td>
                    <td>${m.profName}</td>
                    <td>${m.moyenneClasse != null ? m.moyenneClasse.toFixed(2) : "—"}</td>
                </tr>`;
    }
    modulesBody.innerHTML = modulesHtml || `<tr><td colspan="3">Aucun module</td></tr>`;
}

document.getElementById("filiereOverviewSelect").addEventListener("change", (event) => {
    loadFiliereOverview(event.target.value);
});

async function fillMlFiliereSelect(){
    const filieres = await loadFilieres();
    const select = document.getElementById("mlFiliereSelect");

    let optionsHtml = "";
    for(const f of filieres){
        optionsHtml += `<option value="${f.id}">${f.name}</option>`;
    }
    select.innerHTML = optionsHtml;

    if(filieres.length > 0){
        await loadRiskStudents(filieres[0].id);
    }
}

async function loadRiskStudents(filiereId){
    const response = await fetch(`${API}/director/ml/${filiereId}`, { headers: authHeaders() });
    if(!response.ok) return;
    const students = await response.json();

    const riskList = document.getElementById("riskStudentsList");
    const atRisk = students.filter(s => s.atRisk);

    let riskHtml = "";
    for(const s of atRisk){
        riskHtml += `<div class="risk-row">
                    <span>${s.name} ${s.prenom}</span>
                    <span>${s.successRate}% de réussite estimée</span>
                </div>`;
    }
    riskList.innerHTML = riskHtml || `<div class="risk-row"><span>Aucun étudiant à risque détecté</span></div>`;

    const buckets = [0, 0, 0, 0, 0, 0];
    for(const s of students){
        if(s.successRate == null) continue;
        const index = Math.min(5, Math.floor(s.successRate / (100 / 6)));
        buckets[index]++;
    }
    const maxCount = Math.max(1, ...buckets);
    const bars = document.querySelectorAll("#mlChartPlaceholder .ml-chart-bar");
    bars.forEach((bar, i) => {
        bar.style.height = `${(buckets[i] / maxCount) * 100}%`;
    });
}

document.getElementById("mlFiliereSelect").addEventListener("change", (event) => {
    loadRiskStudents(event.target.value);
});

loadStats();
fillFiliereSelect();
fillMlFiliereSelect();
