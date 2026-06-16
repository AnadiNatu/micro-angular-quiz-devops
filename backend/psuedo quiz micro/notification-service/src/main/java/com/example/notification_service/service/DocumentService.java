package com.example.notification_service.service;

import com.example.notification_service.dto.shared.QuizResultDTO;
import com.example.notification_service.dto.shared.QuizStatsDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DocumentService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy , hh:mm a");

    // ========================= SHARED HTML SHELL =========================

    private String shell(String title, String subtitle, String body) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
                  <style>
                    :root {
                      --brand-dark: #1e3a5f;
                      --brand-mid:  #2980b9;
                      --brand-light:#ebf5fb;
                    }
                    body { background:#f0f2f5; font-family:'Segoe UI',system-ui,sans-serif; }
                    .page-card { background:#fff; border-radius:14px;
                                 box-shadow:0 6px 30px rgba(0,0,0,.10); max-width:960px;
                                 margin:32px auto; }
                    .doc-header { background:linear-gradient(135deg,var(--brand-dark),var(--brand-mid));
                                  border-radius:14px 14px 0 0; padding:28px 36px; color:#fff; }
                    .doc-header h2 { font-weight:700; margin:0; }
                    .doc-header p  { margin:4px 0 0; opacity:.8; font-size:.9rem; }
                    .stat-pill { background:var(--brand-light); border-left:4px solid var(--brand-mid);
                                 border-radius:6px; padding:12px 16px; }
                    .stat-pill .val { font-size:1.6rem; font-weight:700; color:var(--brand-dark); }
                    .stat-pill .lbl { font-size:.75rem; color:#6c757d; text-transform:uppercase; letter-spacing:.05em; }
                    .tbl thead th { background:var(--brand-dark); color:#fff; border:none; padding:10px 14px; }
                    .tbl tbody td { padding:9px 14px; vertical-align:middle; }
                    .tbl tbody tr:nth-child(even) { background:#f8f9fa; }
                    .badge-pass { background:#198754!important; }
                    .badge-fail { background:#dc3545!important; }
                    .print-btn  { position:fixed; bottom:24px; right:24px; z-index:999;
                                  box-shadow:0 4px 16px rgba(0,0,0,.25); }
 
                    @media print {
                      body { background:#fff; }
                      .page-card { box-shadow:none; margin:0; max-width:100%%; border-radius:0; }
                      .doc-header { border-radius:0; -webkit-print-color-adjust:exact; print-color-adjust:exact; }
                      .tbl thead th { -webkit-print-color-adjust:exact; print-color-adjust:exact; }
                      .print-btn { display:none!important; }
                      .no-print  { display:none!important; }
                    }
                  </style>
                </head>
                <body>
 
                  <button class="btn btn-primary print-btn no-print" onclick="window.print()">
                    🖨️ Print / Save PDF
                  </button>
 
                  <div class="page-card">
                    <div class="doc-header">
                      <h2>%s</h2>
                      <p>%s &nbsp;·&nbsp; Generated: %s</p>
                    </div>
                    <div class="p-4">
                      %s
                    </div>
                    <div class="text-center text-muted small py-3 border-top">
                      Quiz Platform &nbsp;|&nbsp; Confidential Document
                    </div>
                  </div>
 
                </body>
                </html>
                """.formatted(title, subtitle, LocalDateTime.now().format(FMT), body);
    }

    // ========================= 1. QUIZ RESULT TABLE (for one quiz - ADMIN/CURATOR) =========================

    public String generateQuizResultTable(QuizStatsDTO stats) {
        StringBuilder rows = new StringBuilder();
        int rank = 1;

        List<QuizResultDTO> results = stats.getParticipantResults();

        if (results == null || results.isEmpty()) {
            rows.append("<tr><td colspan='7' class='text-center text-muted py-4'>No participants have taken this quiz yet.</td></tr>");
        } else {
            for (QuizResultDTO r : results) {
                String badge = r.getPercentage() >= 60
                        ? "<span class='badge badge-pass'>Pass ✅</span>"
                        : "<span class='badge badge-fail'>Fail ❌</span>";
                String takenAt = r.getTakenAt() != null ? r.getTakenAt().format(FMT) : "—";
                rows.append("""
                        <tr>
                          <td class="fw-bold text-center">%d</td>
                          <td>%s</td>
                          <td class="text-center text-success fw-bold">%d</td>
                          <td class="text-center text-danger fw-bold">%d</td>
                          <td class="text-center fw-bold">%d / %d</td>
                          <td class="text-center fw-bold">%.1f%%</td>
                          <td class="text-center">%s</td>
                          <td class="text-center small text-muted">%s</td>
                        </tr>
                        """.formatted(rank++, r.getParticipantUsername(),
                        r.getCorrectAnswers(), r.getIncorrectAnswers(),
                        r.getCorrectAnswers(), r.getTotalQuestions(),
                        r.getPercentage(), badge, takenAt));
            }
        }

        String statsRow = """
                <div class="row g-3 mb-4">
                  <div class="col-md-3 col-6">
                    <div class="stat-pill">
                      <div class="val">%d</div>
                      <div class="lbl">Participants</div>
                    </div>
                  </div>
                  <div class="col-md-3 col-6">
                    <div class="stat-pill">
                      <div class="val">%.1f%%</div>
                      <div class="lbl">Avg Score</div>
                    </div>
                  </div>
                  <div class="col-md-3 col-6">
                    <div class="stat-pill text-success">
                      <div class="val">%d</div>
                      <div class="lbl">Passed</div>
                    </div>
                  </div>
                  <div class="col-md-3 col-6">
                    <div class="stat-pill text-danger">
                      <div class="val">%d</div>
                      <div class="lbl">Failed</div>
                    </div>
                  </div>
                </div>
 
                <h6 class="fw-semibold mb-3 text-muted text-uppercase" style="letter-spacing:.08em">
                  Participant Breakdown
                </h6>
                <div class="table-responsive">
                  <table class="table tbl">
                    <thead>
                      <tr>
                        <th class="text-center">#</th>
                        <th>Participant</th>
                        <th class="text-center">✅ Correct</th>
                        <th class="text-center">❌ Wrong</th>
                        <th class="text-center">Score</th>
                        <th class="text-center">%%</th>
                        <th class="text-center">Status</th>
                        <th class="text-center">Taken At</th>
                      </tr>
                    </thead>
                    <tbody>%s</tbody>
                  </table>
                </div>
                """.formatted(stats.getTotalParticipants(), stats.getAveragePercentage(),
                stats.getPassCount(), stats.getFailCount(), rows);

        String subtitle = "Quiz: " + stats.getQuizTitle()
                + " · Category: " + stats.getCategory()
                + " · Created by: " + stats.getCreatorUsername();

        return shell("📊 Participant Result Table", subtitle, statsRow);
    }

    // ========================= 2. ALL QUIZ STATS (ADMIN/CURATOR overview) =========================

    public String generateAllQuizStatsDocument(List<QuizStatsDTO> allStats, String creatorUsername) {
        if (allStats == null || allStats.isEmpty()) {
            return shell("📋 Quiz Overview Report",
                    "Creator: " + creatorUsername,
                    "<p class='text-muted text-center py-4'>No quizzes found.</p>");
        }

        double overallAvg = allStats.stream()
                .mapToDouble(QuizStatsDTO::getAveragePercentage)
                .average().orElse(0);
        int totalParticipations = allStats.stream()
                .mapToInt(QuizStatsDTO::getTotalParticipants).sum();

        StringBuilder rows = new StringBuilder();
        for (QuizStatsDTO s : allStats) {
            rows.append("""
                    <tr>
                      <td class="fw-semibold">%s</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td class="text-center">%d</td>
                      <td class="text-center">%d</td>
                      <td class="text-center fw-bold">%.1f%%</td>
                      <td class="text-center">%.1f%% / %.1f%%</td>
                      <td class="text-center text-success fw-bold">%d</td>
                      <td class="text-center text-danger fw-bold">%d</td>
                    </tr>
                    """.formatted(
                    s.getQuizTitle(), s.getCategory(), s.getDifficultyLevel(),
                    s.getTotalQuestions(), s.getTotalParticipants(),
                    s.getAveragePercentage(),
                    s.getHighestPercentage(), s.getLowestPercentage(),
                    s.getPassCount(), s.getFailCount()
            ));
        }

        String content = """
                <div class="row g-3 mb-4">
                  <div class="col-md-4 col-6">
                    <div class="stat-pill">
                      <div class="val">%d</div>
                      <div class="lbl">Total Quizzes</div>
                    </div>
                  </div>
                  <div class="col-md-4 col-6">
                    <div class="stat-pill">
                      <div class="val">%d</div>
                      <div class="lbl">Total Participations</div>
                    </div>
                  </div>
                  <div class="col-md-4 col-12">
                    <div class="stat-pill">
                      <div class="val">%.1f%%</div>
                      <div class="lbl">Overall Avg Score</div>
                    </div>
                  </div>
                </div>
 
                <h6 class="fw-semibold mb-3 text-muted text-uppercase" style="letter-spacing:.08em">Quiz Summary</h6>
                <div class="table-responsive">
                  <table class="table tbl">
                    <thead>
                      <tr>
                        <th>Quiz Title</th><th>Category</th><th>Difficulty</th>
                        <th class="text-center">Questions</th><th class="text-center">Participants</th>
                        <th class="text-center">Avg %%</th><th class="text-center">High/Low</th>
                        <th class="text-center text-success">Passed</th>
                        <th class="text-center text-danger">Failed</th>
                      </tr>
                    </thead>
                    <tbody>%s</tbody>
                  </table>
                </div>
                """.formatted(allStats.size(), totalParticipations, overallAvg, rows);

        return shell("📋 Quiz Overview Report", "Creator: " + creatorUsername, content);
    }

    // ========================= 3. PARTICIPANT REPORT CARD (printable) =========================

    public String generateParticipantReportCard(String username, String email,
                                                List<QuizResultDTO> results) {
        if (results == null || results.isEmpty()) {
            return shell("📚 Report Card", "Participant: " + username,
                    "<p class='text-muted text-center py-4'>No quizzes taken yet.</p>");
        }

        double avgPct = results.stream().mapToDouble(QuizResultDTO::getPercentage).average().orElse(0);
        long passed  = results.stream().filter(r -> r.getPercentage() >= 60).count();
        long failed  = results.size() - passed;
        double bestScore = results.stream().mapToDouble(QuizResultDTO::getPercentage).max().orElse(0);

        StringBuilder rows = new StringBuilder();
        int idx = 1;
        for (QuizResultDTO r : results) {
            String badge = r.getPercentage() >= 60
                    ? "<span class='badge badge-pass'>Pass ✅</span>"
                    : "<span class='badge badge-fail'>Fail ❌</span>";
            String takenAt = r.getTakenAt() != null ? r.getTakenAt().format(FMT) : "—";
            rows.append("""
                    <tr>
                      <td class="text-center fw-bold">%d</td>
                      <td class="fw-semibold">%s</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td class="text-center text-success fw-bold">%d</td>
                      <td class="text-center text-danger fw-bold">%d</td>
                      <td class="text-center fw-bold">%d/%d</td>
                      <td class="text-center fw-bold">%.1f%%</td>
                      <td class="text-center">%s</td>
                      <td class="text-center small text-muted">%s</td>
                    </tr>
                    """.formatted(idx++, r.getQuizTitle(), r.getCategory(),
                    r.getDifficultyLevel(), r.getCuratorUsername(),
                    r.getCorrectAnswers(), r.getIncorrectAnswers(),
                    r.getCorrectAnswers(), r.getTotalQuestions(),
                    r.getPercentage(), badge, takenAt));
        }

        // Grade calculation
        String grade;
        if      (avgPct >= 90) grade = "A+";
        else if (avgPct >= 80) grade = "A";
        else if (avgPct >= 70) grade = "B";
        else if (avgPct >= 60) grade = "C";
        else if (avgPct >= 50) grade = "D";
        else                   grade = "F";

        String content = """
                <div class="d-flex align-items-center gap-4 mb-4 p-3 border rounded">
                  <div class="text-center" style="min-width:80px">
                    <div style="width:72px;height:72px;border-radius:50%%;background:var(--brand-dark);
                                color:#fff;display:flex;align-items:center;justify-content:center;
                                font-size:2rem;font-weight:700;margin:0 auto;">%s</div>
                    <div class="small text-muted mt-1">Grade</div>
                  </div>
                  <div>
                    <h5 class="fw-bold mb-0">%s</h5>
                    <div class="text-muted small">%s</div>
                  </div>
                </div>
 
                <div class="row g-3 mb-4">
                  <div class="col-md-3 col-6">
                    <div class="stat-pill">
                      <div class="val">%d</div><div class="lbl">Quizzes Taken</div>
                    </div>
                  </div>
                  <div class="col-md-3 col-6">
                    <div class="stat-pill">
                      <div class="val">%.1f%%</div><div class="lbl">Average Score</div>
                    </div>
                  </div>
                  <div class="col-md-3 col-6">
                    <div class="stat-pill">
                      <div class="val">%.1f%%</div><div class="lbl">Best Score</div>
                    </div>
                  </div>
                  <div class="col-md-3 col-6">
                    <div class="stat-pill">
                      <div class="val">%d / %d</div><div class="lbl">Pass / Fail</div>
                    </div>
                  </div>
                </div>
 
                <h6 class="fw-semibold mb-3 text-muted text-uppercase" style="letter-spacing:.08em">Detailed Results</h6>
                <div class="table-responsive">
                  <table class="table tbl">
                    <thead>
                      <tr>
                        <th class="text-center">#</th>
                        <th>Quiz</th><th>Category</th><th>Difficulty</th><th>Curator</th>
                        <th class="text-center">✅</th><th class="text-center">❌</th>
                        <th class="text-center">Score</th><th class="text-center">%%</th>
                        <th class="text-center">Status</th><th class="text-center">Date</th>
                      </tr>
                    </thead>
                    <tbody>%s</tbody>
                  </table>
                </div>
                """.formatted(grade, username, email,
                results.size(), avgPct, bestScore, passed, failed, rows);

        return shell("📚 Participant Report Card", "Participant: " + username + " · " + email, content);
    }

    // ========================= 4. SINGLE QUIZ RESULT (for participant to print) =========================

    public String generateSingleResultDocument(QuizResultDTO result) {
        String badge = result.getPercentage() >= 60
                ? "<span class='badge badge-pass fs-6 px-3 py-2'>PASSED ✅</span>"
                : "<span class='badge badge-fail fs-6 px-3 py-2'>FAILED ❌</span>";

        // Progress bar colour
        String barColour = result.getPercentage() >= 60 ? "bg-success" : "bg-danger";
        int barWidth = (int) Math.round(result.getPercentage());

        String grade;
        if      (result.getPercentage() >= 90) grade = "A+";
        else if (result.getPercentage() >= 80) grade = "A";
        else if (result.getPercentage() >= 70) grade = "B";
        else if (result.getPercentage() >= 60) grade = "C";
        else if (result.getPercentage() >= 50) grade = "D";
        else                                   grade = "F";

        String takenAt = result.getTakenAt() != null ? result.getTakenAt().format(FMT) : "—";

        String content = """
                <div class="text-center mb-4">
                  %s
                  <div style="font-size:3rem;font-weight:800;color:var(--brand-dark)" class="mt-2">%s</div>
                  <div class="text-muted">Grade</div>
                </div>
 
                <div class="progress mb-4" style="height:22px;border-radius:11px;">
                  <div class="progress-bar %s fw-bold" style="width:%d%%;font-size:.9rem;">%.1f%%</div>
                </div>
 
                <div class="row g-3 mb-4">
                  <div class="col-6">
                    <div class="stat-pill text-success">
                      <div class="val text-success">%d</div>
                      <div class="lbl">Correct Answers</div>
                    </div>
                  </div>
                  <div class="col-6">
                    <div class="stat-pill text-danger">
                      <div class="val text-danger">%d</div>
                      <div class="lbl">Incorrect Answers</div>
                    </div>
                  </div>
                </div>
 
                <div class="table-responsive mb-3">
                  <table class="table tbl">
                    <tbody>
                      <tr><td class="fw-semibold" style="width:40%%">📋 Quiz Title</td><td>%s</td></tr>
                      <tr><td class="fw-semibold">📚 Category</td><td>%s</td></tr>
                      <tr><td class="fw-semibold">⚡ Difficulty</td><td>%s</td></tr>
                      <tr><td class="fw-semibold">👤 Participant</td><td>%s</td></tr>
                      <tr><td class="fw-semibold">👨‍🏫 Created by</td><td>%s</td></tr>
                      <tr><td class="fw-semibold">📊 Total Questions</td><td>%d</td></tr>
                      <tr><td class="fw-semibold">🕐 Taken At</td><td>%s</td></tr>
                    </tbody>
                  </table>
                </div>
                """.formatted(badge, grade, barColour, barWidth, result.getPercentage(),
                result.getCorrectAnswers(), result.getIncorrectAnswers(),
                result.getQuizTitle(), result.getCategory(), result.getDifficultyLevel(),
                result.getParticipantUsername(), result.getCuratorUsername(),
                result.getTotalQuestions(), takenAt);

        return shell("🏆 Quiz Result Certificate",
                "Official result for " + result.getParticipantUsername(), content);
    }
}
