import {
    permitAccess,
    logout,
    getMySchedule,
    getMyModules,
    getMyConversations,
    sendAiQuestion
} from "./utile.js";

permitAccess("Student");


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

        if (btn.dataset.target === "schedule") loadSchedule();
        if (btn.dataset.target === "modules") loadModules();
        if (btn.dataset.target === "ai-assistant") loadModuleOptions();
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

/* ---------- Schedule ---------- */

const TIME_SLOTS = [
    { key: "MORNING", label: "8h-12h" },
    { key: "AFTERNOON", label: "13h-18h" }
];
const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];

async function loadSchedule() {
    const grid = document.getElementById("scheduleGrid");

    // Reset to just the header row, then rebuild
    grid.innerHTML = `
        <div></div>
        <div class="schedule-cell-head">Lun</div>
        <div class="schedule-cell-head">Mar</div>
        <div class="schedule-cell-head">Mer</div>
        <div class="schedule-cell-head">Jeu</div>
        <div class="schedule-cell-head">Ven</div>
        <div class="schedule-cell-head">Sam</div>
    `;

    let scheduleMap = {};
    try {
        // Requires the student's filiere id - TODO: read this from /me once that
        // endpoint exists; for now this call will simply fail gracefully.
        scheduleMap = await getMySchedule(localStorage.getItem("filiereId"));
    } catch (err) {
        console.warn("Schedule endpoint not available yet:", err.message);
    }

    TIME_SLOTS.forEach(slot => {
        const label = document.createElement("div");
        label.className = "schedule-slot-label";
        label.textContent = slot.label;
        grid.appendChild(label);

        DAYS.forEach(day => {
            const cell = document.createElement("div");
            const moduleForSlot = scheduleMap?.[day]?.[slot.key];

            if (moduleForSlot) {
                cell.className = "schedule-cell filled";
                cell.innerHTML = `
                    <div class="module-name">${moduleForSlot.name}</div>
                    <div class="module-teacher">${moduleForSlot.teacher?.name || ""}</div>
                `;
            } else {
                cell.className = "schedule-cell";
            }
            grid.appendChild(cell);
        });
    });
}

/* ---------- Modules & résumés ---------- */

async function loadModules() {
    const container = document.getElementById("modulesList");
    container.innerHTML = `<div class="empty-state">Chargement…</div>`;

    try {
        const modules = await getMyModules();

        if (!modules || modules.length === 0) {
            container.innerHTML = `<div class="empty-state">Aucun module pour l'instant</div>`;
            return;
        }

        container.innerHTML = modules.map(m => `
            <div class="card" style="margin-bottom:14px;">
                <div class="card-title">${m.name} ${m.hasTp ? '<span class="badge badge-warning">TP</span>' : ''}</div>
                <table class="data-table">
                    <tr><td>Note 1</td><td>${m.grade?.note1 ?? "—"}</td></tr>
                    <tr><td>Note 2</td><td>${m.grade?.note2 ?? "—"}</td></tr>
                    <tr><td>Note 3</td><td>${m.grade?.note3 ?? "—"}</td></tr>
                    ${m.hasTp ? `<tr><td>Note TP</td><td>${m.grade?.noteTp ?? "—"}</td></tr>` : ""}
                    <tr><td><strong>Moyenne</strong></td><td><strong>${m.grade?.noteGenerale ?? "—"}</strong></td></tr>
                </table>
                <div class="ai-panel" style="margin-top:14px;">
                    <div class="ai-panel-title">📄 Résumé du cours</div>
                    <p style="margin:0;font-size:13.5px;color:var(--color-text-muted);">
                        ${m.courseSummary || "Pas encore généré - à venir avec le résumeur PDF."}
                    </p>
                </div>
            </div>
        `).join("");
    } catch (err) {
        container.innerHTML = `<div class="empty-state">Endpoint backend pas encore créé (/student/modules)</div>`;
    }
}

/* ---------- AI assistant ---------- */

const moduleSelect = document.getElementById("moduleSelect");
const threadList = document.getElementById("threadList");
const chatMessages = document.getElementById("chatMessages");
const chatInput = document.getElementById("chatInput");
const chatSendBtn = document.getElementById("chatSendBtn");

async function loadModuleOptions() {
    try {
        const modules = await getMyModules();
        moduleSelect.innerHTML = modules.map(m => `<option value="${m.id}">${m.name}</option>`).join("");
        if (modules.length > 0) loadThreads(modules[0].id);
    } catch (err) {
        moduleSelect.innerHTML = `<option>Aucun module disponible</option>`;
    }
}

moduleSelect?.addEventListener("change", () => loadThreads(moduleSelect.value));

async function loadThreads(moduleId) {
    try {
        const conversations = await getMyConversations(moduleId);
        threadList.innerHTML = conversations.map(c => `
            <button class="chat-thread-item" data-title="${c.title}">${c.title}</button>
        `).join("") || `<div class="empty-state">Aucune conversation</div>`;
    } catch (err) {
        threadList.innerHTML = `<div class="empty-state">Backend pas encore branché</div>`;
    }
}

chatSendBtn?.addEventListener("click", async () => {
    const question = chatInput.value.trim();
    if (!question) return;

    const bubble = document.createElement("div");
    bubble.className = "chat-bubble question";
    bubble.textContent = question;
    chatMessages.appendChild(bubble);
    chatInput.value = "";
    chatMessages.scrollTop = chatMessages.scrollHeight;

    try {
        const moduleId = moduleSelect.value;
        const data = await sendAiQuestion(moduleId, "Conversation générale", question);

        const response = document.createElement("div");
        response.className = "chat-bubble response";
        response.textContent = data.response;
        chatMessages.appendChild(response);
    } catch (err) {
        const response = document.createElement("div");
        response.className = "chat-bubble response";
        response.textContent = "L'assistant IA n'est pas encore branché côté backend.";
        chatMessages.appendChild(response);
    }
    chatMessages.scrollTop = chatMessages.scrollHeight;
});

loadSchedule();
