import { permitAccess, logout } from "./utile.js";

permitAccess("Administrator");

const data = await getAllFilliere();
fillTableFiliere(data);

fillOptionFiliereStudent();
await fillOptionModule();
await fillModuleSlotSelection();

 getAllStudent();
getAllModule();
fillOptionFiliereOverview() ; 

 getAllFiliereInfo();

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
    });
});

const sidebar = document.getElementById("sidebar");
document.getElementById("burgerMenuBtn").addEventListener("click", () => {
    sidebar.classList.toggle("collapsed");
});

document.getElementById("logoutBtn").addEventListener("click", logout);

/* ---------- Filiere---------- */

async function getAllFilliere() {
    const response = await fetch("http://localhost:8080/api/v1/admin/filiere", {
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem("jwt"),
        }
    });

    if (response.status === 401 || response.status === 403) {
        window.alert("waaaa");
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


const filiereCreationSubmitBtn = document.getElementById('filiereCreationSubmitBtn');

filiereCreationSubmitBtn.addEventListener('click', async () => {
    await creerFiliere();
    const ListFiliere = await getAllFilliere();
    fillTableFiliere(ListFiliere);
});

async function creerFiliere() {
    const filiereName = document.getElementById("filierName");
    const filiereDescription = document.getElementById("filiereDescription");

    if (filiereName.value == "") {
        window.alert("please enter a feild name");
        return;
    }

    if (filiereDescription.value == "") {
        window.alert("please enter a filiere description");
        return;
    }

    const response = await fetch("http://localhost:8080/api/v1/admin/filiere", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": localStorage.getItem("jwt")
        },
        body: JSON.stringify({
            name: filiereName.value,
            description: filiereDescription.value
        })
    });

    if (response.status === 401 || response.status === 403) {
        window.alert("ur not logging try to ");
        window.location.href = "login.html";
        return;
    }

    if (!response.ok) {
        window.alert("Something went wrong: " + response.status);
        return;
    }

    const data = await response.json();

    window.alert("creation avec succes");

    filiereName.value = "";
    filiereDescription.value = "";
}

function fillTableFiliere(data) {
    let tableHtml = `<tr>
                      <th>Nom</th>
                      <th>Description</th>
                      <th></th>
                        </tr>`;

    for (let i = 0; i < data.length; i++) {
        tableHtml += `<tr>
                        <td>${data[i].name}</td>
                        <td >${data[i].description}</td>
                        <td>
                            <button class="icon-btn delete-filiere-btn" value="${data[i].id}" title="Supprimer" >✕</button>
                        </td>
                    </tr>
        `;
    }

    document.getElementById("tableRows").innerHTML = tableHtml;
}

const table = document.getElementById("tableRows");

table.addEventListener("click", (event) => {
    const button = event.target.closest(".delete-filiere-btn");
    if (!button) return;
    deleteFiliere(button.value);
});

async function deleteFiliere(id) {
    const response = await fetch(`http://localhost:8080/api/v1/admin/filiere?id=${id}`, {
        method: "DELETE",
        headers: {
            "Authorization": localStorage.getItem("jwt")
        }
    });

    if (!response.ok) {
        window.alert("something went wwrong");
        return;
    }

    window.alert("delete by succes ");
    if (response.ok) {
        const data = await getAllFilliere();
        fillTableFiliere(data);
    }
}


/* ---------- Prof---------- */


async function creerProf(){
    const  name = document.getElementById("profLastName").value ; 
    const prenom = document.getElementById("profFirstName").value; 

    const response = await fetch("http://localhost:8080/api/v1/admin/prof" , 
        {
            method :"POST" , 
            headers:{
                "Authorization" : localStorage.getItem("jwt"),
                "Content-Type" :"application/json"
            },
            body :JSON.stringify({
                name : name ,
                prenom : prenom
            })
        });


    if(!response.ok){
         window.alert("prof wasnt created ");
         return 
    }

    const created = await response.json();
    window.alert("professeur cree\nemail : " + created.email + "\nmot de passe : " + created.password);
    document.getElementById("profLastName").value = "";
    document.getElementById("profFirstName").value = "";
}

const creerProfBtn = document.getElementById("createProfBtn");

creerProfBtn.addEventListener('click' ,async ()=>{
    await creerProf() ;
    await getAllProfs();
});

async function getAllProfs() {
    const response = await fetch("http://localhost:8080/api/v1/admin/profs", {
        headers: {
            "Authorization": localStorage.getItem("jwt")
        }
    });

    if (response.status === 401 || response.status === 403) {
        window.location.href = "login.html";
        return;
    }

    if (!response.ok) {
        console.error("Failed to load professors:", response.status);
        return;
    }

    const professors = await response.json();
    fillTableProfs(professors);
    return professors ;
}

