package org.example.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.Paths;

public class CheatDetectorService {

    private final Runnable onCheatDetected;
    private Process process;
    private final String pythonExe;
    private final String scriptPath;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile boolean running = false;

    public CheatDetectorService(String pythonExe, String scriptPath, Runnable onCheatDetected) {
        this.pythonExe        = pythonExe;
        this.scriptPath       = scriptPath;
        this.onCheatDetected  = onCheatDetected;
    }

    public void start() {
        if (running) return;
        running = true;

        try {
            ProcessBuilder pb = new ProcessBuilder(pythonExe, "-u", scriptPath);
            pb.environment().putAll(System.getenv());
            pb.redirectErrorStream(false); // keep separate
            pb.redirectError(ProcessBuilder.Redirect.DISCARD); // discard stderr entirely
            var dir = Paths.get(scriptPath).getParent();
            if (dir != null) pb.directory(dir.toFile());

            process = pb.start();

            Thread reader = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println("[CheatDetector] LINE: " + line);  // ADD THIS
                        try {
                            JsonNode node = mapper.readTree(line);
                            String event  = node.path("event").asText();
                            if ("terminated".equals(event)) {
                                running = false;
                                Platform.runLater(onCheatDetected);
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                } catch (IOException ignored) {}
            }, "cheat-reader");
            reader.setDaemon(true);
            reader.start();

        } catch (IOException e) {
            System.err.println("[CheatDetector] Failed to start: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    public boolean isRunning() { return running; }
}