package io.github.PASAN;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import io.github.PASAN.characters.*;
import io.github.PASAN.characters.Character;
import io.github.PASAN.modes.EndlessMode;
import io.github.PASAN.modes.PVCMode;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.leaderboard.Leaderboard;
import io.github.PASAN.leaderboard.CalculateScore;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;

/**
 * INHERITANCE  : Extends BaseBattleScreen — gets rendering and turn logic for free.
 * POLYMORPHISM : onMatchOver() delegates to EndlessMode, which behaves differently
 *                from ArcadeMode (no stage limit — only ends on a loss).
 * ENCAPSULATION: endlessMode owns all streak and queue state — this screen never
 *                manually tracks streaks or enemy queues.
 */

//1 Thread @ executeEnemyTurn()
public class EndlessBattleScreen extends BaseBattleScreen {

    private final EndlessMode endlessMode;
    private Texture arcadeBoard;
    private boolean showingIntro = true;
    private float introTimer = 0f;
    private static final float INTRO_DURATION = 3.0f;

    public EndlessBattleScreen(Game game, String playerName, String playerCharName) {
        this(game, playerName, playerCharName, new EndlessMode(playerCharName));
    }

    private EndlessBattleScreen(Game game, String playerName, String playerCharName, EndlessMode mode) {
        super(game, playerName, playerCharName, mode.getCurrentOpponent());
        this.endlessMode = mode;
        this.arcadeBoard = loadTextureSafe("backgrounds/arcade_board.png");

        System.out.println("[ENDLESS] Started Session for: " + playerName);
        System.out.println("[ENDLESS] Initial Opponent: " + mode.getCurrentOpponent());
    }

    @Override
    protected void executeSkill(int index) {
        Character attacker = isPlayerTurn ? player : enemy;
        Character defender = isPlayerTurn ? enemy : player;
        int oldHP = defender.getHealth();
        int manaCost = attacker.getSkills().get(index).getManaCost();

        super.executeSkill(index);

        int damage = oldHP - defender.getHealth();
        if (damage > 0) {
            System.out.println("[COMBAT] " + attacker.getName() + " used Skill " + (index + 1) +
                    " | Damage Dealt: " + damage + " | Cost: " + manaCost);
        }

        if (!isPlayerTurn) {
            System.out.println("[STATS] Mana Check | Player: " + player.getCurrentMana() +
                    " | Enemy: " + enemy.getCurrentMana());
        }
    }

    @Override
    protected boolean isPVPMode() { return false; }

    @Override
    protected void executeEnemyTurn() {
        if (!enemy.isAlive()) return;

        new Thread(new Runnable(){
            @Override
            public void run(){
                int skillIndex = PVCMode.chooseSkill(enemy, enemyCD);

                Gdx.app.postRunnable(new Runnable(){
                    @Override
                    public void run(){
                        executeSkill(skillIndex);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onMatchOver(boolean playerWon) {
        if (playerWon) {
            endlessMode.onMatchWon();
            System.out.println("[ENDLESS] Streak increased to: " + endlessMode.getWinStreak());
            loadNextOpponent();
        } else {
            endlessMode.onMatchLost();
            int score = CalculateScore.calculateEndlessScore(endlessMode.getWinStreak());
            new Leaderboard("endless_scores.txt").addScore(username, score);
            game.setScreen(new GameOverScreen(game, username, endlessMode.getWinStreak()));
            dispose();
        }
    }

    @Override
    protected String getHUDText() {
        return endlessMode.getHUDLabel() + "  |  ROUND: " + currentRound;
    }

    private void loadNextOpponent() {
        String nextName = endlessMode.nextOpponent();
        System.out.println("[ENDLESS] Next Opponent: " + nextName);
        enemy = createCharacter(nextName);

        if (enemySprite != null) enemySprite.dispose();
        if (enemyIcon != null) enemyIcon.dispose();

        try {
            String formattedName = nextName.replace(" ", "");
            enemySprite = new Texture("characters/" + formattedName + ".png");
            enemyRegion = new TextureRegion(enemySprite);
            enemyRegion.flip(true, false);

            enemyIcon = new Texture("icons/" + formattedName + "_icon.png");
            enemyIconRegion = new TextureRegion(enemyIcon);
            enemyIconRegion.flip(true, false);
        } catch (Exception e) {
            Gdx.app.error("ASSETS", "Missing assets for: " + nextName);
        }

        resetBattleState();
    }

    private void resetBattleState() {
        currentRound = 1;
        playerWins = 0;
        enemyWins = 0;
        isTransitioning = false;
        transitionTimer = 0;
        matchIsOver = false;
        isPlayerTurn = true;
        playerCD = new int[]{0, 0, 0};
        enemyCD = new int[]{0, 0, 0};

        player.restoreHP();
        player.restoreMana();

        showingIntro = true;
        showingRoundIntro = true;
        roundIntroTimer = 0f;
        introTimer = 0f;

    }

    @Override
    public void render(float delta) {
        if (showingIntro) {
            drawIntroSequence(delta);
            return;
        }
        super.render(delta);
    }

    private void drawIntroSequence(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (background != null) batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.end();

        introTimer += delta;
        drawIntroPopup();

        if (introTimer >= INTRO_DURATION || Gdx.input.justTouched()) {
            showingIntro = false;
            introTimer = 0f;
        }
    }

    private void drawIntroPopup() {
        float boardW = 1400f, boardH = 750f;
        float boardX = (WORLD_WIDTH - boardW) / 2f;
        float boardY = (WORLD_HEIGHT - boardH) / 2f - 30f;
        float charW = 410f, charH = 480f;
        float pSpriteX = boardX + 160f;
        float eSpriteX = boardX + boardW - 130f - charW;
        float spriteY = boardY + 160f;

        batch.begin();
        if (arcadeBoard != null) batch.draw(arcadeBoard, boardX, boardY, boardW, boardH);

        String streakLabel = (endlessMode.getWinStreak() == 0) ? "ENDLESS BATTLE" : "STREAK: " + endlessMode.getWinStreak();
        font.getData().setScale(4.5f);
        font.setColor(Color.RED);
        GlyphLayout gl = new GlyphLayout(font, streakLabel);
        font.draw(batch, gl, WORLD_WIDTH / 2f - gl.width / 2f, boardY + boardH - 55f);

        if (playerSprite != null) batch.draw(playerSprite, pSpriteX, spriteY, charW, charH);
        if (enemyRegion != null) batch.draw(enemyRegion, eSpriteX, spriteY, charW, charH);

        font.getData().setScale(2.8f);
        font.setColor(Color.WHITE);
        GlyphLayout pl = new GlyphLayout(font, player.getName());
        font.draw(batch, pl, pSpriteX + charW / 3f - pl.width / 2f, spriteY - 20f);

        GlyphLayout el = new GlyphLayout(font, enemy.getName());
        font.draw(batch, el, eSpriteX + charW / 1.5f - el.width / 2f, spriteY - 20f);

        font.getData().setScale(2.5f);
        batch.end();
    }

    private Texture loadTextureSafe(String path) {
        try { return new Texture(path); }
        catch (Exception e) { return null; }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (arcadeBoard != null) arcadeBoard.dispose();
        System.out.println("[ENDLESS] Screen Disposed.");
    }
}