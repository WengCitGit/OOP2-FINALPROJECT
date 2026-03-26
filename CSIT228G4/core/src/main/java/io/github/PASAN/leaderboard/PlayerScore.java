package io.github.PASAN.leaderboard;

public class PlayerScore {
    private String player;
    private int score;

    public PlayerScore(String playerName, int score) {
        this.player = playerName;
        this.score = score;
    }

    public String getPlayer(){
        return player;
    }

    public int getScore() {
        return score;
    }

    // Formats the object into a String so it is easy to save to a text file
    public String toString() {
        return player + "," + score;
    }
}