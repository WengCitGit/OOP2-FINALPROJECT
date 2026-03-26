package io.github.PASAN.leaderboard;

public class CalculateScore {

    // arcade mode
    public static int calculateArcadeScore(int stagesCleared, int remainingHP) {
        int baseScore = stagesCleared * 1000;
        int healthBonus = remainingHP * 5;
        return baseScore + healthBonus;
    }

    // endless mode
    public static int calculateEndlessScore(int enemiesDefeated) {
        int score = 0;
        for (int i = 1; i <= enemiesDefeated; i++) {
            score += (i * 100);
        }
        return score;
    }
}