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

    private Random random;

    private boolean bossStarted = false;

    private final String[] allCharacters = {
            "Jollibee", "McDonald", "Colonel Sanders", "Burger King",
            "Wendy", "Jack in the Box", "Little Caesar", "Chief Khai"
    };

    // Player stats
    private int playerHP, playerMaxHP;
    private int playerMana, playerMaxMana;
    private int playerCooldownLeft;

    // Enemy stats
    private int enemyHP, enemyMaxHP;
    private int enemyMana, enemyMaxMana;
    private int enemyCooldownLeft;

    private boolean playerTurn = true;

    // ===============================
    // FIX #1: Enemy turn delay timer
    // ===============================
    private float enemyTurnTimer = 0f;
    private static final float ENEMY_TURN_DELAY = 1.2f;
    private boolean waitingForEnemyTurn = false;

    public ArcadeMode(Game game, String username, String playerChar) {
        this.game = game;
        this.username = username;
        this.playerChar = playerChar;

        enemyOrder = new ArrayList<>();
        random = new Random();

        currentStage = 1;

        initializeGauntlet();
        resetBattleStats();
    }

    // ===============================
    // CREATE ENEMY ORDER (MAX 7)
    // ===============================

    private void initializeGauntlet() {
        for (String name : allCharacters) {
            if (!name.equalsIgnoreCase(playerChar)) {
                enemyOrder.add(name);
            }
        }

        Collections.shuffle(enemyOrder);

        // FIX #2: Use new ArrayList to avoid SubList mutation crash
        if (enemyOrder.size() > 7) {
            enemyOrder = new ArrayList<>(enemyOrder.subList(0, 7));
        }
    }

    // ===============================
    // RESET STATS
    // ===============================

    private void resetBattleStats() {
        playerMaxHP = 200;
        playerHP = playerMaxHP;

        playerMaxMana = 100;
        playerMana = playerMaxMana;

        playerCooldownLeft = 0;

        enemyMaxHP = 180;
        enemyHP = enemyMaxHP;

        enemyMaxMana = 80;
        enemyMana = enemyMaxMana;

        enemyCooldownLeft = 0;

        playerTurn = true;

        // FIX #1: Reset enemy turn timer on new battle
        enemyTurnTimer = 0f;
        waitingForEnemyTurn = false;
    }

    // ===============================
    // BATTLE UPDATE
    // ===============================

    public void update(float delta, boolean skillPressed) {

        if (playerCooldownLeft > 0) playerCooldownLeft--;
        if (enemyCooldownLeft > 0) enemyCooldownLeft--;

        // PLAYER TURN
        if (playerTurn && skillPressed) {
            if (playerMana >= 20 && playerCooldownLeft <= 0) {
                int dmg = 20 + random.nextInt(21);

                enemyHP -= dmg;
                playerMana -= 20;
                playerCooldownLeft = 3;

                // FIX #1: Don't fire enemy immediately — start delay timer
                playerTurn = false;
                waitingForEnemyTurn = true;
                enemyTurnTimer = 0f;

                System.out.println(username + " used skill! Damage: " + dmg);
            }
        }

        // FIX #1: Enemy turn fires only after delay
        if (waitingForEnemyTurn && !playerTurn) {
            enemyTurnTimer += delta;

            if (enemyTurnTimer >= ENEMY_TURN_DELAY) {
                waitingForEnemyTurn = false;
                enemyTurnTimer = 0f;
                processEnemyTurn();
            }
        }

        // PLAYER LOSES
        if (playerHP <= 0) {
            System.out.println("YOU LOSE! Restarting stage " + currentStage + "...");

            // FIX #3: Reset stats but stay on same stage (retry current fight)
            resetBattleStats();

            game.setScreen(new BattleScreen(
                    game,
                    this,
                    username,
                    playerChar,
                    getCurrentEnemy(),
                    currentStage
            ));
        }

        // ENEMY DEFEATED
        else if (enemyHP <= 0) {
            System.out.println("STAGE " + currentStage + " CLEARED!");

            currentStage++;
            nextStage();
        }
    }

    // ===============================
    // FIX #1: Separated enemy turn logic
    // ===============================

    private void processEnemyTurn() {
        if (enemyHP <= 0) return;

        int dmg = 15 + random.nextInt(16);

        // Make sure enemy has mana to attack
        if (enemyMana >= 15) {
            playerHP -= dmg;
            enemyMana -= 15;
            enemyCooldownLeft = 2;
            System.out.println(getCurrentEnemy() + " attacked! Damage: " + dmg);
        } else {
            // Enemy regens mana if it can't attack
            enemyMana = Math.min(enemyMaxMana, enemyMana + 20);
            System.out.println(getCurrentEnemy() + " is recovering mana...");
        }

        // FIX #4: Mana regen happens reliably every round end
        endOfRound();

        playerTurn = true;
    }

    // ===============================
    // FIX #4: Centralized end-of-round logic
    // ===============================

    private void endOfRound() {
        int pRegen = 5 + random.nextInt(6);
        int eRegen = 5 + random.nextInt(6);

        playerMana = Math.min(playerMaxMana, playerMana + pRegen);
        enemyMana = Math.min(enemyMaxMana, enemyMana + eRegen);

        System.out.println("Mana regen: " + username + " +" + pRegen + " | " + getCurrentEnemy() + " +" + eRegen);
    }

    // ===============================
    // LOAD NEXT STAGE
    // ===============================

    public void nextStage() {

        // STAGE 1–7 NORMAL ENEMIES
        if (currentStage <= 7) {
            // FIX #5: Safe index access with bounds check
            if (currentStage - 1 >= enemyOrder.size()) {
                System.out.println("ERROR: Stage index out of bounds! Stage=" + currentStage);
                handleFinalVictory();
                return;
            }

            String enemyName = enemyOrder.get(currentStage - 1);

            System.out.println("STAGE " + currentStage + " VS " + enemyName);

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

        // STAGE 8: FINAL BOSS
        if (!bossStarted) {
            startBossBattle();
            bossStarted = true;
            return;
        }

        // GAME COMPLETE
        handleFinalVictory();
    }

    // ===============================
    // FINAL BOSS
    // ===============================

    private void startBossBattle() {
        String[] devBosses = {
                "Dev Kishanta",
                "Dev Rothesa",
                "Dev Wengie",
                "Dev Kunihiko",
                "Dev Diane"
        };

        String finalBoss = devBosses[random.nextInt(devBosses.length)];

        System.out.println("FINAL BOSS -> " + finalBoss);

        resetBattleStats();

        // Boss has boosted stats
        enemyMaxHP = 350;
        enemyHP = enemyMaxHP;

        enemyMaxMana = 150;
        enemyMana = enemyMaxMana;

        game.setScreen(new BattleScreen(
                game,
                this,
                username,
                playerChar,
                finalBoss,
                8
        ));
    }

    // ===============================
    // FINAL VICTORY
    // ===============================

    private void handleFinalVictory() {
        System.out.println("ARCADE COMPLETE! YOU BEAT THE FINAL BOSS!");

        // TODO: Load VictoryScreen here
        // game.setScreen(new VictoryScreen(game));
    }

    // ===============================
    // GET CURRENT ENEMY
    // ===============================

    public String getCurrentEnemy() {
        if (currentStage <= 7 && currentStage - 1 < enemyOrder.size()) {
            return enemyOrder.get(currentStage - 1);
        }
        return "Final Boss";
    }

    // ===============================
    // GETTERS
    // ===============================

    public int getCurrentStage()        { return currentStage; }
    public String getUsername()         { return username; }
    public String getPlayerChar()       { return playerChar; }

    public int getPlayerHP()            { return playerHP; }
    public int getPlayerMaxHP()         { return playerMaxHP; }
    public int getPlayerMana()          { return playerMana; }
    public int getPlayerMaxMana()       { return playerMaxMana; }
    public int getPlayerCooldownLeft()  { return playerCooldownLeft; }
    public boolean isPlayerTurn()       { return playerTurn; }
    public boolean isWaitingForEnemy()  { return waitingForEnemyTurn; }

    public int getEnemyHP()             { return enemyHP; }
    public int getEnemyMaxHP()          { return enemyMaxHP; }
    public int getEnemyMana()           { return enemyMana; }
    public int getEnemyMaxMana()        { return enemyMaxMana; }
    public int getEnemyCooldownLeft()   { return enemyCooldownLeft; }

    // ===============================
    // RESET ARCADE
    // ===============================

    public void reset() {
        currentStage = 1;
        bossStarted = false;

        // FIX #2: Safe clear — enemyOrder is always a proper ArrayList now
        enemyOrder.clear();
        initializeGauntlet();

        resetBattleStats();
    }
}