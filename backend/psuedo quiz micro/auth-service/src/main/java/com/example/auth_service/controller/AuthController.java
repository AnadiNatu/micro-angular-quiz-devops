package com.example.auth_service.controller;


import com.example.auth_service.dto.*;
import com.example.auth_service.security.JwtUtils;
import com.example.auth_service.service.AuthService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    // PUBLIC
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDTO> signup(@RequestBody SignUpRequestDTO request) {
        log.info("[AUTH] Signup request for username={}", request.getUsername());
        return ResponseEntity.ok(authService.signup(request));
    }

    // PUBLIC
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        log.info("[AUTH] Login attempt for username={}", request.getUsername());
        return ResponseEntity.ok(authService.login(request));
    }

    // FEIGN ACCESS + OWNER CHECK
    @GetMapping("/{id}")
    @PostAuthorize("returnObject.body.id == authentication.principal.id OR hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        log.info("[AUTH] Get User Id = {}", id);
        return ResponseEntity.ok(authService.getUserById(id));
    }

    // ADMIN ONLY
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.debug("[AUTH] Admin fetching all users");
        return ResponseEntity.ok(authService.getAllUsers());
    }

    // PUBLIC
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        log.info("[AUTH] Password reset requested for email={}", email);
        authService.sendResetToken(email);
        return ResponseEntity.ok("Reset token sent");
    }

    // PUBLIC
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @RequestParam String token,
            @RequestParam String newPassword) {
        log.info("[AUTH] Password reset attempt for email={}", email);

        return ResponseEntity.ok(
                authService.resetPassword(email, token, newPassword)
        );
    }

    // FEIGN / GATEWAY
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        log.info("[AUTH] Validate token");
        return ResponseEntity.ok(jwtUtils.isTokenValid(token));
    }

    // DISABLE USER (SOFT DELETE)
    @PutMapping("/disable/{id}")
    @PostAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDto> disableUser(@PathVariable Long id) {
        log.warn("[AUTH] Disable user request for userId={}", id);
        return ResponseEntity.ok(authService.disableUser(id));
    }

    // NOTIFICATION FEIGN
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id){
        log.warn("[AUTH - FEIGN] Notification Service");
        return ResponseEntity.ok(authService.getUserById(id));
    }

    // FEIGN INTERNAL - fetch user (Quiz/Question Service)
    @GetMapping("/internal/auth-id/{authServiceId}")
    public ResponseEntity<UserDto> getUserByAuthServiceId(@PathVariable Long authServiceId) {
        log.debug("[AUTH-FEIGN] getUserByAuthServiceId={}", authServiceId);
        return ResponseEntity.ok(authService.getUserByAuthServiceId(authServiceId));
    }

    // FEIGN INTERNAL - increment a user stat counter (From Question/Quiz Service)
    @PostMapping("/internal/sync-stat")
    public ResponseEntity<Void> syncStat(@RequestBody UserStatSyncDTO dto) {
        log.debug("[AUTH-FEIGN] syncStat authServiceId={}, type={}", dto.getAuthServiceId(), dto.getStatType());
        authService.incrementStat(dto);
        return ResponseEntity.ok().build();
    }
}