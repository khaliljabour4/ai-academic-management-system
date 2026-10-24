import { permitAccess, logout, escapeHtml } from "./utile.js";

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

const API = "/api/v1";

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

/* ---------- Classes & étudiants ---------- */

const classState = { filieres: [], overview: [], students: [], filter: "all", query: "" };

function tier(rate){
    if(rate == null) return { key: "none", label: "—" };
    if(rate < 50) return { key: "risk", label: "À risque" };
    if(rate < 75) return { key: "watch", label: "À surveiller" };
    return { key: "ok", label: "Bien" };
}

async function initClasses(){
    const [filieres, overviewResponse] = await Promise.all([
        loadFilieres(),
        fetch(`${API}/director/filieres-overview`, { headers: authHeaders() })
    ]);
    classState.filieres = filieres;
    classState.overview = overviewResponse.ok ? await overviewResponse.json() : [];

    renderClassList(filieres.length > 0 ? filieres[0].id : null);
    if(filieres.length > 0) await selectClass(filieres[0].id);
}

function renderClassList(activeId){
    const list = document.getElementById("classList");
    let html = "";
    for(const f of classState.filieres){
        const o = classState.overview.find(r => r.filiereName === f.name) || {};
        const total = o.studentsCount ?? 0;
        const risk = o.atRiskCount ?? 0;
        const share = total > 0 ? Math.round((risk / total) * 100) : 0;
        html += `<button class="cls-item ${f.id === activeId ? "active" : ""}" data-id="${f.id}">
                    <div class="cls-item-top">
                        <strong>${escapeHtml(f.name)}</strong>
                        ${risk > 0 ? `<span class="cls-chip danger">${risk} à risque</span>` : `<span class="cls-chip ok">RAS</span>`}
                    </div>
                    <div class="cls-item-sub">${total} étudiants${o.averageGrade != null ? ` · moyenne ${o.averageGrade.toFixed(2)}` : ""}</div>
                    <div class="cls-meter"><span style="width:${share}%"></span></div>
                </button>`;
    }
    list.innerHTML = html || `<p class="cls-empty">Aucune classe</p>`;
    list.querySelectorAll(".cls-item").forEach(btn => {
        btn.addEventListener("click", () => {
            list.querySelectorAll(".cls-item").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            selectClass(Number(btn.dataset.id));
        });
    });
}

async function selectClass(filiereId){
    const response = await fetch(`${API}/director/ml/${filiereId}`, { headers: authHeaders() });
    if(!response.ok) return;
    const students = await response.json();
    classState.students = students.slice().sort((a, b) => (a.successRate ?? 101) - (b.successRate ?? 101));

    const f = classState.filieres.find(x => x.id === filiereId);
    document.getElementById("clsName").textContent = f ? f.name : "";

    const rated = students.filter(s => s.successRate != null);
    const avg = rated.length ? rated.reduce((sum, s) => sum + s.successRate, 0) / rated.length : null;
    document.getElementById("clsCount").textContent = students.length;
    document.getElementById("clsRisk").textContent = students.filter(s => s.atRisk).length;
    document.getElementById("clsAvg").textContent = avg != null ? `${avg.toFixed(0)} %` : "—";

    renderClassStudents();
}

function renderClassStudents(){
    const q = classState.query.trim().toLowerCase();
    const rows = classState.students.filter(s => {
        const matchesTier = classState.filter === "all" || tier(s.successRate).key === classState.filter;
        const matchesQuery = q === "" || `${s.name} ${s.prenom}`.toLowerCase().includes(q);
        return matchesTier && matchesQuery;
    });

    let html = "";
    for(const s of rows){
        const t = tier(s.successRate);
        const initials = `${(s.prenom || "?")[0]}${(s.name || "")[0] || ""}`.toUpperCase();
        html += `<div class="cls-row">
                    <div class="cls-avatar ${t.key}">${escapeHtml(initials)}</div>
                    <div class="cls-who">${escapeHtml(s.prenom)} <strong>${escapeHtml(s.name)}</strong></div>
                    <div class="cls-bar"><span class="${t.key}" style="width:${s.successRate ?? 0}%"></span></div>
                    <div class="cls-rate">${s.successRate != null ? Math.round(s.successRate) + " %" : "—"}</div>
                    <span class="cls-tag ${t.key}">${t.label}</span>
                </div>`;
    }
    document.getElementById("classStudents").innerHTML = html || `<p class="cls-empty">Aucun étudiant à afficher</p>`;
}

