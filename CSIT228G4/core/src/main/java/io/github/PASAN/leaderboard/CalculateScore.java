package io.github.PASAN.leaderboard;

public class CalculateScore {

    // arcade mode
    public static int calculateArcadeScore(int stagesCleared, int remainingHP, long timeSeconds) {
        int baseScore   = stagesCleared * 1000;
        int healthBonus = remainingHP * 5;
        int timeBonus   = (int) Math.max(0, (300 - timeSeconds) * 10); // 10pts/sec under 5 min
        return baseScore + healthBonus + timeBonus;
    }

    public static int calculateArcadeScore(int stagesCleared, int remainingHP) {
        return calculateArcadeScore(stagesCleared, remainingHP, 300L); // no time bonus
    }


    public static int calculateEndlessScore(int enemiesDefeated, long timeSeconds) {
        int score = 0;
        for (int i = 1; i <= enemiesDefeated; i++) score += (i * 100);
        int timeBonus = (int) Math.max(0, (600 - timeSeconds) * 5);
        return score + timeBonus;
    }

    // endless mode
    public static int calculateEndlessScore(int enemiesDefeated) {
        int score = 0;
        for (int i = 1; i <= enemiesDefeated; i++) score += (i * 100);
        return score;
    }

    // pvc mode
    public static int calculatePVCScore(boolean isVictory, int remainingHP, int enemyWins) {
        if (!isVictory) return 0;
        int baseScore      = 500;
        int healthBonus    = Math.max(0, remainingHP) * 3;
        int flawlessBonus  = (enemyWins == 0) ? 500 : 0;
        return baseScore + healthBonus + flawlessBonus;
    }

    // pvp mode
    public int calculatePVPScore(boolean isWinner, int remainingHP) {
        int baseScore   = isWinner ? 300 : 100;
        int healthBonus = Math.max(0, remainingHP) * 2;
        return baseScore + healthBonus;
    }
}