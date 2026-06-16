package com.example.quiz_service.repository;

import com.example.quiz_service.entity.QuizResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult , Long> {
    List<QuizResult> findByQuizId(Long quizId);
    List<QuizResult> findByParticipantId(Long participantId);
    Optional<QuizResult> findByQuizIdAndParticipantId(Long quizId, Long participantId);
    boolean existsByQuizIdAndParticipantId(Long quizId, Long participantId);

    // Global leaderboard - top N participants ranked by avg percentage
    @Query("""
SELECT r FROM QuizResult r 
ORDER BY r.percentage DESC , r.correctAnswers DESC , r.takenAt ASC
""")
    List<QuizResult> findAllForGlobalLeaderboard();

    @Query("""
SELECT r FROM QuizResult r WHERE r.quizId = :quizId 
ORDER BY r.percentage DESC , r.correctAnswers DESC , r.takenAt ASC
""")
    List<QuizResult> findByQuizIdOrderedForLeaderboard(@Param("quizId")Long quizId);

    @Query("""
SELECT r FROM QuizResult r WHERE r.category = :category ORDER BY r.percentage DESC ,
r.correctAnswers DESC , r.takenAt ASC  
""")
    List<QuizResult> findByCategoryOrderedForLeaderboard(@Param("category") String category);

    @Query("""
SELECT COUNT(r) FROM QuizResult r WHERE r.quizId = :quizId AND 
(r.percentage > :percentage OR (r.percentage = :percentage AND r.correctAnswers > :correctAnswers ))
""")
long countRankedAboveInQuiz(@Param("quizId")Long quizId , @Param("percentage") double percentage , @Param("correctAnswers")int correctAnswers);

    @Query("""
SELECT r FROM QuizResult r WHERE r.participantId = :participantId ORDER BY 
r.percentage DESC , r.correctAnswers DESC , r.takenAt ASC
""")
    List<QuizResult> findByParticipantIdOrderedByScore(@Param("participantId")Long participantId);

    @Query("""
SELECT r FROM QuizResult r WHERE r.quizId = :quizId ORDER BY r.percentage DESC , r.correctAnswers DESC , r.takenAt ASC
""")
    List<QuizResult> findTopByQuizId(@Param("quizId")Long quizId , Pageable pageable);

    @Query("""
SELECT r FROM QuizResult r ORDER BY r.percentage DESC , r.correctAnswers DESC , r.takenAt ASC
""")
    List<QuizResult> findTopGlobal(Pageable pageable);
}