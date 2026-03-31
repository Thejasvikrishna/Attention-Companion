package com.attentioncompanion;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PdfReportGenerator {

    public static String generateReport(int frames, double avgAttention, Map<String, Long> appUsage) {
        // Convert to compatible format for old code
        List<LogEntry> log = null;
        ScreenMonitor monitor = new ScreenMonitor();

        return generatePdfReport(log, appUsage, monitor, frames, avgAttention);
    }

    public static String generatePdfReport(List<LogEntry> log,
                                           Map<String, Long> usage,
                                           ScreenMonitor monitor,
                                           int frames,
                                           double avgAttention) {
        CsvReportGenerator.ensureReportDir();
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        String name = Config.REPORT_DIR + "Attention_Report_" + ts + ".pdf";
        double totalMinutes = 0;
        double studyMinutes = 0;
        double nonStudyMinutes = 0;
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(name));
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font subTitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            // CLEAN TITLE (NO PID)
            Paragraph title = new Paragraph("Attention Awareness Learning Companion", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph sub = new Paragraph("Session Report\n\n", subTitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);

//            long totalSec = frames;
//            long studySec = log != null ? log.stream().filter(e -> "study".equals(e.getLabel())).count() : 0;
//            long nonStudySec = log != null ? log.stream().filter(e -> "not_required".equals(e.getLabel())).count() : 0;
//            double avgAtt = avgAttention * 100;

            Paragraph summaryTitle = new Paragraph("SESSION SUMMARY\n", subTitleFont);
            document.add(summaryTitle);

//            Paragraph summary = new Paragraph(String.format(
//                    "Average Attention : %.1f%%\n" +
//                            "Study Time : %.1f min\n" +
//                            "Non-study Time : %.1f min\n\n",
//                    avgAttention * 100.0,
//                    studyMinutes,
//                    nonStudyMinutes
//            ), normalFont);
            Paragraph summary = new Paragraph(String.format(
                    "Average Attention : %.1f%%\n"
                            ,
                    avgAttention * 100.0
            ), normalFont);
            document.add(summary);


            Paragraph tableTitle = new Paragraph("APP USAGE BREAKDOWN\n", subTitleFont);
            document.add(tableTitle);

            Table table = new Table(4);
            table.setWidth(100);
            table.setPadding(3);
            table.addCell("Application");
            table.addCell("Time (min)");
            table.addCell("Type");
            table.addCell("% Total");

            for (Map.Entry<String, Long> e : usage.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .collect(Collectors.toList())) {

                String appKey = e.getKey();
                String[] parts = appKey.split("\\|");
                String proc = parts.length > 0 ? parts[0].trim() : "";
                String titleText = parts.length > 1 ? parts[1].trim() : "";

                // Display FULL TITLE instead of app key
                String displayName = !titleText.isEmpty() ? titleText : proc;
                if (!shouldIncludeInReport(displayName)) {
                    continue;  // ← Skip this row
                }
                // Classify ALL CODING APPS as STUDY
                String label = monitor.classify(appKey, proc);   // e.g. "study", "neutral", "distract"
                String category = classifyApp(displayName) ? "STUDY" : label;

                double minutes = e.getValue() / 60.0;
                totalMinutes += minutes;
                if ("STUDY".equalsIgnoreCase(category) || "study".equalsIgnoreCase(category)) {
                    studyMinutes += minutes;
                } else {
                    nonStudyMinutes += minutes;
                }

                double pct = totalMinutes == 0 ? 0 : minutes * 100.0 / totalMinutes;

                table.addCell(displayName);
                table.addCell(String.format("%.1f", minutes));
                table.addCell(category);
                table.addCell(String.format("%.1f%%", pct));
            }
            document.add(table);

            Paragraph footer = new Paragraph("\nGenerated at: " + LocalDateTime.now(), normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return name;
    }
    private static boolean shouldIncludeInReport(String appName) {
        String app = appName.toLowerCase();

        // EXCLUDE these from report
        String[] excludedApps = {
                "accounts.google.com",
                "signin",
                "login",
                "authentication",
                "challenge",
                "oauth",
                "auth",
                "verify"
        };

        for (String excluded : excludedApps) {
            if (app.contains(excluded)) return false;
        }

        return true;
    }

    // CLASSIFY ALL CODING APPS AS STUDY
    private static boolean classifyApp(String appName) {
        String app = appName.toLowerCase();

        String[] studyApps = {
                "code", "vscode", "visual studio", "intellij", "idea",
                "eclipse", "android studio", "netbeans", "pycharm",
                "java", "python", "cpp", "c++", "javascript",
                "notes", "word", "document", "lecture",
                "canvas", "coursera", "udemy"
        };

        for (String study : studyApps) {
            if (app.contains(study)) return true;
        }

        return false;
    }
}
