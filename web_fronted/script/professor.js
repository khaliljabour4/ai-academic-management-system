import { permitAccess, logout } from "./utile.js";

permitAccess("Professor");

getProfessorModule() ;

 fillModuleFiliereSelection();
  

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

/* ---------- Burger / sidebar  ---------- */

const sidebar = document.getElementById("sidebar");
document.getElementById("burgerMenuBtn").addEventListener("click", () => {
    sidebar.classList.toggle("collapsed");
});

/* ---------- Logout ---------- */

document.getElementById("logoutBtn").addEventListener("click", logout);

/* ---------- fetch data  ---------- */


async function getProfessorModule(){
    const response = await fetch("http://localhost:8080/api/v1/professor/modules" , {
        headers:{
            "Authorization" : localStorage.getItem("jwt")
        }
    })

    const grid = document.getElementById("myModulesList");
    let moduleListHtml = `` ; 
    const data = await response.json();
    for(let i = 0 ; i < data.length  ; i ++){
        moduleListHtml += `<div class="module-card">
                            <div class="module-card-head">
                                <h3>${data[i].name}</h3>
                                <span class="badge badge-warning">TP</span>
                            </div>
                            <p class="module-card-desc">${data[i].filiereName}</p>
                            
                            </div>`
    }


    grid.innerHTML = moduleListHtml ;

    return data ; 
}


const moduleFiliereSelection = document.getElementById("notesFiliereModuleSelect");

async function fillModuleFiliereSelection(){
    const modules = await getProfessorModule() ;
    let selectionHtml= ``;
    for(let i = 0 ; i < modules.length ; i ++){
        selectionHtml += `<option value=${modules[i].id}>${modules[i].name}  -  ${modules[i].filiereName}</option>`
    }
    moduleFiliereSelection.innerHTML = selectionHtml ; 
}

document.getElementById("searchStudent").addEventListener("click" , getEtudiantsFiliere);

async function getEtudiantsFiliere(){
    const moduleId = moduleFiliereSelection.value ;
    const table = document.getElementById("moduleGradesTable");
    let listEtudiantsHtml = `` ; 

    const response = await fetch(`http://localhost:8080/api/v1/professor/students/${moduleId}` , {
        headers:{
            "Authorization" : localStorage.getItem("jwt") 
        }
    }); 

    const data = await response.json();

    let tableStudentHtml = `<thead>
                                <tr>
                                    <th> --- </th>
                                    <th>Étudiant</th>
                                    <th>Note 1</th>
                                    <th>Note 2</th>
                                    <th>Note 3</th>
                                    <th>Moyenne</th>
                                </tr>
                            </thead>`;
    for (let i = 0 ; i < data.length ; i ++){
        tableStudentHtml += `<tr data-student-id="${data[i].studentId}">
                                <td> ${i+1} </td>
                                <td>${data[i].name + " " + data[i].prenom}</td>`
            if(data[i].note1 != null){
                tableStudentHtml += `<td data-col="1">${data[i].note1}</td>`;
            }else{
                tableStudentHtml += `<td data-col="1"><input type="text" class="noteInput" data-col="1"></td>`;
            }
            if(data[i].note2 != null){
                tableStudentHtml += `<td data-col="2">${data[i].note2}</td>`;
            }else if(data[i].note1 != null){
                tableStudentHtml += `<td data-col="2"><input type="text" class="noteInput" data-col="2"></td>`;
            }else{
                tableStudentHtml += `<td data-col="2"> --- </td>`;
            }
            if(data[i].note3 != null){
                tableStudentHtml += `<td data-col="3">${data[i].note3}</td>`;
            }else if(data[i].note2 != null){
                tableStudentHtml += `<td data-col="3"><input type="text" class="noteInput" data-col="3"></td>`;
            }else{
                tableStudentHtml += `<td data-col="3"> --- </td>`;
            }
            tableStudentHtml += `<td class="moyenCell"> ${data[i].moyen != null ? data[i].moyen : "---"} </td>
                            </tr>`
    }

    table.innerHTML = tableStudentHtml + `<tr>
                                <td>  </td>
                                <td></td>
                                <td><button id="saveNote1" class="btn btn-primary">save</button></td>
                                <td><button id="saveNote2" class="btn btn-primary">save</button></td>
                                <td><button id="saveNote3" class="btn btn-primary">save</button></td>
                                <td></td>
                            </tr>` ;

    document.getElementById("saveNote1").addEventListener("click", () => saveGradeColumn(1));
    document.getElementById("saveNote2").addEventListener("click", () => saveGradeColumn(2));
    document.getElementById("saveNote3").addEventListener("click", () => saveGradeColumn(3));
}

async function saveGradeColumn(column){
    const moduleId = moduleFiliereSelection.value;
    const rows = document.querySelectorAll("#moduleGradesTable tr[data-student-id]");
    const entries = [];

    for(const row of rows){
        const studentId = Number(row.dataset.studentId);
        const cell = row.querySelector(`td[data-col="${column}"]`);
        if(!cell) continue;
        const input = cell.querySelector("input");
        if(!input) continue;
        const raw = input.value.trim();
        if(raw === ""){
            alert("Toutes les notes de la colonne doivent être remplies avant l'enregistrement");
            return;
        }
        entries.push({ studentId, value: Number(raw) });
    }

    if(entries.length === 0) return;

    const response = await fetch("http://localhost:8080/api/v1/professor/grades", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem("jwt")
        },
        body: JSON.stringify({ moduleId: Number(moduleId), column, entries })
    });

    if(response.ok){
        getEtudiantsFiliere();
    }else{
        const message = await response.text();
        alert(message || "Erreur lors de l'enregistrement");
    }
}

