package com.codewithkahlil.backend.service;

import com.codewithkahlil.backend.dto.requests.GradeColumnRequest;
import com.codewithkahlil.backend.dto.requests.GradeEntry;
import com.codewithkahlil.backend.dto.requests.ModuleDescriptionRequest;
import com.codewithkahlil.backend.dto.response.ModuleResponse;
import com.codewithkahlil.backend.dto.response.StudentGradeRowResponse;
import com.codewithkahlil.backend.exception.ConflictException;
import com.codewithkahlil.backend.exception.ForbiddenException;
import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.model.Grade;
import com.codewithkahlil.backend.model.Module;
import com.codewithkahlil.backend.model.Professor;
import com.codewithkahlil.backend.model.Student;
import com.codewithkahlil.backend.model.User;
import com.codewithkahlil.backend.repository.GradeRepository;
import com.codewithkahlil.backend.repository.ModuleRepository;
import com.codewithkahlil.backend.repository.ProfessorRepository;
import com.codewithkahlil.backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final GradeRepository gradeRepository;

    public Professor getCurrentProfessor() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return professorRepository.findByUser_Email(user.getEmail());
    }

    public List<ModuleResponse> getMyModules() {
        Professor professor = getCurrentProfessor();
        List<Module> modules = moduleRepository.findByTeacher_Id(professor.getId());

        return modules.stream().map(m ->
                ModuleResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .description(m.getDescription())
                        .profName(professor.getPrenom())
                        .filiereName(m.getFiliere().getName())
                        .filiereId(m.getFiliere().getId())
                        .build()
        ).toList();
    }

    public List<StudentGradeRowResponse> getStudentsForModule(int moduleId) {
        Module module = moduleRepository.findById(moduleId);
        if (module == null) {
            throw new NotFoundException("module introuvable");
        }

        List<Student> students = module.getFiliere().getStudents();

        return students.stream().map(s -> {
            Grade grade = gradeRepository.findByStudent_IdAndModule_Id(s.getId(), moduleId);
            return StudentGradeRowResponse.builder()
                    .studentId(s.getId())
                    .name(s.getName())
                    .prenom(s.getPrenom())
                    .note1(grade != null ? grade.getNote1() : null)
                    .note2(grade != null ? grade.getNote2() : null)
                    .note3(grade != null ? grade.getNote3() : null)
                    .moyen(grade != null &&
                            grade.getNote1() != null &&
                            grade.getNote2() != null &&
                            grade.getNote3() != null ?
                            grade.getNoteGenerale() : null)
                    .build();
        }).toList();
    }

    public void submitGradeColumn(GradeColumnRequest request) {
        if (request.getEntries() == null || request.getEntries().isEmpty()) {
            throw new ConflictException("Aucune note a enregistrer");
        }

        for (GradeEntry entry : request.getEntries()) {
            if (entry.getValue() == null) {
                throw new ConflictException("La colonne contient une ligne vide - impossible de l'enregistrer");
            }
        }

        if (request.getColumn() < 1 || request.getColumn() > 3) {
            throw new ConflictException("Colonne invalide");
        }

        Module module = moduleRepository.findById(request.getModuleId());
        if (module == null) {
            throw new NotFoundException("module introuvable");
        }

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
    }

    public void saveModuleDescription(ModuleDescriptionRequest request) {
        Module module = moduleRepository.findById(request.getModuleId());
        if (module == null) {
            throw new NotFoundException("module introuvable");
        }

        Professor professor = getCurrentProfessor();
        if (module.getTeacher().getId() != professor.getId()) {
            throw new ForbiddenException("Ce module ne t'appartient pas");
        }

        module.setDescription(request.getDescription());
        moduleRepository.save(module);
    }
}
