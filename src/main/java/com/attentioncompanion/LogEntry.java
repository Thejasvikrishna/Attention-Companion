package com.attentioncompanion;

import java.time.Instant;

public class LogEntry {
    private final Instant timestamp;
    private final double attention;
    private final boolean faceFound;
    private final String appKey;
    private final String process;
    private final String title;
    private final String label;

    public LogEntry(Instant timestamp, double attention, boolean faceFound,
                    String appKey, String process, String title, String label) {
        this.timestamp = timestamp;
        this.attention = attention;
        this.faceFound = faceFound;
        this.appKey = appKey;
        this.process = process;
        this.title = title;
        this.label = label;
    }

    public Instant getTimestamp() { return timestamp; }
    public double getAttention() { return attention; }
    public boolean isFaceFound() { return faceFound; }
    public String getAppKey() { return appKey; }
    public String getProcess() { return process; }
    public String getTitle() { return title; }
    public String getLabel() { return label; }
}
