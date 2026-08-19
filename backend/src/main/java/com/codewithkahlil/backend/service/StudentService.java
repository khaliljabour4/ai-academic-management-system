package com.codewithkahlil.backend.service;

import com.codewithkahlil.backend.dto.requests.AskQuestionRequest;
import com.codewithkahlil.backend.dto.response.ConversationDetailResponse;
import com.codewithkahlil.backend.dto.response.ConversationSummaryResponse;
import com.codewithkahlil.backend.dto.response.ScheduleEntryResponse;
import com.codewithkahlil.backend.dto.response.StudentModuleResponse;
import com.codewithkahlil.backend.exception.ForbiddenException;
import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.model.Conversation;
import com.codewithkahlil.backend.model.Grade;
import com.codewithkahlil.backend.model.Module;
import com.codewithkahlil.backend.model.Student;
import com.codewithkahlil.backend.model.User;
import com.codewithkahlil.backend.repository.ConversationRepository;
import com.codewithkahlil.backend.repository.GradeRepository;
import com.codewithkahlil.backend.repository.ModuleRepository;
import com.codewithkahlil.backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final GradeRepository gradeRepository;
    private final ConversationRepository conversationRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public Student getCurrentStudent() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return studentRepository.findByUser_Email(user.getEmail());
    }

    public List<ScheduleEntryResponse> getMySchedule() {
        Student student = getCurrentStudent();
        List<Module> modules = moduleRepository.findByFiliere_Id(student.getFiliere().getId());

        return modules.stream().map(m ->
                ScheduleEntryResponse.builder()
                        .dayOfWeek(m.getDayOfWeek())
                        .timeSlot(m.getTimeSlot())
                        .moduleName(m.getName())
                        .teacherName(m.getTeacher() != null
                                ? m.getTeacher().getName() + " " + m.getTeacher().getPrenom()
                                : "")
                        .build()
        ).toList();
    }

    public List<StudentModuleResponse> getMyModules() {
        Student student = getCurrentStudent();
        List<Module> modules = moduleRepository.findByFiliere_Id(student.getFiliere().getId());

        return modules.stream().map(m -> {
            Grade grade = gradeRepository.findByStudent_IdAndModule_Id(student.getId(), m.getId());
            return StudentModuleResponse.builder()
                    .moduleId(m.getId())
                    .name(m.getName())
                    .description(m.getDescription())
                    .teacherName(m.getTeacher() != null
                            ? m.getTeacher().getName() + " " + m.getTeacher().getPrenom()
                            : "")
                    .note1(grade != null ? grade.getNote1() : null)
                    .note2(grade != null ? grade.getNote2() : null)
                    .note3(grade != null ? grade.getNote3() : null)
                    .noteGenerale(grade != null ? grade.getNoteGenerale() : null)
                    .build();
        }).toList();
    }

    public List<ConversationSummaryResponse> getMyConversations(int moduleId) {
        Student student = getCurrentStudent();
        List<Conversation> conversations =
                conversationRepository.findByStudent_IdAndModule_Id(student.getId(), moduleId);

        return conversations.stream().map(c ->
                ConversationSummaryResponse.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .build()
        ).toList();
    }

    public ConversationDetailResponse getConversation(int id) {
        Student student = getCurrentStudent();
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("conversation introuvable"));

        if (conversation.getStudent().getId() != student.getId()) {
            throw new ForbiddenException("cette conversation ne t'appartient pas");
        }

        return ConversationDetailResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .messages(conversation.getMessages())
                .build();
    }

    public ConversationDetailResponse askQuestion(int moduleId, AskQuestionRequest request) {
        Student student = getCurrentStudent();
        Module module = moduleRepository.findById(moduleId);

        Conversation conversation = conversationRepository
                .findByStudent_IdAndModule_IdAndTitle(student.getId(), moduleId, request.getConversationTitle());

        if (conversation == null) {
            conversation = Conversation.builder()
                    .title(request.getConversationTitle())
                    .student(student)
                    .module(module)
                    .build();
        }

        String response = generateAiResponse(request.getQuestion(), conversation.getMessages());

        conversation.addMessage(request.getQuestion(), response);
        conversationRepository.save(conversation);

        return ConversationDetailResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .messages(conversation.getMessages())
                .build();
    }

    private String generateAiResponse(String question, List<String> history) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("question", question);
            body.put("history", history);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            Map<String, Object> result = restTemplate.postForObject(aiServiceUrl, entity, Map.class);
            return result != null ? String.valueOf(result.get("answer")) : "Pas de reponse disponible pour le moment";
        } catch (Exception e) {
            e.printStackTrace();
            return "Assistant IA indisponible pour le moment";
        }
    }
}
