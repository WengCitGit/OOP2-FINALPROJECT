package io.github.PASAN.modes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * INHERITANCE  : Extends Mode — gets enemy queue management for free.
 * ENCAPSULATION: currentStage and stagesCleared are private.
 *                The screen asks "what's next?" and ArcadeMode answers — screens
 *                never reach in and change stage state directly.
 * POLYMORPHISM : Implements onMatchWon/onMatchLost/isRunOver/getHUDLabel
 *                differently from EndlessMode.
 *
 * RULE: No Game, Screen, Texture, or SpriteBatch here.
 *       ArcadeBattleScreen calls these methods and handles navigation.
 */


public class ArcadeMode extends Mode {

    public static final int TOTAL_STAGES = 8;
    private static final String[] FINAL_BOSSES = {
            "Dev Kishanta", "Dev Rothesa", "Dev Wengie", "Dev Kunihiko", "Dev Diane"
    };

    private int currentStage = 1;
    private boolean runOver = false;
    private boolean playerDefeated = false;
    private final List<String> stageEnemies = new ArrayList<>();
    private final Random random = new Random();

    public ArcadeMode(String playerCharName) {
        super(playerCharName);
        buildStageEnemies();
    }

    // Each stage gets a unique enemy; last stage is a boss
    private void buildStageEnemies() {
        stageEnemies.clear();

        // Take all characters except player
        List<String> temp = new ArrayList<>();
        for (String name : ALL_CHARACTERS) {
            if (!name.equalsIgnoreCase(getPlayerCharName())) {
                temp.add(name);
            }
        }

        // Shuffle regular enemies
        Collections.shuffle(temp);
        int stagesBeforeBoss = TOTAL_STAGES - 1;
        for (int i = 0; i < stagesBeforeBoss && i < temp.size(); i++) {
            stageEnemies.add(temp.get(i));
        }

        // Last stage is a boss
        stageEnemies.add(FINAL_BOSSES[random.nextInt(FINAL_BOSSES.length)]);
    }

    @Override
    public void onMatchWon() {
        if (currentStage >= TOTAL_STAGES) {
            runOver = true;
        } else {
            currentStage++;
        }
    }

    @Override
    public void onMatchLost() {
        runOver = true;
        playerDefeated = true;
    }

    @Override
    public boolean isRunOver() {
        return runOver;
    }

    @Override
    public String getHUDLabel() {
        return "STAGE: " + currentStage + "/" + TOTAL_STAGES;
    }

    public String getCurrentStageName() {
        if (currentStage >= 1 && currentStage <= stageEnemies.size()) {
            return stageEnemies.get(currentStage - 1);
        }
        return "Unknown";
    }

    public boolean isFinalStage() { return currentStage == TOTAL_STAGES; }
    public boolean isPlayerDefeated() { return playerDefeated; }
    public int getCurrentStage() { return currentStage; }

    public int getStagesCleared() {
        return playerDefeated ? currentStage - 1 : currentStage;
    }

    public void reset() {
        currentStage = 1;
        runOver = false;
        playerDefeated = false;
        buildStageEnemies();
    }
}