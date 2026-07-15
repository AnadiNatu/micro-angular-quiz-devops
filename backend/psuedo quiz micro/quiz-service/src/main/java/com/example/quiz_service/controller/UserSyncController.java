package com.example.quiz_service.controller;



import com.example.quiz_service.dto.authDTO.UserSyncDTO;
import com.example.quiz_service.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class UserSyncController {

    private final UserSyncService service;

    @PostMapping("/sync")
    public ResponseEntity<String> syncUser(
            @RequestBody UserSyncDTO dto){

        log.info(
                "[QUESTION] Synchronizing User {}",
                dto.getUsername());

        service.syncUser(dto);

        return ResponseEntity.ok(
                "Question-Service synchronized successfully.");

    }

}
