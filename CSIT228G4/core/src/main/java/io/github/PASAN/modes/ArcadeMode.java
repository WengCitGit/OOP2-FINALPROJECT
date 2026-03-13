package io.github.PASAN.modes;

import com.badlogic.gdx.Game;
import io.github.PASAN.screens.BattleScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ArcadeMode {

    private Game game;
    private String username;
    private String playerChar;

    private List<String> enemyOrder;
    private int currentStage;
    private int wins;

    private Random random;
    private boolean bossStarted = false;

    private final String[] allCharacters = {
            "Jollibee", "McDonald", "Colonel Sanders", "Burger King",
            "Wendy", "Jack in the Box", "Little Caesar", "Chief Khai"
    };

    // Gameplay stats
    private int playerHP, playerMaxHP, playerMana, playerMaxMana, playerSkillCD, playerCooldownLeft;
    private int enemyHP, enemyMaxHP, enemyMana, enemyMaxMana, enemySkillCD, enemyCooldownLeft;
    private boolean playerTurn = true;

    public ArcadeMode(Game game, String username, String playerChar) {
        this.game = game;
        this.username = username;
        this.playerChar = playerChar;

        this.enemyOrder = new ArrayList<>();
        this.random = new Random();
        this.currentStage = 1;
        this.wins = 0;

        initializeGauntlet();
        resetBattleStats();
    }

    private void initializeGauntlet() {
        boolean playerIsChiefKhai = playerChar.equalsIgnoreCase("Chief Khai");

        for (String name : allCharacters) {
            if (name.equalsIgnoreCase(playerChar))
                continue;
            if (!playerIsChiefKhai && name.equalsIgnoreCase("Chief Khai"))
                continue;
            enemyOrder.add(name);
        }

        Collections.shuffle(enemyOrder);
    }

    private void resetBattleStats() {
        playerMaxHP = 200; playerHP = playerMaxHP;
        playerMaxMana = 100; playerMana = playerMaxMana;
        playerSkillCD = 3; playerCooldownLeft = 0;

        enemyMaxHP = 180; enemyHP = enemyMaxHP;
        enemyMaxMana = 80; enemyMana = enemyMaxMana;
        enemySkillCD = 2; enemyCooldownLeft = 0;

        playerTurn = true;
    }

    /**
     * Called every frame in BattleScreen to process player & enemy turns.
     */
    public void update(float delta, boolean skillPressed) {
        // Tick cooldowns
        if (playerCooldownLeft > 0) playerCooldownLeft--;
        if (enemyCooldownLeft > 0) enemyCooldownLeft--;

        // PLAYER TURN
        if (playerTurn && skillPressed && playerMana >= 20 && playerCooldownLeft <= 0) {
            int dmg = 20 + random.nextInt(21); // 20-40 damage
            enemyHP -= dmg;
            playerMana -= 20;
            playerCooldownLeft = playerSkillCD;
            playerTurn = false;
            System.out.println(username + " used Skill! Damage: " + dmg);
        }

        // ENEMY TURN
        if (!playerTurn) {
            if (enemyHP > 0) {
                int dmg = 15 + random.nextInt(16); // 15-30 damage
                playerHP -= dmg;
                enemyMana -= 15;
                enemyCooldownLeft = enemySkillCD;
                playerTurn = true;
                System.out.println(enemyOrder.get(wins) + " attacked! Damage: " + dmg);
            }
        }

        // CHECK DEATHS
        if (playerHP <= 0) {
            System.out.println("You Lose!");
            resetBattleStats();
            game.setScreen(new BattleScreen(game, this, username, playerChar, enemyOrder.get(wins), currentStage));
        } else if (enemyHP <= 0) {
            System.out.println("You Win Stage " + currentStage + "!");
            registerWin();
            nextStage();
        }
    }

    /**
     * Called after every victory to load the next fight
     */
    public void nextStage() {

        boolean playerIsChiefKhai = playerChar.equalsIgnoreCase("Chief Khai");

        // ===== NORMAL ENEMIES =====
        if (wins < enemyOrder.size()) {

            String enemyName = enemyOrder.get(wins);
            System.out.println("ARCADE: Stage " + currentStage + " vs " + enemyName);

            resetBattleStats();

            game.setScreen(new BattleScreen(
                    game,
                    this,
                    username,
                    playerChar,
                    enemyName,
                    currentStage
            ));

            return;
        }

        // ===== SECRET ENDING =====
        if (playerIsChiefKhai) {
            triggerSecretEnding();
            return;
        }

        // ===== FINAL BOSS =====
        if (!bossStarted) {
            startBossBattle();
            bossStarted = true;
            return;
        }

        // ===== TOTAL VICTORY =====
        handleFinalVictory();
    }

    public void registerWin() {
        wins++;
        currentStage++;
    }

    private void startBossBattle() {
        String[] devBosses = {
                "Dev Kishanta",
                "Dev Rothesa",
                "Dev Wengie",
                "Dev Kunihiko",
                "Dev Diane"
        };

        String finalBoss = devBosses[random.nextInt(devBosses.length)];

        System.out.println("ARCADE: FINAL BOSS -> " + finalBoss);

        resetBattleStats();
        enemyHP = 300; enemyMaxHP = 300;
        enemyMana = 150; enemyMaxMana = 150;

        game.setScreen(new BattleScreen(
                game,
                this,
                username,
                playerChar,
                finalBoss,
                99
        ));
    }

    private void triggerSecretEnding() {
        System.out.println("ARCADE: [dev_eye_connected] Chief Khai Secret Ending");
        // TODO: show secret ending screen
    }

    private void handleFinalVictory() {
        System.out.println("ARCADE: CONGRATULATIONS! You defeated the Final Boss!");
        // TODO: show final victory screen
    }

    public int getCurrentStage() { return currentStage; }
    public String getUsername() { return username; }
    public String getPlayerChar() { return playerChar; }

    public int getPlayerHP() { return playerHP; }
    public int getPlayerMaxHP() { return playerMaxHP; }
    public int getPlayerMana() { return playerMana; }
    public int getPlayerMaxMana() { return playerMaxMana; }
    public int getPlayerCooldownLeft() { return playerCooldownLeft; }
    public boolean isPlayerTurn() { return playerTurn; }

    public int getEnemyHP() { return enemyHP; }
    public int getEnemyMaxHP() { return enemyMaxHP; }
    public int getEnemyMana() { return enemyMana; }
    public int getEnemyMaxMana() { return enemyMaxMana; }
    public int getEnemyCooldownLeft() { return enemyCooldownLeft; }

    public void reset() {
        this.currentStage = 1;
        this.wins = 0;
        this.bossStarted = false;
        this.enemyOrder.clear();
        initializeGauntlet();
        resetBattleStats();
    }
}