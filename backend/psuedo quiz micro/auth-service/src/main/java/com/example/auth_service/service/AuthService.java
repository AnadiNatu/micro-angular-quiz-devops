package com.example.auth_service.service;

import com.example.auth_service.dto.*;
import com.example.auth_service.entity.Users;
import com.example.auth_service.enums.UserRoles;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ================= ADMIN INIT =================
    @PostConstruct
    public void createAdminAccount() {

        if (userRepository.findByUsernameIgnoreCase("admin").isEmpty()) {

            Users admin = new Users();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(UserRoles.ADMIN);
            admin.setEnabled(true);

            Users saved = userRepository.save(admin);

            saved.setAuthServiceId(saved.getId());
            userRepository.save(saved);
            log.info("[AUTH-SERVICE] Admin account created");
        } else {
            log.debug("[AUTH-SERVICE] Admin account already exists, skipping creation");
        }
    }

    // ================= SIGNUP =================
    public SignUpResponseDTO signup(SignUpRequestDTO request) {

        if (userRepository.findByUsernameIgnoreCase(request.getUsername()).isPresent()) {
            log.warn("[AUTH-SERVICE] Signup failed - username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        Users user = UserMapper.toEntity(request);

        // Encode password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Default role
//        user.setRoles(UserRoles.PARTICIPANT);

//        No more default role
        UserRoles role = UserRoles.PARTICIPANT;
        if (request.getRole() != null && !request.getRole().isBlank()){
         try {
             role = UserRoles.valueOf(request.getRole().toUpperCase());
         }catch (IllegalArgumentException ex){
             log.warn("[AUTH-SERVICE] Unknown role '{}', defaulting to PARTICIPANT", request.getRole());
         }
         }
        user.setRoles(role);

        Users savedUser = userRepository.save(user);

        savedUser.setAuthServiceId(savedUser.getId());
        userRepository.save(savedUser);

        log.info("[AUTH-SERVICE] New user registered: username={}, role={}", savedUser.getUsername(), savedUser.getRoles());
        return UserMapper.toSignUpResponse(savedUser);
    }

    // ================= LOGIN =================
    public LoginResponseDTO login(LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        Users user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtils.generateToken(user);
        log.info("[AUTH-SERVICE] Login successful: username={}, role={}", user.getUsername(), user.getRoles());
        return UserMapper.toLoginResponse(user, token);
    }

    // ================= GET USER =================
    public UserDto getUserById(Long id) {
        log.debug("[AUTH-SERVICE] Getting User By Id");
        return userRepository.findById(id)
                .map(UserMapper::toDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

//    public List<UserDto> getAllUsers() {
//        List<UserDto> users = userRepository.findAll()
//                .stream()
//                .map(UserMapper::toDTO)
//                .toList();
//
//        log.debug("[AUTH] Fetched all users, count={}", users.size());
//        return  users;
//    }

//    Feign lookup by authServiceId
public UserDto getUserByAuthServiceId(Long authServiceId) {
    return userRepository.findByAuthServiceId(authServiceId)
            .map(UserMapper::toDTO)
            .orElseThrow(() -> new UsernameNotFoundException("User not found for authServiceId=" + authServiceId));
}

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toDTO).toList();
    }

    // Stat sync - called by {quiz/question} service

    public void incrementStat(UserStatSyncDTO dto){
        Users user = userRepository.findByAuthServiceId(dto.getAuthServiceId()).orElseThrow(() -> new RuntimeException("User not found"));

        switch (dto.getStatType()) {
            case "QUESTION_CREATED" -> user.setQuestionsCreatedCount(user.getQuestionsCreatedCount() + 1);
            case "QUIZ_CREATED" -> user.setQuizzesCreatedCount(user.getQuizzesCreatedCount() + 1);
            case "QUIZ_TAKEN" -> user.setQuizzesTakenCount(user.getQuizzesTakenCount() + 1);
            default -> log.warn("[AUTH] Unknown statType={}", dto.getStatType());
        }

        userRepository.save(user);
        log.info("[AUTH] Stat synced: authServiceId={}, statType={}", dto.getAuthServiceId(), dto.getStatType());
    }

    // ================= RESET PASSWORD =================
    public void sendResetToken(String email) {

        Users user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtils.generateToken(user);

        user.setResetToken(token);
        userRepository.save(user);

        // 👉 Hook your email service here
        System.out.println("Reset Token: " + token);
        log.info("[AUTH-SERVICE] Password reset token generated for email={}", email);
    }

    public String resetPassword(String email, String token, String newPassword) {

        Users user = userRepository.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtUtils.isTokenValid(token, user)) {
            throw new RuntimeException("Invalid or expired token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);

        userRepository.save(user);

        log.info("[AUTH] Password reset successful for email={}", email);
        return "Password reset successful";
    }

    // ================= RESET PASSWORD =================
    public UserDto disableUser(Long id) {

        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(false);

        Users saved = userRepository.save(user);
        log.warn("[AUTH] User disabled: userId={}, username={}", id, saved.getUsername());
        return UserMapper.toDTO(saved);
    }
}
