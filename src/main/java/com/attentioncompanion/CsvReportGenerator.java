package com.attentioncompanion;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class CsvReportGenerator {

    public static String generateSessionLog(List<LogEntry> sessionLog) {
        ensureReportDir();
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .format(LocalDateTime.now());
        String filename = Config.REPORT_DIR + "session_log_" + ts + ".csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeNext(new String[]{
                    "timestamp", "attention", "face_found",
                    "app_key", "process", "title", "label"
            });

            for (LogEntry entry : sessionLog) {
                writer.writeNext(new String[]{
                        entry.getTimestamp().toString(),
                        String.format("%.2f", entry.getAttention()),
                        String.valueOf(entry.isFaceFound()),
                        entry.getAppKey(),
                        entry.getProcess(),
                        entry.getTitle(),
                        entry.getLabel()
                });
            }
        } catch (Exception e) {           // catch generic Exception, no “never thrown” issue
            e.printStackTrace();
        }

        return filename;
    }

    public static String generateAppUsageReport(Map<String, Long> appUsage,
                                                ScreenMonitor monitor) {
        ensureReportDir();
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .format(LocalDateTime.now());
        String filename = Config.REPORT_DIR + "app_usage_" + ts + ".csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeNext(new String[]{
                    "app_key", "process", "title", "seconds", "minutes", "label"
            });

            appUsage.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .forEach(entry -> {
                        String[] parts = entry.getKey().split("\\|");
                        String proc = parts.length > 0 ? parts[0].trim() : "";
                        String title = parts.length > 1 ? parts[1].trim() : "";
                        String label = monitor.classify(entry.getKey(), proc);
                        double minutes = entry.getValue() / 60.0;

                        try {
                            writer.writeNext(new String[]{
                                    entry.getKey(),
                                    proc,
                                    title,
                                    String.valueOf(entry.getValue()),
                                    String.format("%.2f", minutes),
                                    label
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filename;
    }

    public static void ensureReportDir() {
        java.io.File dir = new java.io.File(Config.REPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
