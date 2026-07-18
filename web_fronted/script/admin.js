import { permitAccess } from "./utile.js" ;

permitAccess("Administrator");


const burgerMenu = document.getElementById("burgerMenuBtn");
const sidebar =  document.getElementById("sidebar");

burgerMenu.addEventListener("click", () => {
    burgerMenu.style.display="none";
    sidebar.style.display = "block";
});

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

    window.alert("creation avec succes")
}