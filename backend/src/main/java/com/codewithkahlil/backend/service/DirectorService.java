package com.codewithkahlil.backend.service;

import com.codewithkahlil.backend.dto.response.DirectorConversationResponse;
import com.codewithkahlil.backend.dto.response.DirectorFiliereOverviewResponse;
import com.codewithkahlil.backend.dto.response.DirectorStatsResponse;
import com.codewithkahlil.backend.dto.response.EarlyPredictionResponse;
import com.codewithkahlil.backend.dto.response.FiliereSummaryResponse;
import com.codewithkahlil.backend.dto.response.ModuleResponse;
import com.codewithkahlil.backend.dto.response.StudentRiskResponse;
import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.model.Conversation;
import com.codewithkahlil.backend.model.Filiere;
import com.codewithkahlil.backend.model.Grade;
import com.codewithkahlil.backend.model.Module;
import com.codewithkahlil.backend.model.Student;
import com.codewithkahlil.backend.repository.ConversationRepository;
import com.codewithkahlil.backend.repository.FiliereRepository;
import com.codewithkahlil.backend.repository.GradeRepository;
import com.codewithkahlil.backend.repository.ModuleRepository;
import com.codewithkahlil.backend.repository.ProfessorRepository;
import com.codewithkahlil.backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final ConversationRepository conversationRepository;
    private final FiliereRepository filiereRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final ModuleRepository moduleRepository;
    private final RestTemplate restTemplate;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    @Value("${ml.progress.url}")
    private String mlProgressUrl;

    public List<DirectorConversationResponse> getAllConversations() {
        List<Conversation> conversations = conversationRepository.findAll();

        return conversations.stream().map(c ->
                DirectorConversationResponse.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .studentName(c.getStudent().getName() + " " + c.getStudent().getPrenom())
                        .moduleName(c.getModule().getName())
                        .messages(c.getMessages())
                        .build()
        ).toList();
    }

    public DirectorStatsResponse getStats() {
        List<Grade> grades = gradeRepository.findAll();

        double sum = 0;
        int count = 0;
        for (Grade g : grades) {
            if (g.getNoteGenerale() != null) {
                sum += g.getNoteGenerale();
                count++;
            }
        }

        return DirectorStatsResponse.builder()
                .filieresCount(filiereRepository.count())
                .professorsCount(professorRepository.count())
                .studentsCount(studentRepository.count())
                .averageGrade(count > 0 ? sum / count : null)
                .build();
    }

    public DirectorFiliereOverviewResponse getFiliereOverview(int id) {
        Filiere filiere = filiereRepository.findById(id);
        if (filiere == null) {
            throw new NotFoundException("filiere introuvable");
        }

        List<DirectorFiliereOverviewResponse.StudentRow> studentRows = new ArrayList<>();
        for (Student s : filiere.getStudents()) {
            List<Grade> studentGrades = new ArrayList<>();
            for (Module m : filiere.getModules()) {
                Grade g = gradeRepository.findByStudent_IdAndModule_Id(s.getId(), m.getId());
                if (g != null) studentGrades.add(g);
            }

            double sum = 0;
            int count = 0;
            for (Grade g : studentGrades) {
                if (g.getNoteGenerale() != null) {
                    sum += g.getNoteGenerale();
                    count++;
                }
            }

            studentRows.add(DirectorFiliereOverviewResponse.StudentRow.builder()
                    .name(s.getName())
                    .prenom(s.getPrenom())
                    .cne(s.getCne())
                    .moyenneGenerale(count > 0 ? sum / count : null)
                    .build());
        }

        List<DirectorFiliereOverviewResponse.ModuleRow> moduleRows = new ArrayList<>();
        for (Module m : filiere.getModules()) {
            List<Grade> moduleGrades = gradeRepository.findByModule_Id(m.getId());

            double sum = 0;
            int count = 0;
            for (Grade g : moduleGrades) {
                if (g.getNoteGenerale() != null) {
                    sum += g.getNoteGenerale();
                    count++;
                }
            }

            moduleRows.add(DirectorFiliereOverviewResponse.ModuleRow.builder()
                    .name(m.getName())
                    .profName(m.getTeacher() != null ? m.getTeacher().getName() + " " + m.getTeacher().getPrenom() : "")
                    .moyenneClasse(count > 0 ? sum / count : null)
                    .build());
        }

        return DirectorFiliereOverviewResponse.builder()
                .students(studentRows)
                .modules(moduleRows)
                .build();
    }

    public List<StudentRiskResponse> getRiskStudents(int filiereId) {
        Filiere filiere = filiereRepository.findById(filiereId);
        if (filiere == null) {
            throw new NotFoundException("filiere introuvable");
        }

        List<StudentRiskResponse> response = new ArrayList<>();

        for (Student s : filiere.getStudents()) {
            List<Grade> grades = new ArrayList<>();
            for (Module m : filiere.getModules()) {
                Grade g = gradeRepository.findByStudent_IdAndModule_Id(s.getId(), m.getId());
                if (g != null && g.getNote1() != null && g.getNote2() != null && g.getNote3() != null) {
                    grades.add(g);
                }
            }

            if (grades.isEmpty()) continue;

            double n1 = 0, n2 = 0, n3 = 0;
            for (Grade g : grades) {
                n1 += g.getNote1();
                n2 += g.getNote2();
                n3 += g.getNote3();
            }
            n1 /= grades.size();
            n2 /= grades.size();
            n3 /= grades.size();

            Double successRate = predictSuccess(n1, n2, n3);

            response.add(StudentRiskResponse.builder()
                    .name(s.getName())
                    .prenom(s.getPrenom())
                    .successRate(successRate)
                    .atRisk(successRate != null && successRate < 50)
                    .build());
        }

        return response;
    }

    public List<ModuleResponse> getModulesForFiliere(int filiereId) {
        Filiere filiere = filiereRepository.findById(filiereId);
        if (filiere == null) {
            throw new NotFoundException("filiere introuvable");
        }

        return moduleRepository.findByFiliere_Id(filiereId).stream()
                .map(m -> ModuleResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .description(m.getDescription())
                        .filiereName(filiere.getName())
                        .filiereId(filiere.getId())
                        .profName(m.getTeacher() != null ? m.getTeacher().getName() + " " + m.getTeacher().getPrenom() : "")
                        .build())
                .toList();
    }

    public List<EarlyPredictionResponse> getEarlyPredictions(int moduleId) {
        Module module = moduleRepository.findById(moduleId);
        if (module == null) {
            throw new NotFoundException("module introuvable");
        }

        List<EarlyPredictionResponse> response = new ArrayList<>();

        for (Student s : module.getFiliere().getStudents()) {
            Grade grade = gradeRepository.findByStudent_IdAndModule_Id(s.getId(), moduleId);
            if (grade == null || grade.getNote1() == null) {
                continue;
            }

            Map<String, Object> progress = predictProgress(grade.getNote1(), grade.getNote2(), grade.getNote3());

            Double successRate = progress != null ? Double.valueOf(String.valueOf(progress.get("successRate"))) : null;
            int notesUsed = progress != null ? Integer.parseInt(String.valueOf(progress.get("notesUsed"))) : 1;
            String confidence = progress != null ? String.valueOf(progress.get("confidence")) : "indisponible";

            response.add(EarlyPredictionResponse.builder()
                    .studentId(s.getId())
                    .name(s.getName())
                    .prenom(s.getPrenom())
                    .note1(grade.getNote1())
                    .note2(grade.getNote2())
                    .note3(grade.getNote3())
                    .successRate(successRate)
                    .notesUsed(notesUsed)
                    .confidence(confidence)
                    .atRisk(successRate != null && successRate < 50)
                    .build());
        }

        return response;
    }

    public List<FiliereSummaryResponse> getAllFilieresOverview() {
        List<FiliereSummaryResponse> response = new ArrayList<>();

        for (Filiere filiere : filiereRepository.findAll()) {
            double sum = 0;
            int gradedCount = 0;
            int atRiskCount = 0;

            for (Student s : filiere.getStudents()) {
                double studentSum = 0;
                int studentCount = 0;
                for (Module m : filiere.getModules()) {
                    Grade g = gradeRepository.findByStudent_IdAndModule_Id(s.getId(), m.getId());
                    if (g != null && g.getNoteGenerale() != null) {
                        studentSum += g.getNoteGenerale();
                        studentCount++;
                    }
                }
                if (studentCount > 0) {
                    double studentAverage = studentSum / studentCount;
                    sum += studentAverage;
                    gradedCount++;
                    if (studentAverage < 10) {
                        atRiskCount++;
                    }
                }
            }

            long professorsCount = filiere.getModules().stream()
                    .map(Module::getTeacher)
                    .filter(t -> t != null)
                    .distinct()
                    .count();

            response.add(FiliereSummaryResponse.builder()
                    .filiereId(filiere.getId())
                    .filiereName(filiere.getName())
                    .studentsCount(filiere.getStudents().size())
                    .professorsCount((int) professorsCount)
                    .averageGrade(gradedCount > 0 ? sum / gradedCount : null)
                    .atRiskCount(atRiskCount)
                    .gradedCount(gradedCount)
                    .build());
        }

        return response;
    }

    private Map<String, Object> predictProgress(Double note1, Double note2, Double note3) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("note1", note1);
            if (note2 != null) body.put("note2", note2);
            if (note3 != null) body.put("note3", note3);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            return restTemplate.postForObject(mlProgressUrl, entity, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Double predictSuccess(double note1, double note2, double note3) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("note1", note1);
            body.put("note2", note2);
            body.put("note3", note3);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            Map<String, Object> result = restTemplate.postForObject(mlServiceUrl, entity, Map.class);
            return result != null ? Double.valueOf(String.valueOf(result.get("successRate"))) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
