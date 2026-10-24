// Local dev (Live Server on :5500): send /api/... calls to the backend on :8080.
// Behind nginx (Docker/server) the same relative URLs are proxied, so nothing to change.
if (["5500", "5501"].includes(window.location.port)) {
    const originalFetch = window.fetch.bind(window);
    window.fetch = (url, options) =>
        originalFetch(typeof url === "string" && url.startsWith("/api/") ? "http://localhost:8080" + url : url, options);
}


async function login(){
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    try{
        const response = await fetch("/api/v1/auth/authenticate" ,
            {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                email: username,      
                password: password
            })
        });

        if(!response.ok){
            alert("access denied");
            return ; 
        }else{
            console.log("access giving");
        }

        const  data = await response.json() ;
        console.log("token :" + data.token) ;
        console.log("token :" + data.role) ;

        localStorage.setItem("jwt","Bearer " + data.token);
        localStorage.setItem("role", data.role);

        switch(data.role){
            case "Director" : window.location.href="director.html";break;
            case "Student" : window.location.href="student.html";break;
            case "Administrator" : window.location.href="admin.html";break;
            case "Professor" : window.location.href="professor.html";break;
        }
    }
    catch(error){
        console.error(error);
        alert("error");
    }
    }