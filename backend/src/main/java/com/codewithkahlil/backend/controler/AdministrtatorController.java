package com.codewithkahlil.backend.controler;


import com.codewithkahlil.backend.dto.requests.filiereRequests;
import com.codewithkahlil.backend.model.Filiere;
import com.codewithkahlil.backend.repository.FiliereRepository;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class AdministrtatorController {

    private final FiliereRepository filiereRepository ;

    @PostMapping("filiere")
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
}
