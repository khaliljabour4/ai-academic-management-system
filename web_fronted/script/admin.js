import { permitAccess, getAllFilliere, logout } from "./utile.js";

permitAccess("Administrator");

const data = await getAllFilliere();
fillTableFiliere(data);

fillOptionFiliereForProf();


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

/* ---------- Filiere: create / list / delete (unchanged logic) ---------- */

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


function fillOptionFiliereForProf(){
    const selectionFiliereProf = document.getElementById("profFiliere");
    let selectionHtml = ``;
    for (let i = 0 ; i < data.length ; i++){
        selectionHtml += `<option value=${data[i].id}>${data[i].name}</option>`;
    }

    console.log(data);
    console.log(selectionHtml);
    selectionFiliereProf.innerHTML = selectionHtml;


}

async function creerProf(){
    const  name = document.getElementById("profLastName").value ; 
    const prenom = document.getElementById("profFirstName").value; 
    const filiereid = document.getElementById("profFiliere").value;
        const password = document.getElementById("profPassword").value ; 

    const response = await fetch("http://localhost:8080/api/v1/admin/prof" , 
        {
            method :"POST" , 
            headers:{
                "Authorization" : localStorage.getItem("jwt"),
                "Content-Type" :"application/json"
            },
            body :JSON.stringify({
                name : name ,
                prenom : prenom , 
                filiereid : filiereid , 
                password :password 
            })
        });


    if(!response.ok){
         window.alert("prof wasnt created ");
         return 
    }

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
                        <td>${data[i].user?.email ?? "—"}</td>
                        <td>
                            <button class="icon-btn delete-prof-btn" value="${data[i].id}" title="Supprimer">✕</button>
                        </td>
                    </tr>`;
    }

    document.getElementById("profTableRows").innerHTML = tableHtml;
}