package io.github.PASAN.leaderboard;

public class PlayerScore {
    private String player;
    private int score;
    private long timeSeconds; // total arcade run time in seconds

    public PlayerScore(String playerName, int score, long timeSeconds) {
        this.player      = playerName;
        this.score       = score;
        this.timeSeconds = timeSeconds;
    }

    // Backward-compat constructor for modes that don't track time
    public PlayerScore(String playerName, int score) {
        this(playerName, score, 0L);
    }

    public String getPlayer()      { return player; }
    public int    getScore()       { return score; }
    public long   getTimeSeconds() { return timeSeconds; }

    @Override
    public String toString() {
        return player + "," + score + "," + timeSeconds;
    }
}