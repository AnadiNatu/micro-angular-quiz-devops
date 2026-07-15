package com.example.quiz_service.service;

import com.example.quiz_service.dto.applicationDTO.UserResponseDTO;
import com.example.quiz_service.dto.leaderboardDTO.*;
import com.example.quiz_service.entity.Quiz;
import com.example.quiz_service.entity.QuizResult;
import com.example.quiz_service.feign.UserClient;
import com.example.quiz_service.repository.QuizRepository;
import com.example.quiz_service.repository.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final QuizResultRepository quizResultRepository;
    private final QuizRepository quizRepository;
    private final UserClient userClient;

//     Quiz Leaderboard
    public QuizLeaderboardDTO getQuizLeaderboard(Long quizId){

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found : " + quizId));

        List<QuizResult> results = quizResultRepository.findByQuizIdOrderedForLeaderboard(quizId);
        List<LeaderboardEntryDTO> rankings  = assignRanks(results);

        double avg = results.stream().mapToDouble(QuizResult::getPercentage).average().orElse(0);
        double high = results.stream().mapToDouble(QuizResult::getPercentage).max().orElse(0);
        double low = results.stream().mapToDouble(QuizResult::getPercentage).min().orElse(0);

        String creatorUsername = fetchUsernameOrFallback(quiz.getCreatedByUserId() , quiz.getCreatorUsername());

        log.info("[LEADERBOARD] Quiz leaderboard built : quizId={} : , entries={}" , quizId, rankings);

        return QuizLeaderboardDTO.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .category(quiz.getCategory())
                .difficultyLevel(quiz.getDifficultyLevel())
                .creatorUsername(creatorUsername)
                .totalParticipants(results.size())
                .averagePercentage(avg)
                .highestPercentage(high)
                .lowestPercentage(low)
                .rankings(rankings)
                .build();
    }

//    Global Leaderboard
public GlobalLeaderboardDTO getGlobalLeaderboard(int limit) {

    // Fetch all results globally, pre-sorted by percentage desc
    List<QuizResult> allResults = quizResultRepository.findTopGlobal(
            PageRequest.of(0, limit > 0 ? limit : Integer.MAX_VALUE)
    );

    // Group by participantId and aggregate stats
    Map<Long, List<QuizResult>> byParticipant = allResults.stream()
            .collect(Collectors.groupingBy(QuizResult::getParticipantId));

    // Build aggregate entries sorted by averagePercentage desc
    List<GlobalRankEntryDTO> aggregated = byParticipant.entrySet().stream()
            .map(entry -> buildGlobalRankEntry(entry.getKey(), entry.getValue()))
            .sorted(Comparator
                    .comparingDouble(GlobalRankEntryDTO::getAveragePercentage).reversed()
                    .thenComparingInt(GlobalRankEntryDTO::getTotalCorrectAnswers).reversed()
                    .thenComparingInt(GlobalRankEntryDTO::getTotalQuizzesTaken).reversed()
            )
            .toList();

    // Assign sequential ranks to the sorted list
    List<GlobalRankEntryDTO> ranked = assignGlobalRanks(aggregated);

    // Apply limit after aggregation
    List<GlobalRankEntryDTO> limited = limit > 0 && ranked.size() > limit
            ? ranked.subList(0, limit)
            : ranked;

    log.info("[LEADERBOARD] Global leaderboard built: entries={}", limited.size());

    return GlobalLeaderboardDTO.builder()
            .totalEntries(limited.size())
            .rankings(limited)
            .build();
}

// Category Leaderboard
    public CategoryLeaderboardDTO getCategoryLeaderboard(String category){
        List<QuizResult> results = quizResultRepository.findByCategoryOrderedForLeaderboard(category);
        List<LeaderboardEntryDTO> rankings = assignRanks(results);

        log.info("[LEADERBOARD] Category leaderboard: category={}, entries={}", category, rankings.size());

        return CategoryLeaderboardDTO.builder()
                .category(category)
                .totalEntries(rankings.size())
                .rankings(rankings)
                .build();
    }

//    Participant Ranking
    public ParticipantRankDTO getParticipantRanking(Long participantId , Long quizId){
        List<QuizResult> history = quizResultRepository.findByParticipantIdOrderedByScore(participantId);

        if (history.isEmpty()){
            return ParticipantRankDTO.builder()
                    .participantId(participantId)
                    .participantUsername(fetchUsernameOrFallback(participantId, "unknown"))
                    .totalQuizzesTaken(0)
                    .averagePercentage(0)
                    .highestPercentage(0)
                    .totalCorrectAnswers(0)
                    .totalQuestions(0)
                    .quizHistory(List.of())
                    .build();
        }
        String username = history.get(0).getParticipantUsername();;

        int totalQuizzes = history.size();
        int totalCorrect = history.stream().mapToInt(QuizResult::getCorrectAnswers).sum();
        int totalQuestion = history.stream().mapToInt(QuizResult::getTotalQuestions).sum();
        double avgPct = history.stream().mapToDouble(QuizResult::getPercentage).average().orElse(0);
        double highPct = history.stream().mapToDouble(QuizResult::getPercentage).max().orElse(0);

        Integer quizRank = null;
        Long rankedQuizId = null;
        String rankedQuizTitle = null;

        if (quizId != null){
            QuizResult resultQuiz = history
                    .stream()
                    .filter(r -> r.getQuizId().equals(quizId))
                    .findFirst()
                    .orElse(null);

            if (resultQuiz != null){
                long countAbove = quizResultRepository.countRankedAboveInQuiz(quizId , resultQuiz.getPercentage(), resultQuiz.getCorrectAnswers());
                quizRank = (int)countAbove + 1;
                rankedQuizId = quizId;
                rankedQuizTitle = resultQuiz.getQuizTitle();
            }
        }

        // Convert history to entry DTOs without re-assigning ranks (individual snapshots)
        List<LeaderboardEntryDTO> historyDTOs = history.stream()
                .map(r -> toEntryDTO(r, 0)) // rank=0 means unranked in this context
                .toList();

        log.info("[LEADERBOARD] Participant ranking: participantId={}, quizzes={}", participantId, totalQuizzes);

        return ParticipantRankDTO.builder()
                .participantId(participantId)
                .participantUsername(username)
                .quizRank(quizRank)
                .rankedQuizId(rankedQuizId)
                .rankedQuizTitle(rankedQuizTitle)
                .totalQuizzesTaken(totalQuizzes)
                .averagePercentage(avgPct)
                .highestPercentage(highPct)
                .totalCorrectAnswers(totalCorrect)
                .totalQuestions(totalQuestion)
                .quizHistory(historyDTOs)
                .build();
    }

    // Returns the top N participants for a quiz — useful for podium/trophy display
    public List<LeaderboardEntryDTO> getTopNForQuiz(Long quizId, int n) {

        List<QuizResult> top = quizResultRepository.findTopByQuizId(
                quizId, PageRequest.of(0, n)
        );

        return assignRanks(top);
    }

