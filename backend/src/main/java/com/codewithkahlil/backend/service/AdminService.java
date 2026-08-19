package com.codewithkahlil.backend.service;

import com.codewithkahlil.backend.auth.AuthenticationService;
import com.codewithkahlil.backend.auth.RegistreRequest;
import com.codewithkahlil.backend.dto.requests.filiereRequests;
import com.codewithkahlil.backend.dto.requests.moduleRequest;
import com.codewithkahlil.backend.dto.requests.profRequest;
import com.codewithkahlil.backend.dto.requests.studentRequest;
import com.codewithkahlil.backend.dto.response.AccountCreatedResponse;
import com.codewithkahlil.backend.dto.response.AvailableSlotResponse;
import com.codewithkahlil.backend.dto.response.FiliereResponse;
import com.codewithkahlil.backend.dto.response.ModuleResponse;
import com.codewithkahlil.backend.dto.response.OverviewFiliereResponse;
import com.codewithkahlil.backend.dto.response.StudentResponse;
import com.codewithkahlil.backend.exception.ConflictException;
import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.model.Filiere;
import com.codewithkahlil.backend.model.Module;
import com.codewithkahlil.backend.model.Professor;
import com.codewithkahlil.backend.model.Role;
import com.codewithkahlil.backend.model.Student;
import com.codewithkahlil.backend.model.TimeSlot;
import com.codewithkahlil.backend.model.User;
import com.codewithkahlil.backend.repository.FiliereRepository;
import com.codewithkahlil.backend.repository.ModuleRepository;
import com.codewithkahlil.backend.repository.ProfessorRepository;
import com.codewithkahlil.backend.repository.StudentRepository;
import com.codewithkahlil.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FiliereRepository filiereRepository;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;

    public Filiere creerFiliere(filiereRequests request) {
        String name = request.getName().toLowerCase();
        Filiere existing = filiereRepository.findByName(name);
        if (existing != null) {
            throw new ConflictException("filiere existe deja");
        }

        Filiere filiere = Filiere.builder()
                .name(name)
                .description(request.getDescription())
                .build();

        return filiereRepository.save(filiere);
    }

    public List<FiliereResponse> getAllFiliere() {
        return filiereRepository.findAll().stream()
                .map(f -> FiliereResponse.builder()
                        .id(f.getId())
                        .name(f.getName())
                        .description(f.getDescription())
                        .build())
                .toList();
    }

    public void deleteFiliere(int id) {
        filiereRepository.deleteById(id);
    }

    public AccountCreatedResponse creerProf(profRequest request) {
        String name = request.getName().toLowerCase();
        String prenom = request.getPrenom().toLowerCase();
        String email = name + prenom + "@test.com";
        String password = name + prenom + "123";

        User existing = userRepository.findUserByEmail(email);
        if (existing != null) {
            throw new ConflictException("un professeur avec cet email existe deja");
        }

        RegistreRequest registre = RegistreRequest.builder()
                .email(email)
                .password(password)
                .role(Role.Professor)
                .build();

        authenticationService.register(registre);

        User userCreated = userRepository.findUserByEmail(email);

        Professor professor = (Professor) Professor.builder()
                .name(name)
                .prenom(prenom)
                .user(userCreated)
                .build();

        professorRepository.save(professor);

        return AccountCreatedResponse.builder()
                .email(email)
                .password(password)
                .build();
    }

    public List<Professor> getAllProfs() {
        return professorRepository.findAll();
    }

    public void deleteProf(int id) {
        professorRepository.deleteById(id);
    }

    public void updateProf(int id, profRequest request) {
        Professor professor = professorRepository.findById(id);
        if (professor == null) {
            throw new NotFoundException("professeur introuvable");
        }

        professor.setName(request.getName().toLowerCase());
        professor.setPrenom(request.getPrenom().toLowerCase());
        professorRepository.save(professor);
    }

    public AccountCreatedResponse creerStudent(studentRequest request) {
        String email = request.getName() + request.getPrenom() + "@test.com";
        String password = "123456";

        if (studentRepository.findByCne(request.getCne()) != null) {
            throw new ConflictException("un etudiant avec ce cne existe deja");
        }

        RegistreRequest registre = RegistreRequest.builder()
                .email(email)
                .password(password)
                .role(Role.Student)
                .build();

        authenticationService.register(registre);

        User user = userRepository.findUserByEmail(email);
        Filiere filiere = filiereRepository.findById(request.getFiliereId());

        Student student = Student.builder()
                .cni(request.getCni())
                .cne(request.getCne())
                .prenom(request.getPrenom())
                .name(request.getName())
                .user(user)
                .filiere(filiere)
                .build();

        studentRepository.save(student);

        return AccountCreatedResponse.builder()
                .email(email)
                .password(password)
                .build();
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toStudentResponse)
                .toList();
    }

    public void deleteStudent(int id) {
        studentRepository.deleteById(id);
    }

    public void updateStudent(int id, studentRequest request) {
        Student student = studentRepository.findById(id);
        if (student == null) {
            throw new NotFoundException("etudiant introuvable");
        }

        student.setName(request.getName());
        student.setPrenom(request.getPrenom());
        student.setCni(request.getCni());
        student.setCne(request.getCne());

        Filiere filiere = filiereRepository.findById(request.getFiliereId());
        student.setFiliere(filiere);

        studentRepository.save(student);
    }

    public Module createModule(moduleRequest request) {
        Filiere filiere = filiereRepository.findById(request.getFiliereId());
        Professor prof = professorRepository.findById(request.getProfId());

        Module module = Module.builder()
                .name(request.getName().toLowerCase())
                .description(request.getDescription().toLowerCase())
                .filiere(filiere)
                .teacher(prof)
                .dayOfWeek(request.getDayOfWeek())
                .timeSlot(request.getTimeSlot())
                .build();

        return moduleRepository.save(module);
    }

    public void deleteModule(int id) {
        moduleRepository.deleteById(id);
    }

    public List<ModuleResponse> listModules() {
        return moduleRepository.findAll().stream()
                .map(m -> ModuleResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .description(m.getDescription())
                        .profName(m.getTeacher().getPrenom())
                        .filiereName(m.getFiliere().getName())
                        .build())
                .toList();
    }

    public OverviewFiliereResponse getOverview(int filiereId) {
        Filiere filiere = filiereRepository.findById(filiereId);
        if (filiere == null) {
            throw new NotFoundException("filiere introuvable");
        }

        List<String> modules = filiere.getModules().stream().map(Module::getName).toList();
        List<String> professors = filiere.getModules().stream()
                .map(m -> m.getTeacher().getName() + " " + m.getTeacher().getPrenom())
                .toList();

        List<StudentResponse> etudiants = filiere.getStudents().stream()
                .map(this::toStudentResponse)
                .toList();

        return OverviewFiliereResponse.builder()
                .etudiants(etudiants)
                .professors(professors)
                .modules(modules)
                .build();
    }

    public List<AvailableSlotResponse> getAvailableSlots(int filiereId) {
        List<Module> existingModules = moduleRepository.findByFiliere_Id(filiereId);
        List<AvailableSlotResponse> availableSlots = new ArrayList<>();

        for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
            if (day == java.time.DayOfWeek.SUNDAY) continue;

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

        return availableSlots;
    }

    private StudentResponse toStudentResponse(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .prenom(s.getPrenom())
                .email(s.getUser().getEmail())
                .cni(s.getCni())
                .cne(s.getCne())
                .filiereId(s.getFiliere() != null ? s.getFiliere().getId() : 0)
                .filiereName(s.getFiliere() != null ? s.getFiliere().getName() : "")
                .build();
    }
}
