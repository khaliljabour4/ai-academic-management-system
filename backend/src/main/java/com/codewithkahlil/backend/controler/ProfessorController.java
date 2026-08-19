package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.dto.requests.GradeColumnRequest;
import com.codewithkahlil.backend.dto.requests.ModuleDescriptionRequest;
import com.codewithkahlil.backend.exception.ConflictException;
import com.codewithkahlil.backend.exception.ForbiddenException;
import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/professor")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    @GetMapping("/modules")
    ResponseEntity<?> getMyModules() {
        try {
            return new ResponseEntity<>(professorService.getMyModules(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/students/{moduleId}")
    ResponseEntity<?> getStudentsForModule(@PathVariable int moduleId) {
        try {
            return new ResponseEntity<>(professorService.getStudentsForModule(moduleId), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/grades")
    ResponseEntity<?> submitGradeColumn(@RequestBody GradeColumnRequest request) {
        try {
            professorService.submitGradeColumn(request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ConflictException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/module-description")
    ResponseEntity<?> saveModuleDescription(@RequestBody ModuleDescriptionRequest request) {
        try {
            professorService.saveModuleDescription(request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ForbiddenException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
