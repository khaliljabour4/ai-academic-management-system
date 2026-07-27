import { permitAccess, logout } from "./utile.js";

permitAccess("Professor");

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

/* ---------- TODO: your data-loading / form-submit logic goes below ---------- */