function fillTableProfs(data) {
    let tableHtml = `<tr>
                        <th>Nom</th>
                        <th>Prénom</th>
                        <th>Email</th>
                        <th></th>
                      </tr>`;

    for (let i = 0; i < data.length; i++) {
        tableHtml += `<tr>
                        <td>${data[i].name}</td>
                        <td>${data[i].prenom}</td>
                        <td>${data[i].user.email}</td>
                        <td>
                            <button class="icon-btn delete-prof-btn" value="${data[i].id}" title="Supprimer">✕</button>
                        </td>
                    </tr>`;
    }

    document.getElementById("profTableRows").innerHTML = tableHtml;
}

const profRow = document.getElementById("profTableRows");
profRow.addEventListener("click", (event) => {
    const button = event.target.closest(".delete-prof-btn");
    if (!button) return;
    deleteProf(button.value);
});
async function deleteProf(profId){
    const response = await fetch (`http://localhost:8080/api/v1/admin/profs/delete/${profId}` , {
        method: "DELETE" , 
        headers:{
            "Authorization" : localStorage.getItem("jwt")
        }
    })

    if(!response.ok){
        window.alert("prof was not deleted") ; 
        return ;
    }

    await getAllProfs();

}


/* ---------- Etudiant---------- */

function fillOptionFiliereStudent(){
    const etudiantSelectionFiliere = document.getElementById("studentFiliere");

    let optionHtml = `` ;

    for(let i =0  ; i <data.length ; i++){
        optionHtml += `<option value=${data[i].id}>
                            ${data[i].name}
                        </option>`;
    } ; 

    etudiantSelectionFiliere.innerHTML = optionHtml ; 
}

let editingStudentId = null;

async function createStudent(){

    const  lastName = document.getElementById("studentLastName");
    const  firstName = document.getElementById("studentFirstName");
    const  cni = document.getElementById("studentCNI");
    const  cne = document.getElementById("studentCNE");
    const filiereId = document.getElementById("studentFiliere").value;

    const response = await fetch("http://localhost:8080/api/v1/admin/student", {
    method: "POST",
    headers: {
        "Authorization": localStorage.getItem('jwt'),
        "Content-Type": "application/json"
    },
    body: JSON.stringify({
        name: lastName.value,
        prenom: firstName.value,
        cni: cni.value,
        cne: cne.value,
        filiereId: filiereId
        })
    });

 if (response.status === 401 || response.status === 403) {
        window.alert("ur not allowed");
        return;
    }
    if (!response.ok) {
        window.alert("Student wasn't created");
        return;
    }
    const created = await response.json();
    window.alert("etudiant cree\nemail : " + created.email + "\nmot de passe : " + created.password);
    clearStudentForm();
}

async function updateStudent(id){

    const  lastName = document.getElementById("studentLastName");
    const  firstName = document.getElementById("studentFirstName");
    const  cni = document.getElementById("studentCNI");
    const  cne = document.getElementById("studentCNE");
    const filiereID = document.getElementById("studentFiliere").value;

    const response = await fetch(`http://localhost:8080/api/v1/admin/students/${id}`, {
    method: "PUT",
    headers: {
        "Authorization": localStorage.getItem('jwt'),
        "Content-Type": "application/json"
    },
    body: JSON.stringify({
        name: lastName.value,
        prenom: firstName.value,
        cni: cni.value,
        cne: cne.value,
        filiereId: filiereID
        })
    });

    if (response.status === 401 || response.status === 403) {
        window.alert("ur not allowed");
        return;
    }
    if (!response.ok) {
        window.alert("Student wasn't updated");
        return;
    }
    window.alert("Student updated successfully");
    clearStudentForm();
}

function startEditStudent(student){
    editingStudentId = student.id;
    document.getElementById("studentLastName").value = student.name;
    document.getElementById("studentFirstName").value = student.prenom;
    document.getElementById("studentCNI").value = student.cni ?? "";
    document.getElementById("studentCNE").value = student.cne ?? "";
    document.getElementById("studentFiliere").value = student.filiereId;
    saveStudentBtn.textContent = "Modifier";
}

function clearStudentForm(){
    editingStudentId = null;
    document.getElementById("studentLastName").value = "";
    document.getElementById("studentFirstName").value = "";
    document.getElementById("studentCNI").value = "";
    document.getElementById("studentCNE").value = "";
    saveStudentBtn.textContent = "Enregistrer";
}

