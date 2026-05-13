package io.github.PASAN.modes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import io.github.PASAN.characters.Character;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.screens.VictoryScreen;
import io.github.PASAN.leaderboard.Leaderboard;
import io.github.PASAN.leaderboard.CalculateScore;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;

/**
 * INHERITANCE  : Extends BaseBattleScreen — gets rendering and turn logic for free.
 * POLYMORPHISM : executeEnemyTurn() uses PVCMode.chooseSkill() just like PVC,
 *                but onMatchOver() uses ArcadeMode to decide what happens next.
 * ENCAPSULATION: arcadeMode holds all stage/progression state — this screen
 *                never manually tracks stage numbers or enemy queues.
 */
public class ArcadeBattleScreen extends BaseBattleScreen {

    private final ArcadeMode arcadeMode;
    private Texture arcadeBoard;
    private boolean showingIntro = true;
    private float introTimer = 0f;
    private static final float INTRO_DURATION = 3.0f;

    private float arcadeRunTimer = 0f;

    public ArcadeBattleScreen(Game game, String playerName, String playerCharName) {
        this(game, playerName, playerCharName, new ArcadeMode(playerCharName));
    }

    private ArcadeBattleScreen(Game game, String playerName, String playerCharName, ArcadeMode mode) {
        super(game, playerName, playerCharName, mode.getCurrentStageName());
        this.arcadeMode = mode;
        this.arcadeBoard = loadTextureSafe("backgrounds/arcade_board.png");
        randomizeBackground();
        System.out.println("[ARCADE] Started Session for: " + playerName);
        System.out.println("[ARCADE] Initial Stage: " + arcadeMode.getCurrentStage()
                + " | Enemy: " + mode.getCurrentStageName());
    }


    @Override
    protected boolean isPVPMode() { return false; }

    @Override
    protected String getHUDText() {
        return arcadeMode.getHUDLabel() + "  |  ROUND: " + currentRound;
    }

    @Override
    protected void executeSkill(int index) {
        Character attacker = isPlayerTurn ? player : enemy;
        Character defender = isPlayerTurn ? enemy  : player;
        int oldHP    = defender.getHealth();
        int manaCost = attacker.getSkills().get(index).getManaCost();

        super.executeSkill(index);

        int damage = oldHP - defender.getHealth();
        if (damage > 0) {
            System.out.println("[COMBAT] " + attacker.getName() + " used Skill " + (index + 1)
                    + " | Damage: " + damage + " | Mana Cost: " + manaCost);
        }

        if (!isPlayerTurn || isPVPMode()) {
            System.out.println("[STATS] Turn Cycle End | Player Mana: " + player.getCurrentMana()
                    + " | Enemy Mana: " + enemy.getCurrentMana());
        }
    }



    @Override
    protected void executeEnemyTurn() {
        if (!enemy.isAlive()) return;

        new Thread(() -> {
            int skillIndex = PVCMode.chooseSkill(enemy, enemyCD);
            Gdx.app.postRunnable(() -> executeSkill(skillIndex));
        }).start();
    }

    @Override
    protected void onMatchOver(boolean playerWon) {
        System.out.println("[ARCADE] Match Over. Player Won: " + playerWon);

        if (playerWon) arcadeMode.onMatchWon();
        else           arcadeMode.onMatchLost();

        if (arcadeMode.isRunOver()) {
            int  remainingHP  = playerWon ? player.getHealth() : 0;
            long timeSeconds  = (long) arcadeRunTimer;                    // TIMER
            int  score        = CalculateScore.calculateArcadeScore(      // TIMER
                    arcadeMode.getStagesCleared(), remainingHP, timeSeconds);

            System.out.println("[ARCADE] Run time: " + timeSeconds + "s | Score: " + score);

            new Thread(() -> {
                new Leaderboard("arcade_scores.txt")
                        .addScore(username, score, timeSeconds);          // TIMER
                System.out.println("[Thread] Score saved successfully in background.");
            }).start();

            if (arcadeMode.isPlayerDefeated()) {
                game.setScreen(new GameOverScreen(game, username, score, true));
            } else {
                game.setScreen(new VictoryScreen(game, username, score,
                        "Through perseverance and determination,\nyou are victorious!"));
            }
            dispose();
        } else {
            loadNextStage();
        }
    }

