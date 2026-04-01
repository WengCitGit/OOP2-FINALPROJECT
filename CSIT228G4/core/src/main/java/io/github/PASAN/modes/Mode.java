package io.github.PASAN.modes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  * Base class for game modes (Arcade, Endless).
 *  * Handles enemy queue management with proper filtering.
 *
 * ABSTRACTION  : Defines WHAT every game mode must be able to do,
 *                without caring HOW each mode does it.
 * ENCAPSULATION: Shared roster and queue logic live here — subclasses
 *                don't duplicate it.
 * INHERITANCE  : ArcadeMode and EndlessMode extend this and get
 *                buildEnemyQueue() and getNextEnemyName() for free.
 *
 * RULE: No Game, Screen, Texture, or SpriteBatch references here.
 *       Mode classes are pure logic — they work without LibGDX running.
 */

public abstract class Mode {

    private final String playerCharName;
    private List<String> enemyQueue;
    private int queueIndex = 0;

    protected static final String[] ALL_CHARACTERS = {
            "Jollibee", "Colonel Sanders", "McDonald", "Burger King",
            "Wendy", "Jack in the Box", "Little Caesar", "Chief Khai"
    };

    public Mode(String playerCharName) {
        this.playerCharName = playerCharName;
        buildEnemyQueue();
    }

    private void buildEnemyQueue() {
        enemyQueue = new ArrayList<>();
        for (String name : ALL_CHARACTERS) {
            if (!name.equalsIgnoreCase(playerCharName)) {
                enemyQueue.add(name);
            }
        }
        Collections.shuffle(enemyQueue);
        queueIndex = 0;
    }

    protected String getNextEnemyName() {
        queueIndex++;
        if (queueIndex >= enemyQueue.size()) {
            String last = enemyQueue.get(enemyQueue.size() - 1);
            buildEnemyQueue();
            if (enemyQueue.get(0).equals(last) && enemyQueue.size() > 1) {
                Collections.swap(enemyQueue, 0, 1);
            }
        }
        return enemyQueue.get(queueIndex);
    }

    protected String getCurrentEnemyName() {
        if (enemyQueue != null && queueIndex < enemyQueue.size()) {
            return enemyQueue.get(queueIndex);
        }
        return "Unknown";
    }

    protected void resetQueue() {
        buildEnemyQueue();
    }

    public String getPlayerCharName() { return playerCharName; }

    public abstract void onMatchWon();
    public abstract void onMatchLost();
    public abstract boolean isRunOver();
    public abstract String getHUDLabel();
}