document.getElementById("clsTabs").addEventListener("click", (event) => {
    const btn = event.target.closest("button");
    if(!btn) return;
    document.querySelectorAll("#clsTabs button").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    classState.filter = btn.dataset.filter;
    renderClassStudents();
});

document.getElementById("clsSearch").addEventListener("input", (event) => {
    classState.query = event.target.value;
    renderClassStudents();
});

async function loadFilieresOverview(){
    const response = await fetch(`${API}/director/filieres-overview`, { headers: authHeaders() });
    if(!response.ok) return;
    const rows = await response.json();

    const body = document.getElementById("filieresOverviewTableBody");
    let html = "";
    for(const f of rows){
        html += `<tr>
                    <td>${f.filiereName}</td>
                    <td>${f.studentsCount}</td>
                    <td>${f.professorsCount}</td>
                    <td>${f.averageGrade != null ? f.averageGrade.toFixed(2) : "—"}</td>
                    <td>${f.gradedCount}/${f.studentsCount}</td>
                    <td>${f.atRiskCount > 0 ? `<span class="badge badge-danger">${f.atRiskCount}</span>` : "0"}</td>
                </tr>`;
    }
    body.innerHTML = html || `<tr><td colspan="6">Aucune filière</td></tr>`;
}

async function fillPredictionsFiliereSelect(){
    const filieres = await loadFilieres();
    const select = document.getElementById("predictionsFiliereSelect");

    let optionsHtml = "";
    for(const f of filieres){
        optionsHtml += `<option value="${f.id}">${f.name}</option>`;
    }
    select.innerHTML = optionsHtml;

    if(filieres.length > 0){
        await fillPredictionsModuleSelect(filieres[0].id);
    }
}

async function fillPredictionsModuleSelect(filiereId){
    const response = await fetch(`${API}/director/modules/${filiereId}`, { headers: authHeaders() });
    if(!response.ok) return;
    const modules = await response.json();

    const select = document.getElementById("predictionsModuleSelect");
    let optionsHtml = "";
    for(const m of modules){
        optionsHtml += `<option value="${m.id}">${m.name} — ${m.profName}</option>`;
    }
    select.innerHTML = optionsHtml || `<option value="">Aucun module</option>`;

    if(modules.length > 0){
        await loadEarlyPredictions(modules[0].id);
    } else {
        document.getElementById("predictionsTableBody").innerHTML = `<tr><td colspan="7">Aucun module dans cette filière</td></tr>`;
    }
}

async function loadEarlyPredictions(moduleId){
    const body = document.getElementById("predictionsTableBody");
    if(!moduleId){
        body.innerHTML = `<tr><td colspan="7">Aucun module sélectionné</td></tr>`;
        return;
    }

    const response = await fetch(`${API}/director/predictions/${moduleId}`, { headers: authHeaders() });
    if(!response.ok) return;
    const rows = await response.json();

    let html = "";
    for(const r of rows){
        const badgeClass = r.atRisk ? "badge-danger" : "badge-success";
        html += `<tr>
                    <td>${r.name} ${r.prenom}</td>
                    <td>${r.note1 ?? "—"}</td>
                    <td>${r.note2 ?? "—"}</td>
                    <td>${r.note3 ?? "—"}</td>
                    <td>${r.notesUsed}/3</td>
                    <td>${r.confidence}</td>
                    <td><span class="badge ${badgeClass}">${r.successRate != null ? r.successRate + "%" : "—"}</span></td>
                </tr>`;
    }
    body.innerHTML = html || `<tr><td colspan="7">Aucune note saisie pour ce module pour l'instant</td></tr>`;
}

document.getElementById("predictionsFiliereSelect").addEventListener("change", (event) => {
    fillPredictionsModuleSelect(event.target.value);
});

document.getElementById("predictionsModuleSelect").addEventListener("change", (event) => {
    loadEarlyPredictions(event.target.value);
});

loadStats();
loadFilieresOverview();
fillFiliereSelect();
initClasses();
fillPredictionsFiliereSelect();
