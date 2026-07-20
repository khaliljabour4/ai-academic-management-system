import { permitAccess } from "./utile.js" ;

permitAccess("Administrator");


const burgerMenu = document.getElementById("burgerMenuBtn");
const sidebar =  document.getElementById("sidebar");

const closeMenu = document.getElementById("closeMenuBtn");
const creerFiliereBtn = document.getElementById("sidebarCreatioinFiliereBtn");
const creerProfBtn = document.getElementById("sidebarCreationProfBtn");
const creerEtudiantBtn = document.getElementById("sidebarcreerEtudiantBtn");
const overviewBtn = document.getElementById("sidebarOverviewBtn");

const etudiantSection = document.querySelector(".creation-etudiant");
const profSection = document.querySelector(".creation-prof");
const filiereSection = document.querySelector(".creation-filiere");
const overviewSection = document.querySelector(".overview");

burgerMenu.addEventListener("click", () => {
    burgerMenu.style.display="none";
    sidebar.style.display = "flex";
});

closeMenu.addEventListener('click' ,() =>{
    burgerMenu.style.display="block";
    sidebar.style.display = "none";
})


creerFiliereBtn.addEventListener('click' ,() =>{
    burgerMenu.style.display="block";
    sidebar.style.display = "none";
    etudiantSection.style.display = "none";
    profSection.style.display = "none";
    overviewSection.style.display = "none";
    filiereSection.style.display = "flex";
})


creerProfBtn.addEventListener('click' ,() =>{
    burgerMenu.style.display="block";
    sidebar.style.display = "none";
    etudiantSection.style.display = "none";
    profSection.style.display = "flex";
    overviewSection.style.display = "none";
    filiereSection.style.display = "none";
})
creerEtudiantBtn.addEventListener('click' ,() =>{
    burgerMenu.style.display="block";
    sidebar.style.display = "none";
    etudiantSection.style.display = "flex";
    profSection.style.display = "none";
    overviewSection.style.display = "none";
    filiereSection.style.display = "none";
})
overviewBtn.addEventListener('click' ,() =>{
    burgerMenu.style.display="block";
    sidebar.style.display = "none";
    etudiantSection.style.display = "none";
    profSection.style.display = "none";
    overviewSection.style.display = "flex";
    filiereSection.style.display = "none";
})







const filiereCreationSubmitBtn = document.getElementById('filiereCreationSubmitBtn');

filiereCreationSubmitBtn.addEventListener('click' , creerFiliere);

async function creerFiliere(){
    const filiereName = document.getElementById("filierName").value;
    const filiereDescription = document.getElementById("filiereDescription").value;

    const response = await fetch("http://localhost:8080/api/v1/admin/filiere",
        {
            method: "POST",
            headers:{
                "Content-Type": "application/json",
                "Authorization" : localStorage.getItem("jwt")
            },
            body:JSON.stringify({
                name:filiereName,
                description : filiereDescription
            })
        }
    )

    if (response.status === 401 || response.status === 403) {
        window.alert("ur not logging try to ");
        window.location.href = "login.html";
        return;
    }

    if(!response.ok){
        window.alert("Something went wrong: " + response.status);
        return;
    }

    const data = await response.json();

    console.log(data);

    window.alert("creation avec succes")
}