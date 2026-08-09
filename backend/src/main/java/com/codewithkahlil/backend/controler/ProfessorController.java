package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.dto.requests.GradeColumnRequest;
import com.codewithkahlil.backend.dto.requests.GradeEntry;
import com.codewithkahlil.backend.dto.requests.ModuleDescriptionRequest;
import com.codewithkahlil.backend.dto.response.ModuleResponse;
import com.codewithkahlil.backend.dto.response.StudentGradeRowResponse;
import com.codewithkahlil.backend.model.*;
import com.codewithkahlil.backend.model.Module;
import com.codewithkahlil.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/professor")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final GradeRepository gradeRepository;
    private final FiliereRepository filiereRepository;

    private Professor currentProfessor() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return professorRepository.findByUser_Email(user.getEmail());
    }

    @GetMapping("/modules")
    ResponseEntity<?> getMyModules() {
        try {
            Professor professor = currentProfessor();
            List<Module> modules = moduleRepository.findByTeacher_Id(professor.getId());

            List<ModuleResponse> response = modules.stream().map(m ->
                    ModuleResponse.builder()
                            .id(m.getId())
                            .name(m.getName())
                            .description(m.getDescription())
                            .profName(professor.getPrenom())
                            .filiereName(m.getFiliere().getName())
                            .filiereId(m.getFiliere().getId())
                            .build()
            ).toList();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/students/{moduleId}")
    ResponseEntity<?> getStudentsForModule(@PathVariable int moduleId) {
        try {
            Module module = moduleRepository.findById(moduleId);
            List<Student> students = module.getFiliere().getStudents();
            List<StudentGradeRowResponse> rows = students.stream().map(s -> {
                Grade grade = gradeRepository.findByStudent_IdAndModule_Id(s.getId(), moduleId);
                return StudentGradeRowResponse.builder()
                        .studentId(s.getId())
                        .name(s.getName())
                        .prenom(s.getPrenom())
                        .note1(grade != null? grade.getNote1() : null)
                        .note2(grade != null? grade.getNote2() : null)
                        .note3(grade != null? grade.getNote3() : null)
                        .moyen(grade!=null &&
                                grade.getNote1()!=null &&
                                grade.getNote2() != null &&
                                grade.getNote3() != null?
                                grade.getNoteGenerale() : null)
                        .build();
            }).toList();

            return new ResponseEntity<>(rows, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/grades")
    ResponseEntity<?> submitGradeColumn(@RequestBody GradeColumnRequest request) {

        if (request.getEntries() == null || request.getEntries().isEmpty()) {
            return new ResponseEntity<>("Aucune note à enregistrer", HttpStatus.BAD_REQUEST);
        }

        // Validate first - if ANY row in this column is empty, reject the whole column.
        for (GradeEntry entry : request.getEntries()) {
            if (entry.getValue() == null) {
                return new ResponseEntity<>(
                        "La colonne contient une ligne vide - impossible de l'enregistrer",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        if (request.getColumn() < 1 || request.getColumn() > 3) {
            return new ResponseEntity<>("Colonne invalide", HttpStatus.BAD_REQUEST);
        }

        try {
            Module module = moduleRepository.findById(request.getModuleId());

            for (GradeEntry entry : request.getEntries()) {
                Grade grade = gradeRepository.findByStudent_IdAndModule_Id(entry.getStudentId(), request.getModuleId());

                if (grade == null) {
                    Student student = studentRepository.findById(entry.getStudentId());
                    grade = Grade.builder()
                            .student(student)
                            .module(module)
                            .build();
                }

                switch (request.getColumn()) {
                    case 1 -> grade.setNote1(entry.getValue());
                    case 2 -> grade.setNote2(entry.getValue());
                    case 3 -> grade.setNote3(entry.getValue());
                }

                grade.computeNoteGenerale();
                gradeRepository.save(grade);
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Saves the course resume/description for a module - visible to its filiere's students. */
    @PostMapping("/module-description")
    ResponseEntity<?> saveModuleDescription(@RequestBody ModuleDescriptionRequest request) {
        try {
            Module module = moduleRepository.findById(request.getModuleId());

            Professor professor = currentProfessor();
            if (module.getTeacher().getId() != professor.getId()) {
                return new ResponseEntity<>("Ce module ne t'appartient pas", HttpStatus.FORBIDDEN);
            }

            module.setDescription(request.getDescription());
            moduleRepository.save(module);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
