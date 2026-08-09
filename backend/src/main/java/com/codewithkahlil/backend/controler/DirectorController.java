package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.dto.response.DirectorConversationResponse;
import com.codewithkahlil.backend.dto.response.DirectorFiliereOverviewResponse;
import com.codewithkahlil.backend.dto.response.DirectorStatsResponse;
import com.codewithkahlil.backend.dto.response.StudentRiskResponse;
import com.codewithkahlil.backend.model.*;
import com.codewithkahlil.backend.model.Module;
import com.codewithkahlil.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/v1/director")
@RequiredArgsConstructor
public class DirectorController {

    private final ConversationRepository conversationRepository;
    private final FiliereRepository filiereRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final org.springframework.web.client.RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${ml.service.url}")
    private String mlServiceUrl;

    @GetMapping("/conversations")
    ResponseEntity<?> getAllConversations() {
        try {
            List<Conversation> conversations = conversationRepository.findAll();

            List<DirectorConversationResponse> response = conversations.stream().map(c ->
                    DirectorConversationResponse.builder()
                            .id(c.getId())
                            .title(c.getTitle())
                            .studentName(c.getStudent().getName() + " " + c.getStudent().getPrenom())
                            .moduleName(c.getModule().getName())
                            .messages(c.getMessages())
                            .build()
            ).toList();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stats")
    ResponseEntity<?> getStats() {
        try {
            List<Grade> grades = gradeRepository.findAll();

            double sum = 0;
            int count = 0;
            for (Grade g : grades) {
                if (g.getNoteGenerale() != null) {
                    sum += g.getNoteGenerale();
                    count++;
                }
            }

            DirectorStatsResponse response = DirectorStatsResponse.builder()
                    .filieresCount(filiereRepository.count())
                    .professorsCount(professorRepository.count())
                    .studentsCount(studentRepository.count())
                    .averageGrade(count > 0 ? sum / count : null)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/filiere/{id}")
    ResponseEntity<?> getFiliereOverview(@PathVariable int id) {
        try {
            Filiere filiere = filiereRepository.findById(id);

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

            DirectorFiliereOverviewResponse response = DirectorFiliereOverviewResponse.builder()
                    .students(studentRows)
                    .modules(moduleRows)
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ml/{filiereId}")
    ResponseEntity<?> getRiskStudents(@PathVariable int filiereId) {
        try {
            Filiere filiere = filiereRepository.findById(filiereId);
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

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Double predictSuccess(double note1, double note2, double note3) {
        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("note1", note1);
            body.put("note2", note2);
            body.put("note3", note3);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity =
                    new org.springframework.http.HttpEntity<>(body, headers);

            java.util.Map<String, Object> result = restTemplate.postForObject(mlServiceUrl, entity, java.util.Map.class);
            return result != null ? Double.valueOf(String.valueOf(result.get("successRate"))) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
