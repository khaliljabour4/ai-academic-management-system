package com.codewithkahlil.backend.service;

import com.codewithkahlil.backend.dto.requests.UpdateProfileRequest;
import com.codewithkahlil.backend.dto.response.ProfileResponse;
import com.codewithkahlil.backend.exception.ConflictException;
import com.codewithkahlil.backend.exception.NotFoundException;
import com.codewithkahlil.backend.model.Administrator;
import com.codewithkahlil.backend.model.Director;
import com.codewithkahlil.backend.model.Professor;
import com.codewithkahlil.backend.model.Student;
import com.codewithkahlil.backend.model.User;
import com.codewithkahlil.backend.repository.AdministratorRepository;
import com.codewithkahlil.backend.repository.DirectorRepository;
import com.codewithkahlil.backend.repository.ProfessorRepository;
import com.codewithkahlil.backend.repository.StudentRepository;
import com.codewithkahlil.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final DirectorRepository directorRepository;
    private final AdministratorRepository administratorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public ProfileResponse getProfile() {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case Student -> {
                Student student = studentRepository.findByUser_Email(user.getEmail());
                yield ProfileResponse.builder()
                        .id(student.getId())
                        .name(student.getName())
                        .prenom(student.getPrenom())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .cni(student.getCni())
                        .cne(student.getCne())
                        .filiereName(student.getFiliere() != null ? student.getFiliere().getName() : "")
                        .build();
            }
            case Professor -> {
                Professor professor = professorRepository.findByUser_Email(user.getEmail());
                yield ProfileResponse.builder()
                        .id(professor.getId())
                        .name(professor.getName())
                        .prenom(professor.getPrenom())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .number(professor.getNumber())
                        .build();
            }
            case Director -> {
                Director director = directorRepository.findByUser_Email(user.getEmail());
                yield ProfileResponse.builder()
                        .id(director.getId())
                        .name(director.getName())
                        .prenom(director.getPrenom())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build();
            }
            case Administrator -> {
                Administrator administrator = administratorRepository.findByUser_Email(user.getEmail());
                yield ProfileResponse.builder()
                        .id(administrator.getId())
                        .name(administrator.getName())
                        .prenom(administrator.getPrenom())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .department(administrator.getDepartment())
                        .build();
            }
        };
    }

    public void updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        switch (user.getRole()) {
            case Student -> {
                Student student = studentRepository.findByUser_Email(user.getEmail());
                applyNameChange(student, request);
                studentRepository.save(student);
            }
            case Professor -> {
                Professor professor = professorRepository.findByUser_Email(user.getEmail());
                applyNameChange(professor, request);
                professorRepository.save(professor);
            }
            case Director -> {
                Director director = directorRepository.findByUser_Email(user.getEmail());
                applyNameChange(director, request);
                directorRepository.save(director);
            }
            case Administrator -> {
                Administrator administrator = administratorRepository.findByUser_Email(user.getEmail());
                applyNameChange(administrator, request);
                administratorRepository.save(administrator);
            }
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            changePassword(user, request);
        }
    }

    private void applyNameChange(com.codewithkahlil.backend.model.Personne personne, UpdateProfileRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            personne.setName(request.getName());
        }
        if (request.getPrenom() != null && !request.getPrenom().isBlank()) {
            personne.setPrenom(request.getPrenom());
        }
    }

    private void changePassword(User user, UpdateProfileRequest request) {
        if (request.getCurrentPassword() == null ||
                !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ConflictException("mot de passe actuel incorrect");
        }

        User freshUser = userRepository.findUserByEmail(user.getEmail());
        if (freshUser == null) {
            throw new NotFoundException("utilisateur introuvable");
        }

        freshUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(freshUser);
    }
}
