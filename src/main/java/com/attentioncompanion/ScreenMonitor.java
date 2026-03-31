package com.attentioncompanion;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScreenMonitor {

    public static class ScreenActivity {
        private final String appKey;
        private final String processName;
        private final String title;
        private final String label;

        public ScreenActivity(String appKey, String processName, String title, String label) {
            this.appKey = appKey;
            this.processName = processName;
            this.title = title;
            this.label = label;
        }

        public String getAppKey() { return appKey; }
        public String getProcessName() { return processName; }
        public String getTitle() { return title; }
        public String getLabel() { return label; }
    }

    private String currentAppKey = null;
    private long currentStartTime = 0;
    private final Map<String, Long> usageSeconds = new ConcurrentHashMap<>();

    private final Set<String> studyProcesses = ConcurrentHashMap.newKeySet();
    private final Set<String> studyKeywords = ConcurrentHashMap.newKeySet();
    private final Set<String> nonStudyKeywords = ConcurrentHashMap.newKeySet();

    public ScreenMonitor() {
        for (String p : Config.DEFAULT_STUDY_PROCESSES)
            studyProcesses.add(p.toLowerCase());
        for (String k : Config.DEFAULT_STUDY_KEYWORDS)
            studyKeywords.add(k.toLowerCase());
        for (String k : Config.DEFAULT_BAD_KEYWORDS)
            nonStudyKeywords.add(k.toLowerCase());
    }

    public void addStudyProcess(String p) { studyProcesses.add(p.toLowerCase()); }
    public void addStudyKeyword(String k) { studyKeywords.add(k.toLowerCase()); }
    public void addNonStudyKeyword(String k) { nonStudyKeywords.add(k.toLowerCase()); }

    public ScreenActivity getCurrentActivity() {
        try {
            User32 u = User32.INSTANCE;
            HWND hwnd = u.GetForegroundWindow();
            if (hwnd == null) {
                return new ScreenActivity("unknown", "", "", "unknown");
            }

            char[] buffer = new char[512];
            u.GetWindowText(hwnd, buffer, 512);
            String title = new String(buffer).trim();
            if (title.isEmpty()) title = "(no title)";

            IntByReference pid = new IntByReference();
            u.GetWindowThreadProcessId(hwnd, pid);

            String processName = "proc_" + pid.getValue() + ".exe";

            String appKey = processName + " | " + title;
            String label = classify(appKey, processName);

            // usage‑tracking code continues…

            if (!appKey.equals(currentAppKey)) {
                if (currentAppKey != null) {
                    long elapsed = System.currentTimeMillis() - currentStartTime;
                    usageSeconds.merge(currentAppKey, elapsed / 1000, Long::sum);
                }
                currentAppKey = appKey;
                currentStartTime = System.currentTimeMillis();
            }

            return new ScreenActivity(appKey, processName, title, label);
        } catch (Exception e) {
            return new ScreenActivity("error", "", "", "unknown");
        }
    }

    public String classify(String appKey, String processName) {
        String appLower = appKey.toLowerCase();
        String procLower = processName.toLowerCase();

        if (studyProcesses.contains(procLower)) return "study";

        for (String kw : studyKeywords)
            if (appLower.contains(kw)) return "study";

        for (String kw : nonStudyKeywords)
            if (appLower.contains(kw)) return "not_required";

        return "neutral";
    }

    public void flush() {
        if (currentAppKey != null && currentStartTime > 0) {
            long elapsed = System.currentTimeMillis() - currentStartTime;
            usageSeconds.merge(currentAppKey, elapsed / 1000, Long::sum);
        }
    }

    public Map<String, Long> getUsageSeconds() {
        return new HashMap<>(usageSeconds);
    }
}
