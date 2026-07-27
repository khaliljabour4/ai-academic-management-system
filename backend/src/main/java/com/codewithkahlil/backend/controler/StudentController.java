package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.dto.requests.AskQuestionRequest;
import com.codewithkahlil.backend.dto.response.ConversationDetailResponse;
import com.codewithkahlil.backend.dto.response.ConversationSummaryResponse;
import com.codewithkahlil.backend.dto.response.ScheduleEntryResponse;
import com.codewithkahlil.backend.dto.response.StudentModuleResponse;
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
@RequestMapping("api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;
    private final GradeRepository gradeRepository;
    private final ConversationRepository conversationRepository;

    private Student currentStudent() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return studentRepository.findByUser_Email(user.getEmail());
    }

    /**
     * Flat list of this student's weekly schedule - one entry per module in
     * their filiere. No Map anywhere: the frontend just loops through this
     * list and places each entry into the (day, timeSlot) cell it matches.
     */
    @GetMapping("/schedule")
    ResponseEntity<?> getMySchedule() {
        try {
            Student student = currentStudent();
            List<Module> modules = moduleRepository.findByFiliere_Id(student.getFiliere().getId());

            List<ScheduleEntryResponse> schedule = modules.stream().map(m ->
                    ScheduleEntryResponse.builder()
                            .dayOfWeek(m.getDayOfWeek())
                            .timeSlot(m.getTimeSlot())
                            .moduleName(m.getName())
                            .teacherName(m.getTeacher() != null
                                    ? m.getTeacher().getName() + " " + m.getTeacher().getPrenom()
                                    : "")
                            .build()
            ).toList();

            return new ResponseEntity<>(schedule, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** This student's modules, each with the professor's description and the student's own grade. */
    @GetMapping("/modules")
    ResponseEntity<?> getMyModules() {
        try {
            Student student = currentStudent();
            List<Module> modules = moduleRepository.findByFiliere_Id(student.getFiliere().getId());

            List<StudentModuleResponse> response = modules.stream().map(m -> {
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

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** The list of conversation titles this student has for one module - click one to open it. */
    @GetMapping("/conversations/{moduleId}")
    ResponseEntity<?> getMyConversations(@PathVariable int moduleId) {
        try {
            Student student = currentStudent();
            List<Conversation> conversations =
                    conversationRepository.findByStudent_IdAndModule_Id(student.getId(), moduleId);

            List<ConversationSummaryResponse> response = conversations.stream().map(c ->
                    ConversationSummaryResponse.builder()
                            .id(c.getId())
                            .title(c.getTitle())
                            .build()
            ).toList();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Opens one conversation - its full, ordered question/response list. */
    @GetMapping("/conversation/{id}")
    ResponseEntity<?> getConversation(@PathVariable int id) {
        try {
            Student student = currentStudent();
            Conversation conversation = conversationRepository.findById(id).orElseThrow();

            if (conversation.getStudent().getId() != student.getId()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ConversationDetailResponse response = ConversationDetailResponse.builder()
                    .id(conversation.getId())
                    .title(conversation.getTitle())
                    .messages(conversation.getMessages())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Asks a question in a conversation (creates the conversation on first
     * use). The actual AI call is intentionally left empty for now - see
     * generateAiResponse() below.
     */
    @PostMapping("/conversation/{moduleId}/ask")
    ResponseEntity<?> askQuestion(@PathVariable int moduleId, @RequestBody AskQuestionRequest request) {
        try {
            Student student = currentStudent();
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

            String response = generateAiResponse(request.getQuestion());

            conversation.addMessage(request.getQuestion(), response);
            conversationRepository.save(conversation);

            ConversationDetailResponse detail = ConversationDetailResponse.builder()
                    .id(conversation.getId())
                    .title(conversation.getTitle())
                    .messages(conversation.getMessages())
                    .build();

            return new ResponseEntity<>(detail, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * TODO: call FastAPI/LLM here and return its real answer.
     * Left empty on purpose - not implemented yet.
     */
    private String generateAiResponse(String question) {
        return "";
    }
}
