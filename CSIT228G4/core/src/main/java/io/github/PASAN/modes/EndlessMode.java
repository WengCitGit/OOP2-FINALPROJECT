package io.github.PASAN.modes;

import com.badlogic.gdx.Game;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.screens.BattleScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EndlessMode {

    private Game game;
    private String username;
    private String playerChar;

    // --- ENDLESS QUEUE ---
    private List<String> enemyQueue;   // shuffled pool, refilled automatically when exhausted
    private int queueIndex = 0;        // current position within the pool
    private int winStreak  = 0;        // player's running win streak

    private Random random = new Random();

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

    // Enemy turn delay
    private float enemyTurnTimer = 0f;
    private static final float ENEMY_TURN_DELAY = 1.2f;
    private boolean waitingForEnemyTurn = false;

    // ===============================
    // CONSTRUCTOR
    // ===============================

    public EndlessMode(Game game, String username, String playerChar) {
        this.game       = game;
        this.username   = username;
        this.playerChar = playerChar;

        buildEnemyQueue();
        resetBattleStats();
    }

    // ===============================
    // ENEMY QUEUE MANAGEMENT
    // ===============================

    /**
     * Builds a freshly shuffled pool of all characters except the player's pick.
     * Called at startup and again automatically whenever the pool runs out.
     */
    private void buildEnemyQueue() {
        enemyQueue = new ArrayList<>();
        for (String name : allCharacters) {
            if (!name.equalsIgnoreCase(playerChar)) {
                enemyQueue.add(name);
            }
        }
        Collections.shuffle(enemyQueue);
        queueIndex = 0;
    }

    /**
     * Returns the next opponent name, reshuffling the pool if exhausted.
     * Prevents the same character appearing back-to-back after a wrap-around.
     */
    private String nextEnemyName() {
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

    // ===============================
    // RESET STATS
    // ===============================

    private void resetBattleStats() {
        playerMaxHP = 200;
        playerHP    = playerMaxHP;

        playerMaxMana = 100;
        playerMana    = playerMaxMana;

        playerCooldownLeft = 0;

        enemyMaxHP = 180;
        enemyHP    = enemyMaxHP;

        enemyMaxMana = 80;
        enemyMana    = enemyMaxMana;

        enemyCooldownLeft   = 0;
        playerTurn          = true;
        enemyTurnTimer      = 0f;
        waitingForEnemyTurn = false;
    }

    // ===============================
    // BATTLE UPDATE
    // ===============================

    public void update(float delta, boolean skillPressed) {
        if (playerCooldownLeft > 0) playerCooldownLeft--;
        if (enemyCooldownLeft  > 0) enemyCooldownLeft--;

        // PLAYER TURN
        if (playerTurn && skillPressed) {
            if (playerMana >= 20 && playerCooldownLeft <= 0) {
                int dmg = 20 + random.nextInt(21);
                enemyHP            -= dmg;
                playerMana         -= 20;
                playerCooldownLeft  = 3;

                playerTurn          = false;
                waitingForEnemyTurn = true;
                enemyTurnTimer      = 0f;

                System.out.println(username + " used skill! Damage: " + dmg);
            }
        }

        // Enemy turn fires only after delay
        if (waitingForEnemyTurn && !playerTurn) {
            enemyTurnTimer += delta;
            if (enemyTurnTimer >= ENEMY_TURN_DELAY) {
                waitingForEnemyTurn = false;
                enemyTurnTimer      = 0f;
                processEnemyTurn();
            }
        }

        // PLAYER LOSES — report final streak to Game Over screen
        if (playerHP <= 0) {
            System.out.println("YOU LOSE! Final streak: " + winStreak);
            game.setScreen(new GameOverScreen(game, username, winStreak));
            return;
        }

        // ENEMY DEFEATED — extend the streak and load the next opponent
        if (enemyHP <= 0) {
            winStreak++;
            System.out.println("OPPONENT DEFEATED! Streak: " + winStreak);
            loadNextOpponent();
        }
    }

    // ===============================
    // ENEMY TURN
    // ===============================

    private void processEnemyTurn() {
        if (enemyHP <= 0) return;

        int dmg = 15 + random.nextInt(16);

        if (enemyMana >= 15) {
            playerHP          -= dmg;
            enemyMana         -= 15;
            enemyCooldownLeft  = 2;
            System.out.println(getCurrentEnemy() + " attacked! Damage: " + dmg);
        } else {
            enemyMana = Math.min(enemyMaxMana, enemyMana + 20);
            System.out.println(getCurrentEnemy() + " is recovering mana...");
        }

        endOfRound();
        playerTurn = true;
    }

    // ===============================
    // END OF ROUND — mana regen
    // ===============================

    private void endOfRound() {
        int pRegen = 5 + random.nextInt(6);
        int eRegen = 5 + random.nextInt(6);

        playerMana = Math.min(playerMaxMana, playerMana + pRegen);
        enemyMana  = Math.min(enemyMaxMana,  enemyMana  + eRegen);

        System.out.println("Mana regen: " + username + " +"
                + pRegen + " | " + getCurrentEnemy() + " +" + eRegen);
    }

    // ===============================
    // LOAD NEXT OPPONENT (ENDLESS)
    // ===============================

    /**
     * Picks the next random opponent from the queue (reshuffling if needed),
     * resets battle stats, and transitions to BattleScreen.
     * This replaces the old nextStage() / startBossBattle() / handleFinalVictory() trio.
     */
    public void loadNextOpponent() {
        String nextEnemy = nextEnemyName();
        System.out.println("ENDLESS | Streak " + winStreak + " | VS " + nextEnemy);

        resetBattleStats();
        game.setScreen(new BattleScreen(username, playerChar, nextEnemy));
    }

    // ===============================
    // GET CURRENT ENEMY NAME
    // ===============================

    public String getCurrentEnemy() {
        if (enemyQueue != null && queueIndex < enemyQueue.size()) {
            return enemyQueue.get(queueIndex);
        }
        return "Unknown";
    }

    // ===============================
    // FULL RESET (e.g. returning to menu)
    // ===============================

    public void reset() {
        winStreak = 0;
        buildEnemyQueue();
        resetBattleStats();
    }

    // ===============================
    // GETTERS
    // ===============================

    public int    getWinStreak()           { return winStreak; }
    public String getUsername()            { return username; }
    public String getPlayerChar()          { return playerChar; }

    public int    getPlayerHP()            { return playerHP; }
    public int    getPlayerMaxHP()         { return playerMaxHP; }
    public int    getPlayerMana()          { return playerMana; }
    public int    getPlayerMaxMana()       { return playerMaxMana; }
    public int    getPlayerCooldownLeft()  { return playerCooldownLeft; }
    public boolean isPlayerTurn()          { return playerTurn; }
    public boolean isWaitingForEnemy()     { return waitingForEnemyTurn; }

    public int    getEnemyHP()             { return enemyHP; }
    public int    getEnemyMaxHP()          { return enemyMaxHP; }
    public int    getEnemyMana()           { return enemyMana; }
    public int    getEnemyMaxMana()        { return enemyMaxMana; }
    public int    getEnemyCooldownLeft()   { return enemyCooldownLeft; }
}