async function getAllStudent() {
    const response = await fetch("http://localhost:8080/api/v1/admin/students", {
        headers: {
            "Authorization": localStorage.getItem("jwt")
        }
    });

    if (response.status === 401 || response.status === 403) {
        window.location.href = "login.html";
        return;
    }

    if (!response.ok) {
        console.error("Failed to load students:", response.status);
        return;
    }

    const student = await response.json();
    fillTableStudent(student);
}

let lastLoadedStudents = [];

function fillTableStudent(data) {
    lastLoadedStudents = data;

    let tableHtml = `<tr>
                        <th>Nom</th>
                        <th>Prénom</th>
                        <th>Email</th>
                        <th></th>
                      </tr>`;

    for (let i = 0; i < data.length; i++) {
        tableHtml += `<tr>
                        <td>${data[i].name}</td>
                        <td>${data[i].prenom}</td>
                        <td>${data[i].email ?? "—"}</td>
                        <td>
                            <button class="icon-btn edit-student-btn" value="${data[i].id}" title="Modifier">✎</button>
                            <button class="icon-btn delete-student-btn" value="${data[i].id}" title="Supprimer">✕</button>
                        </td>
                    </tr>`;
    }

    document.getElementById("studentTableRows").innerHTML = tableHtml;
}

const saveStudentBtn = document.getElementById("createStudentBtn") ;
saveStudentBtn.addEventListener("click" , async ()=>{
    if(editingStudentId != null){
        await updateStudent(editingStudentId);
    }else{
        await createStudent() ;
    }
    getAllStudent();
})

const studentRow = document.getElementById("studentTableRows");
studentRow.addEventListener("click", (event) => {
    const deleteButton = event.target.closest(".delete-student-btn");
    if (deleteButton) {
        deleteStudent(deleteButton.value);
        return;
    }
    const editButton = event.target.closest(".edit-student-btn");
    if (editButton) {
        const student = lastLoadedStudents.find(s => s.id == editButton.value);
        if (student) startEditStudent(student);
    }
});
async function deleteStudent(studentId){
    const response = await fetch (`http://localhost:8080/api/v1/admin/students/delete?id=${studentId}` , {
        method: "DELETE" , 
        headers:{
            "Authorization" : localStorage.getItem("jwt")
        }
    })

    if(!response.ok){
        window.alert("prof was not deleted") ; 
        return ;
    }

    await getAllStudent();

}
/* ---------- Module---------- */

async function fillOptionModule(){
    const selectionFiliereModule = document.getElementById("moduleFiliere");
    const selectionProfModule = document.getElementById("moduleProfessor");

    let optionfiliereHtml = `` ;
    let optionProfHtml = `` ;

    const professors = await getAllProfs();
    for(let i =0  ; i <data.length ; i++){
        optionfiliereHtml += `<option value=${data[i].id}>
                            ${data[i].name}
                        </option>`;
    } ; 

    for(let i = 0 ; i < professors.length ; i++){
        optionProfHtml += `<option value=${professors[i].id}>
            ${professors[i].name}
        </option>`;
    } 

    selectionFiliereModule.innerHTML = optionfiliereHtml ;
    selectionProfModule.innerHTML = optionProfHtml ; 
}


async function fillModuleSlotSelection(){
    const moduleSlotSelect = document.getElementById("moduleSlot");
    const filiereId = document.getElementById("moduleFiliere").value;
    if(!filiereId){
        moduleSlotSelect.innerHTML = `<option value="">Choisis une filière d'abord</option>`;
        return;
    }

    const response = await fetch(`http://localhost:8080/api/v1/admin/available-slots/${filiereId}`, {
        headers: {
            "Authorization": localStorage.getItem("jwt")
        }
    });

    if(!response.ok){
        moduleSlotSelect.innerHTML = `<option value="">Aucun créneau disponible</option>`;
        return;
    }

    const slots = await response.json();
    let slotOptionsHtml = ``;
    for(let i = 0; i < slots.length; i++){
        slotOptionsHtml += `<option value="${slots[i].dayOfWeek}|${slots[i].timeSlot}">${slots[i].dayOfWeek} - ${slots[i].timeSlot}</option>`;
    }
    moduleSlotSelect.innerHTML = slotOptionsHtml || `<option value="">Aucun créneau disponible</option>`;
}

document.getElementById("moduleFiliere").addEventListener("change", fillModuleSlotSelection);

