package com.codewithkahlil.backend.controler;


import com.codewithkahlil.backend.auth.AuthenticationService;
import com.codewithkahlil.backend.auth.RegistreRequest;
import com.codewithkahlil.backend.dto.requests.filiereRequests;
import com.codewithkahlil.backend.dto.requests.profRequest;
import com.codewithkahlil.backend.model.Filiere;
import com.codewithkahlil.backend.model.Professor;
import com.codewithkahlil.backend.model.Role;
import com.codewithkahlil.backend.model.User;
import com.codewithkahlil.backend.repository.FiliereRepository;
import com.codewithkahlil.backend.repository.ProfessorRepository;
import com.codewithkahlil.backend.repository.UserRepository;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.web.ReactiveOffsetScrollPositionHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class AdministrtatorController {

    private final FiliereRepository filiereRepository ;
    private final AuthenticationService authenticationService ;
    private final UserRepository userRepository ;
    private final  ProfessorRepository professorRepository ;

    @PostMapping("/filiere")
    ResponseEntity<?> creerFiliere (@RequestBody filiereRequests filiereRequest){
        Filiere filiereExisted = filiereRepository.findByName(filiereRequest.getName().toLowerCase());

        if(filiereExisted != null){
            return new ResponseEntity<>("filiere existe deja",HttpStatus.CONFLICT);
        }
        try{
            Filiere filiere = Filiere.builder()
                    .name(filiereRequest.getName().toLowerCase())
                    .description(filiereRequest.getDescription())
                    .build();

            Filiere saved =filiereRepository.save(filiere);

            return new ResponseEntity<>( saved ,HttpStatus.CREATED);
        }catch(Exception e){
            return new ResponseEntity<>("filiere not saved ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/filiere")
    ResponseEntity<?> getAllFiliere (){
        List<Filiere> filieres  = new ArrayList<>();
        try{
            filieres = filiereRepository.findAll();
            return new ResponseEntity<>(filieres , HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("filiere")
    ResponseEntity<?> deleteFiliereByID(@RequestParam int id){
        try{
            filiereRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    //prof
    @PostMapping("/prof")
    ResponseEntity<?> creerProf(@RequestBody profRequest request){
        String name = request.getName().toLowerCase() ;
        String prenom = request.getPrenom().toLowerCase() ;
        String email = name+prenom+"@test.com";
        String password = request.getPassword();
        int filiereid = request.getFiliereid();

        RegistreRequest registre= RegistreRequest.builder()
                .email(email)
                .password(password)
                .role(Role.Professor)
                .build() ;

        User user  = userRepository.findUserByEmail(email);
        if(user!= null){
            return new ResponseEntity<>(HttpStatus.IM_USED);
        }
        try{
            authenticationService.register(registre) ;
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        User userCreated = userRepository.findUserByEmail(email);

        Professor professor = (Professor) Professor.builder()
                .name(name)
                .prenom(prenom)
                .user(userCreated)
                .build();

        try{
            professorRepository.save(professor) ;
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return  new ResponseEntity<>("email : "+email,HttpStatus.OK) ;
    }

    @GetMapping("/profs")
    ResponseEntity<?> getAllProfs(){
        try {
            List<Professor> professors = professorRepository.findAll();
            return new ResponseEntity<>(professors, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
