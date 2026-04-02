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
    //pvc mode
    public static int calculatePVCScore(boolean isVictory, int remainingHP, int enemyWins) {
        if (!isVictory) return 0; // No points for losing!

        int baseScore = 500;
        int healthBonus = Math.max(0, remainingHP) * 3; // 3 points per HP left
        int flawlessBonus = (enemyWins == 0) ? 500 : 0; // 500 bonus points if CPU won 0 rounds

        return baseScore + healthBonus + flawlessBonus;
    }

    // pvp mode
    public int calculatePVPScore(boolean isWinner, int remainingHP) {
        // Winner gets 300 base score, Loser gets 100
        int baseScore = isWinner ? 300 : 100;
        int healthBonus = Math.max(0, remainingHP) * 2;
        return baseScore + healthBonus;
    }
}