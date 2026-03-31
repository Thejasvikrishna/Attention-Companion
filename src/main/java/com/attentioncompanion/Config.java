package com.attentioncompanion;

public class Config {

    public static final String REPORT_DIR = "reports/";

    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final int SMTP_PORT = 465;

    public static final String[] DEFAULT_STUDY_KEYWORDS = {
            "physics", "nptel", "gate", "leetcode", "udemy", "coursera","visual studio", "android studio", "code",
            "java", "python", "c++", "javascript",
            "intellij", "eclipse", "netbeans", "pycharm",
            "notes", "word", "document", "lecture",
            "canvas", "coursera", "udemy"
    };

    public static final String[] DEFAULT_STUDY_PROCESSES = {
            "code.exe", "WINWORD.EXE", "EXCEL.EXE", "notion.exe","code", "code.exe", "Code.exe",
            "devenv", "devenv.exe",
            "studio64", "studio64.exe",
            "idea64", "idea64.exe", "intellij",
            "eclipse", "eclipse.exe",
            "netbeans", "netbeans.exe",
            "pycharm64", "pycharm64.exe",
            "codeblocks", "codeblocks.exe",
            "sublime_text", "sublime_text.exe"
    };

    public static final String[] DEFAULT_BAD_KEYWORDS = {
            "game", "steam", "epic", "discord", "spotify", "instagram",
            "facebook", "twitter", "tiktok", "valorant", "warframe",
            "pubg", "csgo", "netflix", "youtube music","reddit", "twitch", "discord"
    };
}
