package com.attentioncompanion;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.bytedeco.opencv.global.opencv_core.mean;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.opencv_core.Rect;

public class AttentionEstimator {

    public static class AttentionResult {
        private final double score;
        private final boolean faceFound;
        private final double pitch;
        private final double yaw;

        public AttentionResult(double score, boolean faceFound,
                               double pitch, double yaw) {
            this.score = score;
            this.faceFound = faceFound;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public double getScore() { return score; }
        public boolean isFaceFound() { return faceFound; }
        public double getPitch() { return pitch; }
        public double getYaw() { return yaw; }
    }

    private final Queue<Double> history = new LinkedList<>();
    private final int window;
    private final CascadeClassifier faceDetector;

    public AttentionEstimator(int smoothingWindow) {
        this.window = smoothingWindow;

        // Load cascade from resources using InputStream
        try {
            var inputStream = getClass().getResourceAsStream("/haarcascade_frontalface_default.xml");
            if (inputStream == null) {
                throw new RuntimeException("Cannot find haarcascade_frontalface_default.xml in resources");
            }

            // Write to temp file
            java.nio.file.Path tempPath = java.nio.file.Files.createTempFile("haarcascade", ".xml");
            java.nio.file.Files.copy(inputStream, tempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();

            faceDetector = new CascadeClassifier(tempPath.toString());
            tempPath.toFile().deleteOnExit();  // cleanup

            if (faceDetector.empty()) {
                throw new RuntimeException("Failed to load face cascade from temp file");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load face cascade", e);
        }
    }

    public AttentionResult estimateAttention(Mat frame) {
        Mat gray = new Mat();
        cvtColor(frame, gray, COLOR_BGR2GRAY);

        RectVector faces = new RectVector();

// More sensitive detection
        double scaleFactor = 1.05;   // was default ~1.1–1.3
        int minNeighbors = 2;        // lower → more detections, less strict

        faceDetector.detectMultiScale(
                gray,
                faces,
                scaleFactor,
                minNeighbors,
                0,
                null,
                null
        );


        if (faces.size() == 0) {
            add(0.0);
            gray.release();
            faces.close();
            return new AttentionResult(avg(), false, 0, 0);
        }

// Take first face
        Rect face = faces.get(0);

// Compute vertical position of face center
        double centerY = face.y() + face.height() / 2.0;
        double frameCenterY = gray.rows() / 2.0;

// If face is much lower than center (head dropped), treat as sleepy
        double offset = (centerY - frameCenterY) / frameCenterY;  // >0 if below center

        double baseScore;
        if (offset > 0.25) {
            // head significantly down → sleepy
            baseScore = 25.0;     //  low attention
        } else {
            baseScore = 100.0;    // normal attentive
        }

        add(baseScore);
        gray.release();
        faces.close();
        return new AttentionResult(avg(), true, 0, 0);

    }

    private void add(double v) {
        history.add(v);
        if (history.size() > window) history.poll();
    }

    private double avg() {
        if (history.isEmpty()) return 0.0;
        return history.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

}
