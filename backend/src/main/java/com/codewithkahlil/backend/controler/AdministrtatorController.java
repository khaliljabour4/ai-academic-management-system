package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.dto.requests.filiereRequests;
import com.codewithkahlil.backend.dto.requests.moduleRequest;
import com.codewithkahlil.backend.dto.requests.profRequest;
import com.codewithkahlil.backend.dto.requests.studentRequest;
import com.codewithkahlil.backend.exception.ConflictException;
import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.service.AdminService ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class AdministrtatorController {

    private final AdminService adminService;

    @PostMapping("/filiere")
    ResponseEntity<?> creerFiliere(@RequestBody filiereRequests request) {
        try {
            return new ResponseEntity<>(adminService.creerFiliere(request), HttpStatus.CREATED);
        } catch (ConflictException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/filiere")
    ResponseEntity<?> getAllFiliere() {
        try {
            return new ResponseEntity<>(adminService.getAllFiliere(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("filiere")
    ResponseEntity<?> deleteFiliereByID(@RequestParam int id) {
        try {
            adminService.deleteFiliere(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/prof")
    ResponseEntity<?> creerProf(@RequestBody profRequest request) {
        try {
            return new ResponseEntity<>(adminService.creerProf(request), HttpStatus.OK);
        } catch (ConflictException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/profs")
    ResponseEntity<?> getAllProfs() {
        try {
            return new ResponseEntity<>(adminService.getAllProfs(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/profs/delete/{id}")
    ResponseEntity<?> deleteProf(@PathVariable int id) {
        try {
            adminService.deleteProf(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/profs/{id}")
    ResponseEntity<?> updateProf(@PathVariable int id, @RequestBody profRequest request) {
        try {
            adminService.updateProf(id, request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/student")
    ResponseEntity<?> crateStiudent(@RequestBody studentRequest request) {
        try {
            return new ResponseEntity<>(adminService.creerStudent(request), HttpStatus.OK);
        } catch (ConflictException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/students")
    ResponseEntity<?> getAllStudent() {
        try {
            return new ResponseEntity<>(adminService.getAllStudents(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/students/delete")
    ResponseEntity<?> deleteStudent(@RequestParam int id) {
        try {
            adminService.deleteStudent(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/students/{id}")
    ResponseEntity<?> updateStudent(@PathVariable int id, @RequestBody studentRequest request) {
        try {
            adminService.updateStudent(id, request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/module")
    ResponseEntity<?> createModule(@RequestBody moduleRequest request) {
        try {
            adminService.createModule(request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/module/delete/{id}")
    ResponseEntity<?> deleteModule(@PathVariable int id) {
        try {
            adminService.deleteModule(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/modules")
    ResponseEntity<?> listAllModules() {
        try {
            return new ResponseEntity<>(adminService.listModules(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/overview/{filiereId}")
    ResponseEntity<?> getALLfiliereDetails(@PathVariable int filiereId) {
        try {
            return new ResponseEntity<>(adminService.getOverview(filiereId), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/available-slots/{filiereId}")
    ResponseEntity<?> getAvailableSlots(@PathVariable int filiereId) {
        try {
            return new ResponseEntity<>(adminService.getAvailableSlots(filiereId), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
