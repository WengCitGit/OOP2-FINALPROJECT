package io.github.PASAN.modes;

import com.badlogic.gdx.Game;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.screens.VictoryScreen;
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

    // Enemy turn delay
    private float enemyTurnTimer = 0f;
    private static final float ENEMY_TURN_DELAY = 1.2f;
    private boolean waitingForEnemyTurn = false;

    public ArcadeMode(Game game, String username, String playerChar) {
        this.game        = game;
        this.username    = username;
        this.playerChar  = playerChar;

        enemyOrder   = new ArrayList<>();
        random       = new Random();
        currentStage = 1;

        initializeGauntlet();
        resetBattleStats();
    }

    // ===============================
    // CREATE ENEMY ORDER (7 enemies)
    // ===============================

    private void initializeGauntlet() {
        for (String name : allCharacters) {
            if (!name.equalsIgnoreCase(playerChar)) {
                enemyOrder.add(name);
            }
        }
        Collections.shuffle(enemyOrder);
        if (enemyOrder.size() > 7) {
            enemyOrder = new ArrayList<>(enemyOrder.subList(0, 7));
        }
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
                enemyHP        -= dmg;
                playerMana     -= 20;
                playerCooldownLeft = 3;

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

        // PLAYER LOSES
        if (playerHP <= 0) {
            System.out.println("YOU LOSE!");
            resetBattleStats();
            game.setScreen(new GameOverScreen(game, username));
            return;
        }

        // ENEMY DEFEATED
        if (enemyHP <= 0) {
            System.out.println("STAGE " + currentStage + " CLEARED!");
            currentStage++;
            nextStage();
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
    // LOAD NEXT STAGE
    // ===============================

    public void nextStage() {

        // STAGES 1–7: normal enemies
        if (currentStage <= 7) {
            if (currentStage - 1 >= enemyOrder.size()) {
                System.out.println("ERROR: Stage index out of bounds! Stage=" + currentStage);
                handleFinalVictory();
                return;
            }

            String enemyName = enemyOrder.get(currentStage - 1);
            System.out.println("STAGE " + currentStage + " VS " + enemyName);

            resetBattleStats();
            game.setScreen(new BattleScreen(username, playerChar, enemyName));
            return;
        }

        // STAGE 8: final boss
        if (!bossStarted) {
            startBossBattle();
            bossStarted = true;
            return;
        }

        // ALL STAGES COMPLETE
        handleFinalVictory();
    }

    // ===============================
    // FINAL BOSS
    // ===============================

    private void startBossBattle() {
        String[] devBosses = {
                "Dev Kishanta", "Dev Rothesa", "Dev Wengie",
                "Dev Kunihiko", "Dev Diane"
        };

        String finalBoss = devBosses[random.nextInt(devBosses.length)];
        System.out.println("FINAL BOSS -> " + finalBoss);

        resetBattleStats();

        // Boss boosted stats
        enemyMaxHP   = 350;
        enemyHP      = enemyMaxHP;
        enemyMaxMana = 150;
        enemyMana    = enemyMaxMana;

        game.setScreen(new BattleScreen(username, playerChar, finalBoss));
    }

    // ===============================
    // FINAL VICTORY
    // ===============================

    private void handleFinalVictory() {
        System.out.println("ARCADE COMPLETE! YOU BEAT THE FINAL BOSS!");
        game.setScreen(new VictoryScreen(game, username));
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
        bossStarted  = false;
        enemyOrder.clear();
        initializeGauntlet();
        resetBattleStats();
    }
}