async function createModule (){
    const moduleName = document.getElementById("moduleName").value;
    const moduleDesc = document.getElementById("moduleDesc").value ;
    const profId = parseInt(document.getElementById("moduleProfessor").value);
    const filiereId = parseInt(document.getElementById("moduleFiliere").value); 
    const slotValue = moduleSlotSelect.value;

    if(!slotValue){
        window.alert("Aucun créneau libre sélectionné");
        return;
    }

    const [filiereDay, timeSlot] = slotValue.split("|");

    const response = await fetch("http://localhost:8080/api/v1/admin/module", {
    method: "POST",
    headers: {
        "Authorization": localStorage.getItem('jwt'),
        "Content-Type": "application/json"
    },
    body: JSON.stringify({
            name :moduleName , 
            description : moduleDesc , 
            profId : profId , 
            filiereId : filiereId , 
            dayOfWeek : filiereDay , 
            timeSlot : timeSlot 
        })
    });

    if (response.status === 401 || response.status === 403) {
        window.alert("ur not allowed");
        return;
    }
    if (!response.ok) {
        window.alert("Module wasn't created");
        return;
    }
    window.alert("Module created successfully");
    await fillModuleSlotSelection();
}

async function getAllModule() {
    const response = await fetch("http://localhost:8080/api/v1/admin/modules", {
        method: "GET",
        headers: {
            "Authorization": localStorage.getItem("jwt")
        }
    });

    if (response.status === 401 || response.status === 403) {
        console.log('unhotorized');
        return;
    }

    if (!response.ok) {
        console.error("Failed to load students:", response.status);
        return;
    }

    const modules = await response.json();
    fillTableModule(modules);
}
function fillTableModule(modules) {

    let tableHtml = `<tr>
                        <th>Nom</th>
                        <th>description</th>
                        <th>filiere</th>
                        <th>Prof</th>
                        <th></th>
                      </tr>`;

    for (let i = 0; i < modules.length; i++) {
        tableHtml += `<tr>
                        <td>${modules[i].name}</td>
                        <td>${modules[i].description}</td>
                        <td>${modules[i].filiereName}</td>
                        <td>${modules[i].profName}</td>
                        <td>
                            <button class="icon-btn delete-module-btn" value="${modules[i].id}" title="Supprimer">✕</button>
                        </td>
                    </tr>`;
    }

    document.getElementById("moduleTableRows").innerHTML = tableHtml;
}

document.getElementById("moduleTableRows").addEventListener("click", (event) => {
    const button = event.target.closest(".delete-module-btn");
    if (!button) return;
    deleteModule(button.value);
});

async function deleteModule(id){
    const response = await fetch(`http://localhost:8080/api/v1/admin/module/delete/${id}`, {
        method: "DELETE",
        headers: {
            "Authorization": localStorage.getItem("jwt")
        }
    });

    if(!response.ok){
        window.alert("module was not deleted");
        return;
    }

    await getAllModule();
    await fillModuleSlotSelection();
}

document.getElementById("createModuleBtn").addEventListener("click" , async () =>{
    await createModule();
    await getAllModule();
})
/* ---------- overview---------- */



function fillOptionFiliereOverview(){
    const selectionFiliere= document.getElementById("overviewFiliere");

    let optionHtml = `` ;

    for(let i =0  ; i <data.length ; i++){
        optionHtml += `<option value=${data[i].id}>
                            ${data[i].name}
                        </option>`;
    } ; 

    selectionFiliere.innerHTML = optionHtml ; 
}


document.addEventListener("DOMContentLoaded" , getAllFiliereInfo);

document.getElementById("overviewFiliere").addEventListener("change" , getAllFiliereInfo);
;

async function getAllFiliereInfo(){
    const etudiantTable = document.getElementById("overviewStudentTable");
    const profModuleTable = document.getElementById("overviewProfessorModuleTable");
    const filiereId = document.getElementById("overviewFiliere").value;
    let profTable = `<tr>
                        <th>prof name</th>
                        <th>module name </th>
                    </tr>` ; 
    let etudiantTableHtml = `<tr>
                        <th>student name</th>
                        <th>student prenom </th>
                    </tr>` ; 
    const response = await fetch (`http://localhost:8080/api/v1/admin/overview/${filiereId}`,{
        method: "GET",
        headers: {
            "Authorization": localStorage.getItem("jwt")
        }
    });

    const details = await response.json();
    console.log(details);
    for(let i = 0 ; i < details.modules.length ; i ++){
        profTable += `<tr>
            <td>${details.professors[i]}</td>
            <td>${details.modules[i]}</td>
        </tr>`
    }

    for(let i =0 ; i <details.etudiants.length ; i++){
        etudiantTableHtml += `<tr>
            <td>${details.etudiants[i].name}</td>
            <td>${details.etudiants[i].prenom}</td>
        </tr>`
    }

    console.log(details);

    profModuleTable.innerHTML = profTable ; 
    etudiantTable.innerHTML = etudiantTableHtml ; 
}
