package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.dto.requests.AskQuestionRequest;
import com.codewithkahlil.backend.exception.ForbiddenException;
import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/schedule")
    ResponseEntity<?> getMySchedule() {
        try {
            return new ResponseEntity<>(studentService.getMySchedule(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/modules")
    ResponseEntity<?> getMyModules() {
        try {
            return new ResponseEntity<>(studentService.getMyModules(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/conversations/{moduleId}")
    ResponseEntity<?> getMyConversations(@PathVariable int moduleId) {
        try {
            return new ResponseEntity<>(studentService.getMyConversations(moduleId), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/conversation/{id}")
    ResponseEntity<?> getConversation(@PathVariable int id) {
        try {
            return new ResponseEntity<>(studentService.getConversation(id), HttpStatus.OK);
        } catch (ForbiddenException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/conversation/{moduleId}/ask")
    ResponseEntity<?> askQuestion(@PathVariable int moduleId, @RequestBody AskQuestionRequest request) {
        try {
            return new ResponseEntity<>(studentService.askQuestion(moduleId, request), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
