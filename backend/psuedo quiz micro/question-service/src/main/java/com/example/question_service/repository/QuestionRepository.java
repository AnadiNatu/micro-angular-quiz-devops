package com.example.question_service.repository;

import com.example.question_service.entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Questions , Long>{
    List<Questions> findByCategoryIgnoreCase(String category);

    List<Questions> findByDifficultyLevelIgnoreCase(String difficultyLevel);

    Optional<Questions> findByQuestionTitle(String questionTitle);

    boolean existsByQuestionTitle(String questionTitle);

    List<Questions> findByCategoryIgnoreCaseAndDifficultyLevelIgnoreCase(
            String category,
            String difficultyLevel
    );

    @Query(value = """
            SELECT q.id 
            FROM questions q 
            WHERE LOWER(q.category) = LOWER(:category) 
            ORDER BY RANDOM() 
            LIMIT :num
            """, nativeQuery = true)
    List<Long> findRandomQuestionsByCategory(
            @Param("category") String category,
            @Param("num") Integer num
    );

    List<Questions> findByIdIn(List<Long> ids);

    @Query("""
           SELECT DISTINCT q.category
           FROM Questions q
           WHERE q.category IS NOT NULL
           AND TRIM(q.category) <> ''
           ORDER BY q.category ASC
           """)
    List<String> findAllDistinctCategories();

    List<Questions> findByCreatorAuthServiceIdOrderByIdDesc(Long creatorAuthServiceId);
}
