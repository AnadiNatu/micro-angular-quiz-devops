package com.example.quiz_service.repository;

import com.example.quiz_service.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByTitleIgnoreCase(String title);

    @Query("SELECT q FROM Quiz q WHERE q.createdByUserId = :creatorUserId ")
    List<Quiz> findByCreatedByUserId(@Param("creatorUserId") Long creatorUserId);

    @Query("SELECT q FROM Quiz q WHERE :userId MEMBER OF q.participantIds")
    List<Quiz> findByParticipantId(@Param("userId") Long userId);
}
