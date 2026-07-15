package com.example.quiz_service.service;


import com.example.quiz_service.dto.authDTO.UserSyncDTO;
import com.example.quiz_service.entity.Users;
import com.example.quiz_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository repository;

    public void syncUser(UserSyncDTO dto){

        Users user = repository.findByAuthServiceId(dto.getAuthServiceId())
                .orElse(new Users());

        boolean created = user.getId() == null;

        user.setAuthServiceId(dto.getAuthServiceId());

        user.setUsername(dto.getUsername());

        user.setPassword(dto.getPassword());

        user.setEmail(dto.getEmail());

        user.setPhoneNumber(dto.getPhoneNumber());

        user.setProfilePicture(dto.getProfilePicture());

        user.setResetToken(dto.getResetToken());

        user.setRoles(dto.getRoles());

        user.setEnabled(dto.isEnabled());

        user.setAccountNonExpired(dto.isAccountNonExpired());

        user.setAccountNonLocked(dto.isAccountNonLocked());

        user.setCredentialsNonExpired(dto.isCredentialsNonExpired());

        repository.save(user);

        if(created){

            log.info(
                    "[QUESTION] User Created | authServiceId={} username={}",
                    user.getAuthServiceId(),
                    user.getUsername());

        }else{

            log.info(
                    "[QUESTION] User Updated | authServiceId={} username={}",
                    user.getAuthServiceId(),
                    user.getUsername());
        }
    }
}