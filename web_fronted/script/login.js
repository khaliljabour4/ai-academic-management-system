
async function login(){
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    console.log(username + " " + password) ;


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
            console.log("acces denied");
            return ; 
        }else{
            console.log("acces giving");
        }

        const  data = await response.json() ;
        console.log("token :" + data.token) ;

        localStorage.setItem("jwt", data.token);
        alert("Login successful!");

    }
    catch(error){
        console.error(error);
    }
    }