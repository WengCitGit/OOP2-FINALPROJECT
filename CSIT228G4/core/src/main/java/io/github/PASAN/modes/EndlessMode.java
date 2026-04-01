package io.github.PASAN.modes;

import java.util.Random;

/**
 * INHERITANCE  : Extends Mode — gets enemy queue management for free.
 * ENCAPSULATION: winStreak is private, only changed through onMatchWon/onMatchLost.
 * POLYMORPHISM : Implements onMatchWon/onMatchLost/isRunOver/getHUDLabel
 *                differently from ArcadeMode.
 *
 * RULE: No Game, Screen, Texture, or SpriteBatch here.
 *       EndlessBattleScreen calls these methods and decides what to do with the results.
 */


public class EndlessMode extends Mode {

    private int winStreak = 0;

    public EndlessMode(String playerCharName) {
        super(playerCharName);
    }

    @Override
    public void onMatchWon() {
        winStreak++;
    }

    @Override
    public void onMatchLost() {
        // Endless ends only on loss
    }

    @Override
    public boolean isRunOver() {
        return false;
    }

    @Override
    public String getHUDLabel() {
        return "WIN STREAK: " + winStreak;
    }

    /** Returns the current opponent (without advancing) */
    public String getCurrentOpponent() {
        return getCurrentEnemyName();
    }

    /** Advances the queue and returns the next opponent */
    public String nextOpponent() {
        return getNextEnemyName();
    }

    public int getWinStreak() { return winStreak; }

    public void reset() {
        winStreak = 0;
        resetQueue();
    }
}