    // =========================================================================
    //  RENDER — intercept to show stage intro before battle starts
    // =========================================================================

    @Override
    public void render(float delta) {
        if (showingIntro) {
            drawIntroSequence(delta);
            return;
        }
        // TIMER — only tick when not paused and battle is live
        if (!isPaused && !matchIsOver) {
            arcadeRunTimer += delta;
        }
        super.render(delta);
    }

    // =========================================================================
    //  STAGE INTRO
    // =========================================================================

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
        float boardW   = 1400f, boardH = 750f;
        float boardX   = (WORLD_WIDTH  - boardW) / 2f;
        float boardY   = (WORLD_HEIGHT - boardH) / 2f - 30f;
        float charW    = 550f,  charH  = 620f;
        float pSpriteX = boardX + 160f;
        float eSpriteX = boardX + boardW - 130f - charW;
        float spriteY  = boardY + 160f;

        batch.begin();

        if (arcadeBoard != null) batch.draw(arcadeBoard, boardX, boardY, boardW, boardH);

        font.getData().setScale(4.5f);
        font.setColor(Color.RED);
        String label = arcadeMode.isFinalStage() ? "FINAL STAGE" : "STAGE " + arcadeMode.getCurrentStage();
        GlyphLayout gl = new GlyphLayout(font, label);
        font.draw(batch, gl, WORLD_WIDTH / 2f - gl.width / 2f, boardY + boardH - 55f);

        // Use pose1 for the intro (idle poses)
        TextureRegion pFrame = animManager.getPose1(playerCharName);
        batch.draw(pFrame, pSpriteX, spriteY, charW, charH);

        TextureRegion eFrame = new TextureRegion(animManager.getPose1(enemyCharName));
        if (!eFrame.isFlipX()) eFrame.flip(true, false);
        batch.draw(eFrame, eSpriteX, spriteY, charW, charH);

        font.getData().setScale(2.8f);
        font.setColor(Color.BLUE);

        GlyphLayout pl = new GlyphLayout(font, player.getName());
        font.draw(batch, pl, pSpriteX + charW / 3f - pl.width / 2f, spriteY - 20f);

        GlyphLayout el = new GlyphLayout(font, enemy.getName());
        font.draw(batch, el, eSpriteX + charW / 1.5f - el.width / 2f, spriteY - 20f);

        font.getData().setScale(2.5f);
        batch.end();
    }

    // =========================================================================
    //  STAGE LOADING — FIXED: properly resets round state for new stage
    // =========================================================================

    private void loadNextStage() {
        String nextEnemyName = arcadeMode.getCurrentStageName();
        String safe = nextEnemyName.replace(" ", "");
        System.out.println("[ARCADE] Loading Stage " + arcadeMode.getCurrentStage() + ": " + nextEnemyName);

        // Update character names for pose lookup
        this.enemyCharName = safe;

        enemy = createCharacter(nextEnemyName);

        // Reload enemy icon
        if (enemyIcon != null) { enemyIcon.dispose(); enemyIcon = null; }
        try {
            enemyIcon = new Texture("icons/" + safe + "_icon.png");
            enemyIconRegion = new TextureRegion(enemyIcon);
            enemyIconRegion.flip(true, false);
        } catch (Exception e) {
            Gdx.app.error("ASSETS", "Missing icon for: " + nextEnemyName);
        }

        // Reload enemy poses through AnimationManager
        animManager.loadCharacter(safe);

        randomizeBackground();

        resetForNewStage();
        showingIntro = true;
        introTimer = 0f;
    }

    // =========================================================================
    //  TEXTURE HELPER
    // =========================================================================

    private Texture loadTextureSafe(String path) {
        try { return new Texture(path); }
        catch (Exception e) { return null; }
    }

    // =========================================================================
    //  DISPOSE
    // =========================================================================

    @Override
    public void dispose() {
        super.dispose();
        if (arcadeBoard != null) arcadeBoard.dispose();
        System.out.println("[ARCADE] Screen Disposed.");
    }
}