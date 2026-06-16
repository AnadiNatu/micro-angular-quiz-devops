package com.example.question_service.repository;


import com.example.question_service.entity.Users;
import com.example.question_service.enums.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsernameIgnoreCase(String username);

    Optional<Users> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    Optional<Users> findByRoles(UserRoles role);

    List<Users> findAllByRoles(UserRoles role);
}
