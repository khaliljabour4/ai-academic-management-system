package com.codewithkahlil.backend.controler;

import com.codewithkahlil.backend.dto.requests.UpdateProfileRequest;
import com.codewithkahlil.backend.exception.ConflictException;
import com.codewithkahlil.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    ResponseEntity<?> getProfile() {
        try {
            return new ResponseEntity<>(profileService.getProfile(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping
    ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            profileService.updateProfile(request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ConflictException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
