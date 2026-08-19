package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.service.DirectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/director")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping("/conversations")
    ResponseEntity<?> getAllConversations() {
        try {
            return new ResponseEntity<>(directorService.getAllConversations(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stats")
    ResponseEntity<?> getStats() {
        try {
            return new ResponseEntity<>(directorService.getStats(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/filiere/{id}")
    ResponseEntity<?> getFiliereOverview(@PathVariable int id) {
        try {
            return new ResponseEntity<>(directorService.getFiliereOverview(id), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ml/{filiereId}")
    ResponseEntity<?> getRiskStudents(@PathVariable int filiereId) {
        try {
            return new ResponseEntity<>(directorService.getRiskStudents(filiereId), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/modules/{filiereId}")
    ResponseEntity<?> getModulesForFiliere(@PathVariable int filiereId) {
        try {
            return new ResponseEntity<>(directorService.getModulesForFiliere(filiereId), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/predictions/{moduleId}")
    ResponseEntity<?> getEarlyPredictions(@PathVariable int moduleId) {
        try {
            return new ResponseEntity<>(directorService.getEarlyPredictions(moduleId), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/filieres-overview")
    ResponseEntity<?> getAllFilieresOverview() {
        try {
            return new ResponseEntity<>(directorService.getAllFilieresOverview(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
