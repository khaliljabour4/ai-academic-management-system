import { permitAccess, logout, escapeHtml } from "./utile.js";

permitAccess("Student");

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

const API = "/api/v1/student";

function authHeaders(){
    return { "Authorization": localStorage.getItem("jwt") };
}

async function loadSchedule(){
    const response = await fetch(`${API}/schedule`, { headers: authHeaders() });
    if(!response.ok) return;
    const entries = await response.json();

    const days = ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];
    const slots = ["MORNING","AFTERNOON"];
    const grid = document.getElementById("scheduleGrid");

    let html = `<div></div>
        <div class="schedule-cell-head">Lun</div>
        <div class="schedule-cell-head">Mar</div>
        <div class="schedule-cell-head">Mer</div>
        <div class="schedule-cell-head">Jeu</div>
        <div class="schedule-cell-head">Ven</div>
        <div class="schedule-cell-head">Sam</div>`;

    for(const slot of slots){
        html += `<div class="schedule-slot-label">${slot === "MORNING" ? "Matin" : "Après-midi"}</div>`;
        for(const day of days){
            const entry = entries.find(e => e.dayOfWeek === day && e.timeSlot === slot);
            if(entry){
                html += `<div class="schedule-cell filled">
                            <div class="module-name">${escapeHtml(entry.moduleName)}</div>
                            <div class="module-teacher">${escapeHtml(entry.teacherName)}</div>
                        </div>`;
            }else{
                html += `<div class="schedule-cell"></div>`;
            }
        }
    }

    grid.innerHTML = html;
}

async function loadModules(){
    const response = await fetch(`${API}/modules`, { headers: authHeaders() });
    if(!response.ok) return [];
    const modules = await response.json();

    const list = document.getElementById("modulesList");
    let html = "";
    for(const m of modules){
        html += `<div class="module-card">
                    <div class="module-card-head">
                        <h3>${escapeHtml(m.name)}</h3>
                        <span class="badge badge-success">Prof. ${escapeHtml(m.teacherName)}</span>
                    </div>
                    <p class="module-card-desc">${escapeHtml(m.description ?? "Aucune description pour le moment")}</p>
                </div>`;
    }
    list.innerHTML = html || "<p>Aucun module pour le moment</p>";

    const gradesBody = document.getElementById("gradesTableBody");
    let gradesHtml = "";
    for(const m of modules){
        gradesHtml += `<tr>
                    <td>${escapeHtml(m.name)}</td>
                    <td>${m.note1 ?? "—"}</td>
                    <td>${m.note2 ?? "—"}</td>
                    <td>${m.note3 ?? "—"}</td>
                    <td>—</td>
                    <td>${m.noteGenerale ?? "—"}</td>
                </tr>`;
    }
    gradesBody.innerHTML = gradesHtml;

    return modules;
}

let currentConversationId = null;
let currentConversationTitle = null;

async function loadConversations(moduleId){
    const response = await fetch(`${API}/conversations/${moduleId}`, { headers: authHeaders() });
    if(!response.ok) return;
    const conversations = await response.json();

    const threadList = document.getElementById("threadList");
    let html = "";
    for(const c of conversations){
        html += `<button class="chat-thread-item" data-id="${c.id}" data-title="${escapeHtml(c.title)}">${escapeHtml(c.title)}</button>`;
    }
    threadList.innerHTML = html || `<p style="padding:10px;color:var(--color-text-muted);font-size:13px;">Aucune conversation</p>`;

    threadList.querySelectorAll(".chat-thread-item").forEach(btn => {
        btn.addEventListener("click", () => openConversation(Number(btn.dataset.id), btn.dataset.title));
    });
}

async function openConversation(id, title){
    currentConversationId = id;
    currentConversationTitle = title;

    document.querySelectorAll(".chat-thread-item").forEach(b => b.classList.remove("active"));
    const activeBtn = document.querySelector(`.chat-thread-item[data-id="${id}"]`);
    if(activeBtn) activeBtn.classList.add("active");

    const response = await fetch(`${API}/conversation/${id}`, { headers: authHeaders() });
    if(!response.ok) return;
    const conversation = await response.json();
    renderMessages(conversation.messages);
}

function renderMessages(messages){
    const chatMessages = document.getElementById("chatMessages");
    let html = "";
    for(let i = 0; i < messages.length; i += 2){
        html += `<div class="chat-bubble question">${escapeHtml(messages[i])}</div>`;
        if(messages[i+1] !== undefined){
            html += `<div class="chat-bubble response">${escapeHtml(messages[i+1])}</div>`;
        }
    }
    chatMessages.innerHTML = html;
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

document.getElementById("newConversationBtn").addEventListener("click", () => {
    const title = window.prompt("Titre de la conversation");
    if(!title) return;
    currentConversationId = null;
    currentConversationTitle = title;
    document.querySelectorAll(".chat-thread-item").forEach(b => b.classList.remove("active"));
    document.getElementById("chatMessages").innerHTML = "";
});

async function sendQuestion(){
    const input = document.getElementById("chatInput");
    const question = input.value.trim();
    if(question === "") return;

    const moduleId = document.getElementById("moduleSelect").value;
    if(!moduleId){
        window.alert("Choisis un module d'abord");
        return;
    }
    if(!currentConversationTitle){
        window.alert("Choisis ou crée une conversation d'abord");
        return;
    }

    input.value = "";

    const response = await fetch(`${API}/conversation/${moduleId}/ask`, {
        method: "POST",
        headers: { ...authHeaders(), "Content-Type": "application/json" },
        body: JSON.stringify({ conversationTitle: currentConversationTitle, question })
    });

    if(!response.ok){
        window.alert("L'assistant IA n'a pas pu répondre");
        return;
    }

    const conversation = await response.json();
    currentConversationId = conversation.id;
    renderMessages(conversation.messages);
    await loadConversations(moduleId);
}

document.getElementById("chatSendBtn").addEventListener("click", sendQuestion);
document.getElementById("chatInput").addEventListener("keydown", (event) => {
    if(event.key === "Enter") sendQuestion();
});

document.getElementById("moduleSelect").addEventListener("change", () => {
    currentConversationId = null;
    currentConversationTitle = null;
    document.getElementById("chatMessages").innerHTML = "";
    loadConversations(document.getElementById("moduleSelect").value);
});

async function init(){
    await loadSchedule();
    const modules = await loadModules();

    const moduleSelect = document.getElementById("moduleSelect");
    let optionsHtml = "";
    for(const m of modules){
        optionsHtml += `<option value="${m.moduleId}">${escapeHtml(m.name)}</option>`;
    }
    moduleSelect.innerHTML = optionsHtml;

    if(modules.length > 0){
        await loadConversations(modules[0].moduleId);
    }
}

init();
