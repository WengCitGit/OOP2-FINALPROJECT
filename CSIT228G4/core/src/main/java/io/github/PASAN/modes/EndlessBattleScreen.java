package io.github.PASAN.modes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import java.util.Arrays;

import io.github.PASAN.characters.Character;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.leaderboard.Leaderboard;
import io.github.PASAN.leaderboard.CalculateScore;

public class EndlessBattleScreen extends BaseBattleScreen {

    private final EndlessMode endlessMode;
    private Texture arcadeBoard;

    private boolean showingIntro = true;
    private float introTimer = 0f;
    private static final float INTRO_DURATION = 3.0f;

    private float endlessRunTimer = 0f;

    public EndlessBattleScreen(Game game, String playerName, String playerCharName) {
        this(game, playerName, playerCharName, new EndlessMode(playerCharName));
    }

    private EndlessBattleScreen(Game game, String playerName, String playerCharName, EndlessMode mode) {
        super(game, playerName, playerCharName, mode.getCurrentOpponent());
        this.endlessMode = mode;

        this.arcadeBoard = loadTextureSafe("backgrounds/arcade_board.png");
        randomizeBackground();

        System.out.println("[ENDLESS] Started Session for: " + playerName);
        System.out.println("[ENDLESS] Initial Opponent: " + mode.getCurrentOpponent());
    }

    @Override
    protected void executeSkill(int index) {
        Character attacker = isPlayerTurn ? player : enemy;
        Character defender = isPlayerTurn ? enemy : player;

        int oldHP    = defender.getHealth();
        int manaCost = attacker.getSkills().get(index).getManaCost();

        super.executeSkill(index);

        int damage = oldHP - defender.getHealth();
        if (damage > 0) {
            System.out.println("[COMBAT] " + attacker.getName()
                    + " used Skill " + (index + 1)
                    + " | Damage: " + damage
                    + " | Cost: " + manaCost);
        }

        if (!isPlayerTurn) {
            System.out.println("[STATS] Mana | Player: " + player.getCurrentMana()
                    + " | Enemy: " + enemy.getCurrentMana());
        }
    }

    @Override
    protected boolean isPVPMode() { return false; }

    @Override
    protected void executeEnemyTurn() {
        if (!enemy.isAlive()) return;
        int skillIndex = PVCMode.chooseSkill(enemy, enemyCD);
        executeSkill(skillIndex);
    }

    @Override
    protected void onMatchOver(boolean playerWon) {
        if (playerWon) {
            endlessMode.onMatchWon();
            System.out.println("[ENDLESS] Streak: " + endlessMode.getWinStreak());
            loadNextOpponent();
        } else {
            endlessMode.onMatchLost();

            long timeSeconds = (long) endlessRunTimer;
            int  finalScore  = CalculateScore.calculateEndlessScore(
                    endlessMode.getWinStreak(), timeSeconds);

            System.out.println("[ENDLESS] Run time: " + timeSeconds + "s | Score: " + finalScore);

            new Thread(() -> {
                new Leaderboard("endless_scores.txt").addScore(username, finalScore, timeSeconds);
                System.out.println("[Thread] Endless score saved in background.");
            }).start();

            game.setScreen(new GameOverScreen(game, username, endlessMode.getWinStreak()));
            dispose();
        }
    }

    @Override
    protected String getHUDText() {
        return endlessMode.getHUDLabel() + " | ROUND: " + currentRound;
    }

    private void loadNextOpponent() {
        String nextName = endlessMode.nextOpponent();
        String safe = nextName.replace(" ", "");
        System.out.println("[ENDLESS] Next Opponent: " + nextName);

        // Update character names for pose lookup
        this.enemyCharName = safe;

        enemy = createCharacter(nextName);

        // Reload enemy icon
        if (enemyIcon != null) enemyIcon.dispose();
        try {
            enemyIcon = new Texture("icons/" + safe + "_icon.png");
            enemyIconRegion = new TextureRegion(enemyIcon);
            enemyIconRegion.flip(true, false);
        } catch (Exception e) {
            Gdx.app.error("ASSETS", "Missing icon for: " + nextName, e);
        }

        // Reload enemy poses through AnimationManager
        animManager.loadCharacter(safe);

        activeEffects.clear();
        randomizeBackground();
        resetBattleState();
    }

    private void resetBattleState() {
        currentRound = 1;
        playerWins   = 0;
        enemyWins    = 0;

        isTransitioning = false;
        transitionTimer = 0;
        matchIsOver     = false;
        isPlayerTurn    = true;

        Arrays.fill(playerCD, 0);
        Arrays.fill(enemyCD,  0);

        player.restoreHP();
        player.restoreMana();

        showingIntro      = true;
        showingRoundIntro = true;
        roundIntroTimer   = 0f;
        introTimer        = 0f;

        playerSkillPoseTimer = 0f;
        enemySkillPoseTimer = 0f;
    }

    @Override
    public void render(float delta) {
        if (showingIntro) {
            drawIntroSequence(delta);
            return;
        }
        if (!isPaused && !matchIsOver) {
            endlessRunTimer += delta;
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

        if (introTimer >= INTRO_DURATION || Gdx.input.justTouched()
                || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            showingIntro = false;
            introTimer   = 0f;
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

        String streakLabel = (endlessMode.getWinStreak() == 0)
                ? "ENDLESS BATTLE" : "STREAK: " + endlessMode.getWinStreak();
        font.getData().setScale(4.5f);
        font.setColor(Color.RED);
        GlyphLayout gl = new GlyphLayout(font, streakLabel);
        font.draw(batch, gl, WORLD_WIDTH / 2f - gl.width / 2f, boardY + boardH - 55f);

        // Use pose1 for the intro (idle poses)
        TextureRegion pFrame = animManager.getPose1(playerCharName);
        TextureRegion eFrame = new TextureRegion(animManager.getPose1(enemyCharName));
        if (!eFrame.isFlipX()) eFrame.flip(true, false);

        batch.draw(pFrame, pSpriteX, spriteY, charW, charH);
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

    private Texture loadTextureSafe(String path) {
        try { return new Texture(path); } catch (Exception e) { return null; }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (arcadeBoard != null) arcadeBoard.dispose();
        System.out.println("[ENDLESS] Screen Disposed.");
    }
}