//    Private helpers
    private List<LeaderboardEntryDTO> assignRanks(List<QuizResult> sortedResults){

        List<LeaderboardEntryDTO> ranked = new ArrayList<>();
        int rank = 1;

        for (int i = 0 ; i < sortedResults.size() ; i++){
            QuizResult current = sortedResults.get(i);

            if (i > 0){
                QuizResult prev = sortedResults.get(i-1);

                boolean tied = (Double.compare(current.getPercentage(), prev.getPercentage())) == 0 && (current.getCorrectAnswers() == prev.getCorrectAnswers());
                if (!tied){
                    rank = i + 1;
                }
            }
            ranked.add(toEntryDTO(current , rank));
        }
        return ranked;
    }

    private List<GlobalRankEntryDTO> assignGlobalRanks(List<GlobalRankEntryDTO> sorted){
        List<GlobalRankEntryDTO> ranked = new ArrayList<>();
        int rank = 1;

        for (int i = 0 ; i< sorted.size() ; i++){
            GlobalRankEntryDTO current = sorted.get(i);

            if (i > 0){
                GlobalRankEntryDTO prev = sorted.get(i - 1);

                boolean tied = (Double.compare(current.getAveragePercentage(), prev.getAveragePercentage())) == 0 && (current.getTotalCorrectAnswers() == prev.getTotalCorrectAnswers());
                if (!tied){
                    rank = i + 1;
                }
            }

            ranked.add(GlobalRankEntryDTO.builder()
                    .rank(rank)
                    .participantId(current.getParticipantId())
                    .participantUsername(current.getParticipantUsername())
                    .participantEmail(current.getParticipantEmail())
                    .totalQuizzesTaken(current.getTotalQuizzesTaken())
                    .totalCorrectAnswers(current.getTotalCorrectAnswers())
                    .totalQuestions(current.getTotalQuestions())
                    .averagePercentage(current.getAveragePercentage())
                    .highestPercentage(current.getHighestPercentage())
                    .build());
        }
        return ranked;
    }

    private GlobalRankEntryDTO buildGlobalRankEntry(Long participantId , List<QuizResult> result){

        String username = result.get(0).getParticipantUsername();
        String email = result.get(0).getParticipantEmail();

        int totalCorrect = result.stream().mapToInt(QuizResult::getCorrectAnswers).sum();
        int totalQuestions = result.stream().mapToInt(QuizResult::getTotalQuestions).sum();
        double avgPct = result.stream().mapToDouble(QuizResult::getPercentage).average().orElse(0);
        double highPct = result.stream().mapToDouble(QuizResult::getPercentage).max().orElse(0);

        return GlobalRankEntryDTO.builder()
                .rank(0) // assigned later
                .participantId(participantId)
                .participantUsername(username)
                .participantEmail(email)
                .totalQuizzesTaken(result.size())
                .totalCorrectAnswers(totalCorrect)
                .totalQuestions(totalQuestions)
                .averagePercentage(avgPct)
                .highestPercentage(highPct)
                .build();
    }

    private String fetchUsernameOrFallback(Long authServiceId ,String fallback){
        try{
            UserResponseDTO user = userClient.getUserByAuthServiceId(authServiceId);
            return user != null ? user.getUsername() : fallback;
        }catch (Exception ex){
            log.warn("[LEADERBOARD] Could not fetch username for authServiceId={} : {}" , authServiceId , ex.getMessage());
            return fallback;
        }
    }

    private LeaderboardEntryDTO toEntryDTO(QuizResult r, int rank) {
        return LeaderboardEntryDTO.builder()
                .rank(rank)
                .participantId(r.getParticipantId())
                .participantUsername(r.getParticipantUsername())
                .participantEmail(r.getParticipantEmail())
                .quizId(r.getQuizId())
                .quizTitle(r.getQuizTitle())
                .category(r.getCategory())
                .difficultyLevel(r.getDifficultyLevel())
                .totalQuestions(r.getTotalQuestions())
                .correctAnswers(r.getCorrectAnswers())
                .incorrectAnswers(r.getIncorrectAnswers())
                .percentage(r.getPercentage())
                .takenAt(r.getTakenAt())
                .build();
    }
}