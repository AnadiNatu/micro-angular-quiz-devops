package com.example.question_service.controller;


import com.example.question_service.dto.authDTO.UserSyncDTO;
import com.example.question_service.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
