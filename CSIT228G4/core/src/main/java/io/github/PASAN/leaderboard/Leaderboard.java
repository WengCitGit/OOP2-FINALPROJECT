package io.github.PASAN.leaderboard;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Leaderboard {
    private String filePath;
    private ArrayList<PlayerScore> scores;
    private static final int MAX_ENTRIES = 10;

    public Leaderboard(String filePath) {
        this.filePath = filePath;
        this.scores   = new ArrayList<>();
        readFile();
    }

    // PVC / PVP — no time, no streak
    public void addScore(String playerName, int newScore) {
        addScore(playerName, newScore, 0L, 0);
    }

    // Arcade — time, no streak
    public void addScore(String playerName, int newScore, long timeSeconds) {
        addScore(playerName, newScore, timeSeconds, 0);
    }

    // Endless — time + streak
    public void addScore(String playerName, int newScore, long timeSeconds, int streak) {
        boolean playerExists = false;
        for (int i = 0; i < scores.size(); i++) {
            PlayerScore ps = scores.get(i);
            if (ps.getPlayer().equalsIgnoreCase(playerName)) {
                playerExists = true;
                if (newScore > ps.getScore()) {
                    scores.set(i, new PlayerScore(playerName, newScore, timeSeconds, streak));
                }
                break;
            }
        }
        if (!playerExists) {
            scores.add(new PlayerScore(playerName, newScore, timeSeconds, streak));
        }
        sortScoresDescending();
        if (scores.size() > MAX_ENTRIES) scores.remove(scores.size() - 1);
        writeFile();
    }

    public ArrayList<PlayerScore> getTopScores() { return scores; }

    private void sortScoresDescending() {
        Collections.sort(scores, (p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
    }

    private void readFile() {
        scores.clear();
        File file = new File(filePath);
        if (!file.exists()) return;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String   line  = scanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String name   = parts[0];
                    int    score  = Integer.parseInt(parts[1].trim());
                    long   time   = parts.length >= 3 ? Long.parseLong(parts[2].trim())   : 0L;
                    int    streak = parts.length >= 4 ? Integer.parseInt(parts[3].trim()) : 0;
                    scores.add(new PlayerScore(name, score, time, streak));
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading leaderboard: " + e.getMessage());
        }
    }

    private void writeFile() {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (PlayerScore ps : scores) {
                writer.write(ps.toString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving leaderboard: " + e.getMessage());
        }
    }
}