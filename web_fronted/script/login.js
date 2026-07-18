
async function login(){
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    try{
        const response = await fetch("http://localhost:8080/api/v1/auth/authenticate" ,
            {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                email: username,      
                password: password
            })
        });

        if(!response.ok){
            console.log("access denied");
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
    }
    }