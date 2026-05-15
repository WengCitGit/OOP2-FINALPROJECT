package io.github.PASAN.leaderboard;

public class PlayerScore {
    private String player;
    private int    score;
    private long   timeSeconds;
    private int    streak;      // STREAK — only used by Endless, 0 for other modes

    public PlayerScore(String playerName, int score, long timeSeconds, int streak) {
        this.player      = playerName;
        this.score       = score;
        this.timeSeconds = timeSeconds;
        this.streak      = streak;
    }

    // Backward-compat constructors — other modes don't pass streak
    public PlayerScore(String playerName, int score, long timeSeconds) {
        this(playerName, score, timeSeconds, 0);
    }
    public PlayerScore(String playerName, int score) {
        this(playerName, score, 0L, 0);
    }

    public String getPlayer()      { return player; }
    public int    getScore()       { return score; }
    public long   getTimeSeconds() { return timeSeconds; }
    public int    getStreak()      { return streak; }

    @Override
    public String toString() {
        return player + "," + score + "," + timeSeconds + "," + streak;
    }
}