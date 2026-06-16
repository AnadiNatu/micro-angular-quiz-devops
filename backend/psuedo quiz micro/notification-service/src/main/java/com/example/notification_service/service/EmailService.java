package com.example.notification_service.service;

import com.example.notification_service.dto.internal.QuizSubmittedNotificationDTO;
import com.example.notification_service.dto.internal.QuizTakenNotificationDTO;
import com.example.notification_service.dto.shared.QuizResultDTO;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("📧 Email sent → {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String wrapper(String title, String body) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
                  <style>
                    body { background:#f0f2f5; font-family:'Segoe UI',sans-serif; }
                    .card { border-radius:12px; box-shadow:0 4px 20px rgba(0,0,0,.08); }
                    .header-bar { background:linear-gradient(135deg,#1e3a5f,#2980b9); border-radius:12px 12px 0 0; }
                    .badge-pass  { background:#198754; }
                    .badge-fail  { background:#dc3545; }
                    .stat-box { background:#f8f9fa; border-radius:8px; padding:12px 20px; }
                    table { border-collapse:collapse; width:100%%; }
                    th { background:#1e3a5f; color:#fff; padding:10px 14px; }
                    td { padding:9px 14px; border-bottom:1px solid #dee2e6; }
                    tr:nth-child(even) td { background:#f8f9fa; }
                  </style>
                </head>
                <body>
                  <div class="container py-4" style="max-width:680px;">
                    <div class="card">
                      <div class="header-bar p-4 text-white">
                        <h4 class="mb-0 fw-bold">🎓 %s</h4>
                        <small class="opacity-75">Quiz Platform — Notification</small>
                      </div>
                      <div class="card-body p-4">
                        %s
                      </div>
                      <div class="card-footer text-muted text-center small py-3">
                        This is an automated notification from the Quiz Platform. Please do not reply.
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(title, body);
    }

    // ========================= 1. CURATOR: Participant took your quiz =========================

    public void sendCuratorQuizTakenEmail(QuizTakenNotificationDTO dto) {
        String body = """
                <p class="mb-3">Hello <strong>%s</strong>,</p>
                <p>A participant has just started your quiz. Here are the details:</p>
                <div class="stat-box my-3">
                  <table class="table table-borderless mb-0">
                    <tr><td class="fw-semibold" style="width:40%%">📋 Quiz</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">📚 Category</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">⚡ Difficulty</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">👤 Participant</td><td>%s</td></tr>
                  </table>
                </div>
                <p class="text-muted small">You will receive another notification with the score once the participant submits.</p>
                """.formatted(
                dto.getCuratorUsername(),
                dto.getQuizTitle(),
                dto.getCategory(),
                dto.getDifficultyLevel(),
                dto.getParticipantUsername()
        );

        send(dto.getCuratorEmail(),
                "🎯 " + dto.getParticipantUsername() + " started your quiz — " + dto.getQuizTitle(),
                wrapper("Participant Started Your Quiz", body));
    }

    // ========================= 2. PARTICIPANT: You joined a quiz =========================

    public void sendParticipantQuizStartedEmail(QuizTakenNotificationDTO dto) {
        String body = """
                <p class="mb-3">Hello <strong>%s</strong>,</p>
                <p>You have successfully started a quiz. Good luck! 🍀</p>
                <div class="stat-box my-3">
                  <table class="table table-borderless mb-0">
                    <tr><td class="fw-semibold" style="width:40%%">📋 Quiz</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">📚 Category</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">⚡ Difficulty</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">👨‍🏫 Created by</td><td>%s</td></tr>
                  </table>
                </div>
                <p>Complete all questions carefully and submit when ready.</p>
                """.formatted(
                dto.getParticipantUsername(),
                dto.getQuizTitle(),
                dto.getCategory(),
                dto.getDifficultyLevel(),
                dto.getCuratorUsername()
        );

        send(dto.getParticipantEmail(),
                "📝 You started: " + dto.getQuizTitle(),
                wrapper("Quiz Started Successfully", body));
    }

    // ========================= 3. PARTICIPANT: Your quiz result =========================

    public void sendParticipantResultEmail(QuizSubmittedNotificationDTO dto) {
        String passBadge = dto.getPercentage() >= 60
                ? "<span class='badge badge-pass'>PASSED ✅</span>"
                : "<span class='badge badge-fail'>FAILED ❌</span>";

        String body = """
                <p class="mb-3">Hello <strong>%s</strong>,</p>
                <p>Your quiz has been submitted! Here are your results:</p>
                <div class="text-center my-3">%s</div>
                <div class="stat-box my-3">
                  <table class="table table-borderless mb-0">
                    <tr><td class="fw-semibold" style="width:50%%">📋 Quiz</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">📚 Category</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">⚡ Difficulty</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">✅ Correct Answers</td><td class="text-success fw-bold">%d / %d</td></tr>
                    <tr><td class="fw-semibold">❌ Incorrect Answers</td><td class="text-danger fw-bold">%d</td></tr>
                    <tr><td class="fw-semibold">📊 Score</td><td class="fw-bold">%.1f%%</td></tr>
                  </table>
                </div>
                <p class="text-muted small">Quiz created by: <strong>%s</strong></p>
                """.formatted(
                dto.getParticipantUsername(),
                passBadge,
                dto.getQuizTitle(),
                dto.getCategory(),
                dto.getDifficultyLevel(),
                dto.getCorrectAnswers(), dto.getTotalQuestions(),
                dto.getIncorrectAnswers(),
                dto.getPercentage(),
                dto.getCuratorUsername()
        );

        send(dto.getParticipantEmail(),
                "📊 Your result for: " + dto.getQuizTitle() + " — " + String.format("%.1f%%", dto.getPercentage()),
                wrapper("Quiz Result", body));
    }

    // ========================= 4. CURATOR: Participant submitted quiz =========================

    public void sendCuratorSubmissionEmail(QuizSubmittedNotificationDTO dto) {
        String passBadge = dto.getPercentage() >= 60
                ? "<span class='badge badge-pass'>PASSED ✅</span>"
                : "<span class='badge badge-fail'>FAILED ❌</span>";

        String body = """
                <p class="mb-3">Hello <strong>%s</strong>,</p>
                <p><strong>%s</strong> has just submitted your quiz. Here is their result:</p>
                <div class="text-center my-3">%s</div>
                <div class="stat-box my-3">
                  <table class="table table-borderless mb-0">
                    <tr><td class="fw-semibold" style="width:50%%">📋 Quiz</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">👤 Participant</td><td>%s</td></tr>
                    <tr><td class="fw-semibold">✅ Correct</td><td class="text-success fw-bold">%d / %d</td></tr>
                    <tr><td class="fw-semibold">❌ Incorrect</td><td class="text-danger fw-bold">%d</td></tr>
                    <tr><td class="fw-semibold">📊 Score</td><td class="fw-bold">%.1f%%</td></tr>
                  </table>
                </div>
                """.formatted(
                dto.getCuratorUsername(),
                dto.getParticipantUsername(),
                passBadge,
                dto.getQuizTitle(),
                dto.getParticipantUsername(),
                dto.getCorrectAnswers(), dto.getTotalQuestions(),
                dto.getIncorrectAnswers(),
                dto.getPercentage()
        );

        send(dto.getCuratorEmail(),
                "📬 " + dto.getParticipantUsername() + " submitted — " + dto.getQuizTitle(),
                wrapper("Participant Submitted Your Quiz", body));
    }

    // ========================= 5. PARTICIPANT: Full Report Card =========================

    public void sendParticipantReportCardEmail(String toEmail, String username, List<QuizResultDTO> results) {
        if (results == null || results.isEmpty()) {
            log.warn("No results found for participant {} — skipping report card email", username);
            return;
        }

        double avgPct = results.stream().mapToDouble(QuizResultDTO::getPercentage).average().orElse(0);
        int totalQuizzes = results.size();
        long passed = results.stream().filter(r -> r.getPercentage() >= 60).count();

        StringBuilder rows = new StringBuilder();
        int rank = 1;
        for (QuizResultDTO r : results) {
            String badge = r.getPercentage() >= 60
                    ? "<span class='badge badge-pass'>Pass</span>"
                    : "<span class='badge badge-fail'>Fail</span>";
            rows.append("""
                    <tr>
                      <td>%d</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td class="text-center">%d/%d</td>
                      <td class="text-center fw-bold">%.1f%%</td>
                      <td class="text-center">%s</td>
                    </tr>
                    """.formatted(rank++, r.getQuizTitle(), r.getCategory(),
                    r.getDifficultyLevel(), r.getCorrectAnswers(),
                    r.getTotalQuestions(), r.getPercentage(), badge));
        }

        String body = """
                <p class="mb-3">Hello <strong>%s</strong>,</p>
                <p>Here is your complete Quiz Report Card:</p>
 
                <div class="row g-3 my-3">
                  <div class="col-4 text-center stat-box">
                    <div class="fs-3 fw-bold text-primary">%d</div>
                    <div class="small text-muted">Total Quizzes</div>
                  </div>
                  <div class="col-4 text-center stat-box">
                    <div class="fs-3 fw-bold text-success">%d</div>
                    <div class="small text-muted">Passed</div>
                  </div>
                  <div class="col-4 text-center stat-box">
                    <div class="fs-3 fw-bold text-info">%.1f%%</div>
                    <div class="small text-muted">Avg Score</div>
                  </div>
                </div>
 
                <table class="mt-3">
                  <thead>
                    <tr>
                      <th>#</th><th>Quiz</th><th>Category</th><th>Difficulty</th>
                      <th class="text-center">Score</th><th class="text-center">%%</th><th class="text-center">Status</th>
                    </tr>
                  </thead>
                  <tbody>%s</tbody>
                </table>
                """.formatted(username, totalQuizzes, passed, avgPct, rows);

        send(toEmail, "📚 Your Complete Quiz Report Card",
                wrapper("Personal Report Card — " + username, body));
    }

}
