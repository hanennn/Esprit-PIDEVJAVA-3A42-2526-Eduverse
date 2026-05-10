package org.example.services;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class CheatDetectorService {

    private final String pythonExe;
    private final String scriptPath;

    private Process process;
    private BufferedWriter writer;

    private volatile boolean running = false;

    private final Consumer<Integer> onCheat;
    private final Consumer<String> onWarning;

    public CheatDetectorService(String pythonExe, String scriptPath,
                                Consumer<Integer> onCheat,
                                Consumer<String> onWarning) {
        this.pythonExe = pythonExe;
        this.scriptPath = scriptPath;
        this.onCheat = onCheat;
        this.onWarning = onWarning;
    }

    public void start() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExe,
                    "-u",
                    scriptPath
            );

            pb.redirectErrorStream(true);

            process = pb.start();
            running = true;

            writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
            );

            new Thread(this::readOutput, "python-reader").start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readOutput() {
        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null && running) {

                line = line.trim();
                if (line.isEmpty()) continue;

                System.out.println("[PYTHON] " + line);

                String[] parts = line.split(":", 6);
                String type = parts[0];

                switch (type) {

                    case "READY":
                        System.out.println("Python READY: " + line);
                        break;

                    case "CHEAT":
                        if (parts.length >= 2) {
                            int count = Integer.parseInt(parts[1]);
                            onWarning.accept("Cheating detected (" + count + ")");
                        }
                        break;

                    case "OK":
                        break;

                    case "FRAME":
                        break;

                    case "TERMINATED":
                        if (parts.length >= 2) {
                            int cheats = Integer.parseInt(parts[1]);
                            onCheat.accept(cheats);
                        }
                        stop();
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            running = false;

            if (writer != null) {
                writer.write("STOP\n");
                writer.flush();
            }

            if (process != null) {
                process.destroy();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return running;
    }
}
