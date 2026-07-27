package com.codewithkahlil.backend.controler;


import com.codewithkahlil.backend.auth.AuthenticationService;
import com.codewithkahlil.backend.auth.RegistreRequest;
import com.codewithkahlil.backend.dto.requests.filiereRequests;
import com.codewithkahlil.backend.dto.requests.moduleRequest;
import com.codewithkahlil.backend.dto.requests.profRequest;
import com.codewithkahlil.backend.dto.requests.studentRequest;
import com.codewithkahlil.backend.dto.response.*;
import com.codewithkahlil.backend.dto.response.StudentResponse;
import com.codewithkahlil.backend.model.*;
import com.codewithkahlil.backend.model.Module;
import com.codewithkahlil.backend.repository.*;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
import org.hibernate.dialect.SybaseASEDialect;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.data.web.ReactiveOffsetScrollPositionHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class AdministrtatorController {

    private final FiliereRepository filiereRepository ;
    private final AuthenticationService authenticationService ;
    private final UserRepository userRepository ;
    private final  ProfessorRepository professorRepository ;
    private final StudentRepository studentRepository ;
    private final ModuleRepository moduleRepository ;

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
        List<FiliereResponse> filieres  = new ArrayList<>();
        try{
            filieres = filiereRepository.findAll().stream()
                    .map(f -> FiliereResponse.builder()
                                    .id(f.getId())
                                    .name(f.getName())
                                    .description(f.getDescription())
                                    .build()
                    ).toList();
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

    //student
    @PostMapping("/student")
    ResponseEntity<?> crateStiudent(@RequestBody studentRequest request){
        String email = request.getName() +request.getPrenom() +"@test.com";
         RegistreRequest registre = RegistreRequest.builder()
                 .email(email)
                 .password("123456")
                 .role(Role.Student)
                 .build() ;
         if(studentRepository.findByCne(request.getCne()) != null){
             return new ResponseEntity<>(HttpStatus.CONFLICT) ;
         }
         try{
             authenticationService.register(registre) ;
         }catch(Exception e){
             return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR) ;
         }
         try{
             User user = userRepository.findUserByEmail(email) ;
             Filiere filiere = filiereRepository.findById(request.getFiliereID());
             Student student = Student.builder()
                     .cni(request.getCni())
                     .cne(request.getCne())
                     .prenom(request.getPrenom())
                     .name(request.getName())
                     .user(user)
                     .filiere(filiere).build();

             studentRepository.save(student) ;
         }catch (Exception e ){
             return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
         }

         return new ResponseEntity<>(HttpStatus.OK);

    }


    @GetMapping("/students")
    ResponseEntity<?> getAllStudent(){
        try{
            List<StudentResponse> students = studentRepository.findAll()
                    .stream().map(
                            m-> {
                                return StudentResponse.builder()
                                        .name(m.getName())
                                        .prenom(m.getPrenom())
                                        .email(m.getUser().getEmail())
                                        .id(m.getId())
                                        .build();
                            }
                    ).toList();
            return new ResponseEntity<>(students , HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //module
    @PostMapping("/module")
    ResponseEntity<?> createModule(@RequestBody moduleRequest request){

        try{
            Filiere  filiere = filiereRepository.findById(request.getFiliereId());
            Professor prof = professorRepository.findById(request.getProfId());

            Module module = Module.builder()
                    .name(request.getName().toLowerCase())
                    .description(request.getDescription().toLowerCase())
                    .filiere(filiere)
                    .teacher(prof)
                    .dayOfWeek(request.getDayOfWeek())
                    .timeSlot(request.getTimeSlot())
                    .build();

            moduleRepository.save(module);

            System.out.println( "nice");
        }catch(Exception e ){
            e.printStackTrace();
            System.out.println( "not nice");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/modules")
    ResponseEntity<?> listAllModules(){
        try{
            List<Module> modules = moduleRepository.findAll();
            List<ModuleResponse> modulesResponse = modules.stream().map(
                    (m) ->ModuleResponse.builder()
                            .id(m.getId())
                            .name(m.getName())
                            .description(m.getDescription())
                            .profName(m.getTeacher().getPrenom())
                            .filiereName(m.getFiliere().getName())
                            .build()
            ).toList();

            return new ResponseEntity<>(modulesResponse ,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    //overview
    @GetMapping("/overview/{filiereId}")
    ResponseEntity<?> getALLfiliereDetails(@PathVariable int filiereId){
        Filiere filiere = filiereRepository.findById(filiereId);
        List<String> modules = filiere.getModules().stream().map(m -> m.getName()).toList();
        List<String> professors = filiere.getModules().stream()
                .map(m->m.getTeacher().getName()+" "+m.getTeacher().getPrenom())
                .toList();
        try{
            OverviewFiliereResponse overview= OverviewFiliereResponse.builder()
                    .etudiants(filiere.getStudents().stream()
                            .map(e->StudentResponse.builder()
                                    .id(e.getId())
                                    .name(e.getName())
                                    .prenom(e.getPrenom())
                                    .email(e.getUser().getEmail())
                                    .build()
                            ).toList()
                    )
                    .professors(professors)
                    .modules(modules)
                    .build();

            return new ResponseEntity<>(overview , HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/available-slots/{filiereId}")
    ResponseEntity<?> getAvailableSlots(@PathVariable int filiereId) {
        try {
            List<Module> existingModules = moduleRepository.findByFiliere_Id(filiereId);

            List<AvailableSlotResponse> availableSlots = new ArrayList<>();

            for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
                if (day == java.time.DayOfWeek.SUNDAY) continue; // no classes on Sunday

                for (TimeSlot slot : TimeSlot.values()) {

                    boolean taken = false;
                    for (Module module : existingModules) {
                        if (module.getDayOfWeek() == day && module.getTimeSlot() == slot) {
                            taken = true;
                            break;
                        }
                    }

                    if (!taken) {
                        availableSlots.add(AvailableSlotResponse.builder()
                                .dayOfWeek(day)
                                .timeSlot(slot)
                                .build());
                    }
                }
            }

            return new ResponseEntity<>(availableSlots, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
