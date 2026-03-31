package com.attentioncompanion;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS;

public class AttentionCompanionApp {
    private final UIUpdater uiUpdater;
    private final FrameConsumer frameConsumer;
    private VideoCapture camera;  // Changed from FFmpegFrameGrabber
    private OpenCVFrameConverter.ToMat conv;
    private AttentionEstimator estimator;
    private ScreenMonitor screenMonitor;
    private Thread sessionThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private String lastAppTitle = "";
    public interface UIUpdater {
        void updateUI(double attention, double appScore, String status, int timeSec, double avgAttention);
        void log(String message);
    }

    public interface FrameConsumer {
        void onFrame(Mat mat);
    }

    public AttentionCompanionApp(UIUpdater updater, FrameConsumer consumer) {
        this.uiUpdater = updater;
        this.frameConsumer = consumer;
    }

    public void startSession() {
        if (running.get()) return;
        running.set(true);
        sessionThread = new Thread(this::runSession);
        sessionThread.start();
    }

    public void stopSession() {
        running.set(false);
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
    }

    private void runSession() {
        try {
            uiUpdater.log("Initializing camera...");

            // Use OpenCV VideoCapture instead of FFmpeg
            camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                uiUpdater.log("❌ Cannot open camera. Check if webcam is connected.");
                return;
            }

            // Set camera properties
            camera.set(CAP_PROP_FRAME_WIDTH, 640);
            camera.set(CAP_PROP_FRAME_HEIGHT, 480);
            camera.set(CAP_PROP_FPS, 30);

            conv = new OpenCVFrameConverter.ToMat();
            estimator = new AttentionEstimator(5);
            screenMonitor = new ScreenMonitor();

            uiUpdater.log("✓ Camera ready. Starting tracking...");

            int frameCount = 0;
            double totalAttention = 0;
            long startTime = System.currentTimeMillis();

            Mat frame = new Mat();

            while (running.get()) {
                // Capture frame
                if (!camera.read(frame)) {
                    uiUpdater.log("Failed to read frame");
                    break;
                }

                frameConsumer.onFrame(frame);

                // Estimate attention
                AttentionEstimator.AttentionResult result = estimator.estimateAttention(frame);
                double attentionScore = result.getScore() / 100.0;
                boolean faceDetected = result.isFaceFound();

                // Screen monitoring
                ScreenMonitor.ScreenActivity activity = screenMonitor.getCurrentActivity();
                double appScore = "study".equals(activity.getLabel()) ? 1.0 : 0.0;

                // Extract full title (no PID)
                String[] parts = activity.getAppKey().split("\\|");
                String appTitle = parts.length > 1 ? parts[1].trim() : parts[0].trim();
                if (!appTitle.equals(lastAppTitle)) {
                    uiUpdater.log(appTitle + " [" + activity.getLabel().toUpperCase() + "]");
                    lastAppTitle = appTitle;
                }


                // Combined score
                double attentionForUI = attentionScore;  // Pure attention (max 100%)
                double combinedScore = 0.7 * attentionScore + 0.3 * appScore;  // Keep for avg
                totalAttention += attentionScore;   // ← ADD THIS LINE
                frameCount++;

                double avgAttention = frameCount > 0 ? totalAttention / frameCount : 0;
                int elapsedSec = (int)((System.currentTimeMillis() - startTime) / 1000);
                String status = faceDetected ? "TRACKING" : "NO FACE";

// Send PURE attention to UI
                uiUpdater.updateUI(attentionForUI, appScore, status, elapsedSec, avgAttention);

                Thread.sleep(33); // ~30 FPS
            }

            frame.release();
            screenMonitor.flush();
            generateReport(frameCount, totalAttention / (frameCount > 0 ? frameCount : 1), screenMonitor.getUsageSeconds());

        } catch (Exception e) {
            uiUpdater.log("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            running.set(false);
            if (camera != null && camera.isOpened()) {
                camera.release();
            }
        }
    }

    private void generateReport(int frames, double avgAttention, Map<String, Long> appUsage) {
        new Thread(() -> {
            String reportPath = PdfReportGenerator.generateReport(frames, avgAttention, appUsage);
            uiUpdater.log("✅ Report saved: " + reportPath);

            // Store files for sending
            if (uiUpdater instanceof AttentionCompanionUI) {
                ((AttentionCompanionUI) uiUpdater).setReportFiles(
                        "reports/session.csv",  // CSV path
                        reportPath              // PDF path
                );
            }
        }).start();
    }

}
