package io.github.PASAN.modes;

import io.github.PASAN.AnimationManager;
import io.github.PASAN.Main;
import io.github.PASAN.MainMenu;
import io.github.PASAN.characters.*;
import io.github.PASAN.characters.Character;
import io.github.PASAN.SkillEffect;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import java.util.Random;
import java.util.ArrayList;

public abstract class BaseBattleScreen implements Screen {
    public static String currentBackgroundPath = "backgrounds/bg1.png";
    protected static final float WORLD_WIDTH = 1920, WORLD_HEIGHT = 1080;

    protected Game game;
    protected SpriteBatch batch;
    protected BitmapFont font;
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected ShapeRenderer shapeRenderer;
    protected Vector3 touch;

    protected String username;
    protected String player2Name = "Player 2";

    // --- TEXTURES ---
    protected Texture background, playerIcon, enemyIcon;
    protected TextureRegion enemyIconRegion;
    protected Texture redUi, yellowUi;
    protected Texture skill1Btn, skill1BtnP, skill2Btn, skill2BtnP, skill3Btn, skill3BtnP;
    protected Texture round1Img, round2Img, round3Img, fightImg;
    protected Rectangle skill1Bounds, skill2Bounds, skill3Bounds;

    // --- FALLBACK TEXTURES (1x1 solid colour, always valid, never null) ---
    private Texture fallbackWhite;
    private Texture fallbackBlack;

    // --- PAUSE MENU ---
    protected boolean isPaused = false, playPressed = false, mutePressed = false, exitPressed = false;
    protected Texture dialogueBox, playBtn, playBtnP, muteBtn, muteBtnP, unmuteBtn, unmuteBtnP, exitBtn, exitBtnP;
    protected Rectangle dialogueBounds, playBounds, muteBounds, exitBounds;
    protected boolean isMuted = false;

    // --- CHARACTERS ---
    protected Character player, enemy;

    // --- ANIMATION ---
    protected AnimationManager animManager;
    protected String playerCharName, enemyCharName;

    protected float playerSkillPoseTimer = 0f;
    protected float enemySkillPoseTimer  = 0f;
    protected static final float SKILL_POSE_DURATION = 0.5f;

    protected ArrayList<SkillEffect> activeEffects = new ArrayList<>();

    // --- HIT FLASH ---
    private boolean hitActive  = false;
    private boolean hitTarget  = true;
    private float   hitTimer   = 0f;
    private static final float HIT_FLASH_DURATION = 0.35f;

    private boolean burstActive = false;
    private float   burstTimer  = 0f;
    private float   burstX, burstY;
    private static final float BURST_DURATION   = 0.25f;
    private static final float BURST_MAX_RADIUS = 140f;

    // --- FLOATING TEXT ---
    protected class FloatingText {
        String text; float x, y, timer; Color color;
        public FloatingText(String text, float x, float y, Color color) {
            this.text = text; this.x = x; this.y = y; this.color = color; this.timer = 1.5f;
        }
    }
    protected ArrayList<FloatingText> floatingTexts = new ArrayList<>();

    // --- COMBAT STATE ---
    protected boolean isPlayerTurn     = true;
    protected int     pressedSkillIndex = -1;
    protected float   turnDelay = 1.5f, turnTimer = 0;
    protected int[]   playerCD  = {0, 0, 0}, enemyCD = {0, 0, 0};
    protected Random  random    = new Random();

    protected int     currentRound    = 1, playerWins = 0, enemyWins = 0;
    protected boolean isTransitioning = false, matchIsOver = false, showingRoundIntro = true;
    protected float   transitionTimer = 0, roundIntroTimer = 0f;
    protected String  transitionMessage = "";
    protected boolean player1GoesFirstNextRound = true;

    // --- AUDIO ---
    protected Sound round1Sound, round2Sound, finalRoundSound;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public BaseBattleScreen(Game game, String username, String playerCharName, String enemyCharName) {
        this.game     = game;
        this.username = username;
        init(playerCharName, enemyCharName);
    }

    // =========================================================================
    // INIT
    // =========================================================================

    private void init(String playerCharName, String enemyCharName) {
        this.playerCharName = playerCharName.replace(" ", "");
        this.enemyCharName  = enemyCharName.replace(" ", "");

        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font          = new BitmapFont();
        font.getData().setScale(2.5f);

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();

        // Build 1x1 fallback textures — these NEVER fail and are used when
        // any UI texture file is missing, so batch.draw() never receives null.
        fallbackWhite = buildSolidTexture(1f, 1f, 1f, 0.4f);
        fallbackBlack = buildSolidTexture(0f, 0f, 0f, 0.4f);

        background = loadTexture(currentBackgroundPath);

        redUi      = loadTexture("backgrounds/red_background.png");
        yellowUi   = loadTexture("backgrounds/yellow_background.png");
        skill1Btn  = loadTexture("buttons/skill1_button.png");
        skill1BtnP = loadTexture("buttons/skill1_button_pressed.png");
        skill2Btn  = loadTexture("buttons/skill2_button.png");
        skill2BtnP = loadTexture("buttons/skill2_button_pressed.png");
        skill3Btn  = loadTexture("buttons/skill3_button.png");
        skill3BtnP = loadTexture("buttons/skill3_button_pressed.png");
        round1Img  = loadTexture("backgrounds/round1.png");
        round2Img  = loadTexture("backgrounds/round2.png");
        round3Img  = loadTexture("backgrounds/finalround.png");
        fightImg   = loadTexture("backgrounds/fighttext.png");

        playerIcon = loadTexture("icons/" + this.playerCharName + "_icon.png");
        enemyIcon  = loadTexture("icons/" + this.enemyCharName  + "_icon.png");
        if (enemyIcon != null) {
            enemyIconRegion = new TextureRegion(enemyIcon);
            enemyIconRegion.flip(true, false);
        }

        player = createCharacter(playerCharName);
        enemy  = createCharacter(enemyCharName);

        animManager = new AnimationManager();
        animManager.loadCharacter(this.playerCharName);
        animManager.loadCharacter(this.enemyCharName);

        skill1Bounds = new Rectangle(555, 230, 250, 70);
        skill2Bounds = new Rectangle(555, 145, 250, 70);
        skill3Bounds = new Rectangle(555,  60, 250, 70);

        round1Sound     = loadSound("audio/round1_audio.wav");
        round2Sound     = loadSound("audio/round2_audio.wav");
        finalRoundSound = loadSound("audio/finalround_audio.wav");

        dialogueBox = loadTexture("backgrounds/dialogue-box.png");
        playBtn     = loadTexture("buttons/resume_button.png");
        playBtnP    = loadTexture("buttons/resume_button_pressed.png");
        muteBtn     = loadTexture("buttons/mute_button.png");
        muteBtnP    = loadTexture("buttons/mute_button_pressed.png");
        unmuteBtn   = loadTexture("buttons/unmute_button.png");
        unmuteBtnP  = loadTexture("buttons/unmute_button_pressed.png");
        exitBtn     = loadTexture("buttons/exit_button.png");
        exitBtnP    = loadTexture("buttons/exit_button_pressed.png");

        float cx = WORLD_WIDTH / 2f, cy = WORLD_HEIGHT / 2f;
        dialogueBounds = new Rectangle(cx - 400, cy - 300, 800, 600);
        playBounds     = new Rectangle(cx - 175, cy + 100, 350, 100);
        muteBounds     = new Rectangle(cx - 175, cy -  20, 350, 100);
        exitBounds     = new Rectangle(cx - 175, cy - 140, 350, 100);

        player1GoesFirstNextRound = random.nextBoolean();
        isPlayerTurn = player1GoesFirstNextRound;
    }

    // =========================================================================
    // FALLBACK TEXTURE — always returns a valid non-null Texture
    // =========================================================================

    private Texture buildSolidTexture(float r, float g, float b, float a) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(r, g, b, a);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /** Returns tex if non-null, otherwise fallbackWhite. NEVER returns null. */
    private Texture safe(Texture tex) {
        return tex != null ? tex : fallbackWhite;
    }

    /** Returns tex if non-null, otherwise the supplied fallback. NEVER returns null. */
    private Texture safe(Texture tex, Texture fallback) {
        return tex != null ? tex : (fallback != null ? fallback : fallbackWhite);
    }

    // =========================================================================
    // HIT EFFECT
    // =========================================================================

    protected void triggerHitEffect(boolean targetIsEnemy, float impactX, float impactY) {
        hitActive   = true;
        hitTarget   = targetIsEnemy;
        hitTimer    = 0f;
        burstActive = true;
        burstTimer  = 0f;
        burstX      = impactX;
        burstY      = impactY;
    }

    // =========================================================================
    // RENDER
    // =========================================================================

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) isPaused = !isPaused;

        if (!isPaused) {
            if (hitActive)   { hitTimer   += delta; if (hitTimer   >= HIT_FLASH_DURATION) hitActive   = false; }
            if (burstActive) { burstTimer += delta; if (burstTimer >= BURST_DURATION)     burstActive = false; }
            if (playerSkillPoseTimer > 0) playerSkillPoseTimer -= delta;
            if (enemySkillPoseTimer  > 0) enemySkillPoseTimer  -= delta;
        }

        // Background — safe() ensures never null
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(safe(background), 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.end();

        // Character frames — AnimationManager always returns non-null (magenta placeholder)
        TextureRegion pFrame = playerSkillPoseTimer > 0
                ? animManager.getPose2(playerCharName)
                : animManager.getPose1(playerCharName);
        TextureRegion eFrame = enemySkillPoseTimer > 0
                ? animManager.getPose2(enemyCharName)
                : animManager.getPose1(enemyCharName);
        if (!eFrame.isFlipX()) eFrame.flip(true, false);

        batch.begin();
        if (showingRoundIntro) {
            batch.draw(pFrame, 80,  100, 850, 950);
            batch.draw(eFrame, WORLD_WIDTH - 900, 100, 850, 950);
        } else {
            // Player — flash + shake if hit
            if (hitActive && !hitTarget) {
                float alpha  = 1f - (hitTimer / HIT_FLASH_DURATION);
                float shakeX = (random.nextFloat() * 2 - 1) * 18f * alpha;
                batch.setColor(1f, 0.15f, 0.15f, 1f);
                batch.draw(pFrame, 200 + shakeX, 350, 550, 600);
                batch.setColor(Color.WHITE);
            } else {
                batch.draw(pFrame, 200, 350, 550, 600);
            }

            // Enemy — flash + shake if hit
            if (hitActive && hitTarget) {
                float alpha  = 1f - (hitTimer / HIT_FLASH_DURATION);
                float shakeX = (random.nextFloat() * 2 - 1) * 18f * alpha;
                batch.setColor(1f, 0.15f, 0.15f, 1f);
                batch.draw(eFrame, WORLD_WIDTH - 800 + shakeX, 350, 550, 600);
                batch.setColor(Color.WHITE);
            } else {
                batch.draw(eFrame, WORLD_WIDTH - 800, 350, 550, 600);
            }

            // Skill projectiles
            for (int i = activeEffects.size() - 1; i >= 0; i--) {
                SkillEffect effect    = activeEffects.get(i);
                boolean     wasActive = effect.active;
                if (!isPaused) effect.update(delta);
                TextureRegion frame = effect.getCurrentFrame();
                if (frame != null) {
                    batch.draw(frame,
                            effect.position.x - 250, effect.position.y - 250, 500, 500);
                }
                if (wasActive && !effect.active) {
                    boolean enemyWasTarget = !effect.isFlipX();
                    float   hitCentreX     = enemyWasTarget ? WORLD_WIDTH - 700 + 225 : 200 + 225;
                    triggerHitEffect(enemyWasTarget, hitCentreX, 600f);
                }
                if (!effect.active) activeEffects.remove(i);
            }
        }
        batch.end();

        // Impact burst circle
        if (burstActive && !showingRoundIntro) {
            float progress = burstTimer / BURST_DURATION;
            float radius   = BURST_MAX_RADIUS * progress;
            float alpha    = 1f - progress;
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.9f, 0.3f, alpha * 0.55f);
            shapeRenderer.circle(burstX, burstY, radius * 0.55f);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 1f, 1f, alpha);
            shapeRenderer.circle(burstX, burstY, radius);
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if (showingRoundIntro) { renderRoundIntro(delta); return; }

        drawHealthBars();
        drawBattleUI();
        renderFloatingText(delta);

        if (isPaused) {
            drawPauseMenu();
            handlePauseInput();
        } else {
            handleCombatState(delta);
        }
    }

    // =========================================================================
    // ROUND INTRO
    // =========================================================================

    protected void renderRoundIntro(float delta) {
        if (!isPaused) {
            if (roundIntroTimer == 0f) playRoundSound();
            roundIntroTimer += delta;
        }
        batch.begin();
        if (roundIntroTimer < 1.5f) {
            Texture popUp = getRoundIntroTexture();
            if (popUp != null) batch.draw(popUp, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        } else if (roundIntroTimer < 2.5f) {
            if (fightImg != null) batch.draw(fightImg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        } else {
            showingRoundIntro = false;
            roundIntroTimer   = 0f;
        }
        batch.end();
    }

    protected Texture getRoundIntroTexture() {
        // Show correct round image based on current round and win counts
        if (playerWins == 1 && enemyWins == 1) {
            return round3Img; // Final round
        } else if (currentRound == 1 && playerWins == 0 && enemyWins == 0) {
            return round1Img; // First round of match
        } else if (currentRound == 2) {
            return round2Img; // Second round
        } else if (currentRound == 3) {
            return round3Img; // Third/final round
        }
        return round1Img; // Default fallback
    }

    // =========================================================================
    // FLOATING TEXT
    // =========================================================================

    private void renderFloatingText(float delta) {
        batch.begin();
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.y    += 100 * delta;
            ft.timer -= delta;
            font.setColor(ft.color.r, ft.color.g, ft.color.b, Math.max(0, ft.timer / 1.5f));
            GlyphLayout layout = new GlyphLayout(font, ft.text);
            font.draw(batch, ft.text, ft.x - layout.width / 2f, ft.y);
            if (ft.timer <= 0) floatingTexts.remove(i);
        }
        font.setColor(Color.WHITE);
        batch.end();
    }

    // =========================================================================
    // COMBAT STATE — draws transition message, then waits 2s
    // =========================================================================

    protected void handleCombatState(float delta) {
        if (isTransitioning) {
            transitionTimer += delta;

            batch.begin();
            font.getData().setScale(5.0f);
            font.setColor(Color.YELLOW);
            GlyphLayout tl = new GlyphLayout(font, transitionMessage);
            font.draw(batch, tl, (WORLD_WIDTH - tl.width) / 2f, WORLD_HEIGHT / 2f + 100);
            font.getData().setScale(2.5f);
            font.setColor(Color.WHITE);
            batch.end();

            if (transitionTimer >= 2.0f) {
                if (matchIsOver) onMatchOver(playerWins >= 2);
                else { isTransitioning = false; transitionTimer = 0; resetRound(); }
            }
        } else {
            handleGameLogic(delta);
        }
    }

    // =========================================================================
    // GAME LOGIC
    // =========================================================================

    protected void handleGameLogic(float delta) {
        if (isPlayerTurn || isPVPMode()) {
            if (Gdx.input.justTouched()) {
                if      (skill1Bounds.contains(touch.x, touch.y)) { pressedSkillIndex = 0; Main.clickSound.play(); }
                else if (skill2Bounds.contains(touch.x, touch.y)) { pressedSkillIndex = 1; Main.clickSound.play(); }
                else if (skill3Bounds.contains(touch.x, touch.y)) { pressedSkillIndex = 2; Main.clickSound.play(); }
            }
            if (!Gdx.input.isTouched() && pressedSkillIndex != -1) {
                if (getBounds(pressedSkillIndex).contains(touch.x, touch.y)) executeSkill(pressedSkillIndex);
                pressedSkillIndex = -1;
            }
        } else {
            turnTimer += delta;
            if (turnTimer >= turnDelay) { turnTimer = 0; executeEnemyTurn(); }
        }
    }

    // =========================================================================
    // EXECUTE SKILL - Cooldowns only decrement on current player's turn
    // =========================================================================

    protected void executeSkill(int index) {
        if (index < 0 || index > 2) return;

        Character attacker = isPlayerTurn ? player : enemy;
        Character defender = isPlayerTurn ? enemy  : player;
        int[]  activeCD    = isPlayerTurn ? playerCD : enemyCD;
        int    cost        = attacker.getSkills().get(index).getManaCost();

        if (attacker.getCurrentMana() < cost || activeCD[index] > 0) return;

        float startX  = isPlayerTurn ? 450 : WORLD_WIDTH - 450;
        float targetX = isPlayerTurn ? WORLD_WIDTH - 450 : 450;
        int   skillNum = index + 1;

        Animation<TextureRegion> skillAnim = isPlayerTurn
                ? animManager.getSkillAnimation(playerCharName, skillNum)
                : animManager.getSkillAnimation(enemyCharName,  skillNum);

        activeEffects.add(new SkillEffect(skillAnim, startX, 600, targetX, 600, !isPlayerTurn));

        if (isPlayerTurn) playerSkillPoseTimer = SKILL_POSE_DURATION;
        else              enemySkillPoseTimer  = SKILL_POSE_DURATION;

        int oldHp = defender.getHealth();

        // Set cooldowns ONLY for the skill being used
        switch (index) {
            case 0:
                attacker.basicAttack(defender);
                // Basic attack has no cooldown
                break;
            case 1:
                attacker.secondarySkill(defender);
                activeCD[1] = 3;  // Secondary skill: 3-turn cooldown
                break;
            case 2:
                attacker.ultimateSkill(defender);
                activeCD[2] = 5;  // Ultimate skill: 5-turn cooldown
                break;
        }

        spawnCombatText(oldHp - defender.getHealth(), cost);

        // Switch turns FIRST
        isPlayerTurn = !isPlayerTurn;
        turnTimer    = 0;

        // Decrement cooldowns ONLY for the NEW current player
        decrementCooldowns();

        checkMatchState();
    }

    // =========================================================================
    // DECREMENT COOLDOWNS - Cooldowns ONLY decrement on current player's turn
    // =========================================================================

    protected void decrementCooldowns() {
        int[] currentCD = isPlayerTurn ? playerCD : enemyCD;

        // Only decrement cooldowns for the CURRENT player whose turn it is
        for (int i = 0; i < 3; i++) {
            if (currentCD[i] > 0) {
                currentCD[i]--;
            }
        }

        // Add mana to both players every turn
        player.addMana(random.nextInt(6) + 5);
        enemy.addMana(random.nextInt(6) + 5);
    }

    // =========================================================================
    // COMBAT TEXT
    // =========================================================================

    private void spawnCombatText(int damage, int cost) {
        float pX = 200 + 225f, eX = WORLD_WIDTH - 700 + 225f;
        if (isPlayerTurn) {
            if (damage > 0) floatingTexts.add(new FloatingText("-" + damage,        eX, 850, Color.RED));
            if (cost   > 0) floatingTexts.add(new FloatingText("-" + cost + " MP",  pX, 800, Color.CYAN));
        } else {
            if (damage > 0) floatingTexts.add(new FloatingText("-" + damage,        pX, 850, Color.RED));
            if (cost   > 0) floatingTexts.add(new FloatingText("-" + cost + " MP",  eX, 800, Color.CYAN));
        }
    }

    // =========================================================================
    // CHECK MATCH STATE
    // =========================================================================

    protected void checkMatchState() {
        if (!player.isAlive() || !enemy.isAlive()) {
            isTransitioning = true;
            transitionTimer = 0;

            if (player.isAlive()) {
                playerWins++;
                transitionMessage = isPVPMode()
                        ? username.toUpperCase() + " WINS ROUND " + currentRound + "!"
                        : "YOU WIN ROUND " + currentRound + "!";
            } else {
                enemyWins++;
                transitionMessage = isPVPMode()
                        ? player2Name.toUpperCase() + " WINS ROUND " + currentRound + "!"
                        : enemy.getName().toUpperCase() + " WINS ROUND " + currentRound + "!";
            }

            // Overwrite with match-over message if someone reached 2 wins
            if (playerWins == 2 || enemyWins == 2) {
                matchIsOver = true;
                if (isPVPMode()) {
                    transitionMessage = (playerWins == 2)
                            ? username.toUpperCase()    + " WINS THE MATCH!"
                            : player2Name.toUpperCase() + " WINS THE MATCH!";
                } else {
                    transitionMessage = (playerWins == 2) ? "VICTORY!" : "DEFEATED!";
                }
            }
        }
    }

    // =========================================================================
    // RESET ROUND
    // =========================================================================

    protected void resetRound() {
        player.restoreHP();   player.restoreMana();
        enemy.restoreHP();    enemy.restoreMana();
        playerCD = new int[]{0, 0, 0};
        enemyCD  = new int[]{0, 0, 0};
        player1GoesFirstNextRound = random.nextBoolean();
        isPlayerTurn         = player1GoesFirstNextRound;
        showingRoundIntro    = true;
        roundIntroTimer      = 0f;
        activeEffects.clear();
        hitActive            = false;
        burstActive          = false;
        playerSkillPoseTimer = 0f;
        enemySkillPoseTimer  = 0f;

        // Only increment round if match is NOT over
        if (!matchIsOver) {
            currentRound++;
        }
    }

    // =========================================================================
    // RESET FOR NEW STAGE
    // =========================================================================

    protected void resetForNewStage() {
        currentRound = 1;
        playerWins = 0;
        enemyWins = 0;
        matchIsOver = false;
        isTransitioning = false;
        transitionTimer = 0;
        player1GoesFirstNextRound = random.nextBoolean();

        player.restoreHP();   player.restoreMana();
        enemy.restoreHP();    enemy.restoreMana();
        playerCD = new int[]{0, 0, 0};
        enemyCD  = new int[]{0, 0, 0};
        isPlayerTurn = player1GoesFirstNextRound;
        showingRoundIntro    = true;
        roundIntroTimer      = 0f;
        activeEffects.clear();
        hitActive            = false;
        burstActive          = false;
        playerSkillPoseTimer = 0f;
        enemySkillPoseTimer  = 0f;
    }

    // =========================================================================
    // PAUSE MENU
    // =========================================================================

    protected void drawPauseMenu() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        batch.draw(safe(dialogueBox),
                dialogueBounds.x, dialogueBounds.y, dialogueBounds.width, dialogueBounds.height);

        boolean tp = playBounds.contains(touch.x, touch.y) && Gdx.input.isTouched();
        batch.draw(safe(tp ? playBtnP : playBtn),
                playBounds.x, playBounds.y, playBounds.width, playBounds.height);

        Texture mNorm = isMuted ? unmuteBtn  : muteBtn;
        Texture mPres = isMuted ? unmuteBtnP : muteBtnP;
        boolean tm = muteBounds.contains(touch.x, touch.y) && Gdx.input.isTouched();
        batch.draw(safe(tm ? mPres : mNorm),
                muteBounds.x, muteBounds.y, muteBounds.width, muteBounds.height);

        boolean te = exitBounds.contains(touch.x, touch.y) && Gdx.input.isTouched();
        batch.draw(safe(te ? exitBtnP : exitBtn),
                exitBounds.x, exitBounds.y, exitBounds.width, exitBounds.height);
        batch.end();
    }

    protected void handlePauseInput() {
        if (Gdx.input.justTouched()) {
            if (playBounds.contains(touch.x, touch.y)) { playPressed = true; Main.clickSound.play(); }
            if (muteBounds.contains(touch.x, touch.y)) { mutePressed = true; Main.clickSound.play(); }
            if (exitBounds.contains(touch.x, touch.y)) { exitPressed = true; Main.clickSound.play(); }
        }
        if (!Gdx.input.isTouched()) {
            if (playPressed && playBounds.contains(touch.x, touch.y)) isPaused = false;
            if (mutePressed && muteBounds.contains(touch.x, touch.y)) {
                isMuted = !isMuted;
                if (Main.bgm != null) { if (isMuted) Main.bgm.pause(); else Main.bgm.play(); }
            }
            if (exitPressed && exitBounds.contains(touch.x, touch.y)) {
                game.setScreen(new MainMenu(game)); dispose();
            }
            playPressed = mutePressed = exitPressed = false;
        }
    }

    // =========================================================================
    // HEALTH BARS
    // =========================================================================

    protected void drawHealthBars() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(160, 950, 450, 50);
        float pP = (float) player.getHealth() / player.getMaxHealth();
        shapeRenderer.setColor(pP > 0.3f ? Color.GREEN : Color.RED);
        shapeRenderer.rect(165, 955, 440 * pP, 40);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(WORLD_WIDTH - 610, 950, 450, 50);
        float eP = (float) enemy.getHealth() / enemy.getMaxHealth();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(WORLD_WIDTH - 605, 955, 440 * eP, 40);

        for (int i = 0; i < 2; i++) {
            shapeRenderer.setColor(i < playerWins ? Color.GOLD : Color.DARK_GRAY);
            shapeRenderer.rect(580 - (i * 35), 915, 25, 25);
            shapeRenderer.setColor(i < enemyWins ? Color.GOLD : Color.DARK_GRAY);
            shapeRenderer.rect(1315 + (i * 35), 915, 25, 25);
        }
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        String pHealth = player.getHealth() + " / " + player.getMaxHealth();
        GlyphLayout pLayout = new GlyphLayout(font, pHealth);
        font.draw(batch, pHealth, 160 + (450 - pLayout.width) / 2f, 988);

        String eHealth = enemy.getHealth() + " / " + enemy.getMaxHealth();
        GlyphLayout eLayout = new GlyphLayout(font, eHealth);
        font.draw(batch, eHealth, (WORLD_WIDTH - 610) + (450 - eLayout.width) / 2f, 988);
        batch.end();
    }

    // =========================================================================
    // BATTLE UI
    // =========================================================================

    protected void drawBattleUI() {
        batch.begin();
        if (yellowUi != null) batch.draw(yellowUi, 40,   20, 1420, 320);
        if (redUi    != null) {
            batch.draw(redUi, 140,  60,  350, 250);
            batch.draw(redUi, 1480, 20,  400, 300);
        }

        Character uiChar      = (isPVPMode() && !isPlayerTurn) ? enemy   : player;
        int[]     uiCD        = (isPVPMode() && !isPlayerTurn) ? enemyCD : playerCD;
        boolean   disableBtns = !isPlayerTurn && !isPVPMode();

        if (uiChar.getSkills() != null && uiChar.getSkills().size() >= 3) {
            drawSkillUI(0, uiChar.getSkills().get(0).getName(), skill1Bounds, uiCD[0], uiChar, disableBtns);
            drawSkillUI(1, uiChar.getSkills().get(1).getName(), skill2Bounds, uiCD[1], uiChar, disableBtns);
            drawSkillUI(2, uiChar.getSkills().get(2).getName(), skill3Bounds, uiCD[2], uiChar, disableBtns);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "What will\n" + uiChar.getName() + "\ndo?", 180, 240);
        font.draw(batch, "HP  - " + uiChar.getHealth()      + "/" + uiChar.getMaxHealth(), 1540, 220);
        font.draw(batch, "Mana- " + uiChar.getCurrentMana() + "/" + uiChar.getMaxMana(),   1540, 140);
        font.draw(batch, username + " (" + player.getName() + ")", 160, 1040);

        String p2Disp = (isPVPMode() ? player2Name : "CPU") + " (" + enemy.getName() + ")";
        GlyphLayout p2L = new GlyphLayout(font, p2Disp);
        font.draw(batch, p2Disp, WORLD_WIDTH - 160 - p2L.width, 1040);

        GlyphLayout hudL = new GlyphLayout(font, getHUDText());
        font.draw(batch, hudL, (WORLD_WIDTH - hudL.width) / 2f, 1040);

        if (playerIcon    != null) batch.draw(playerIcon,    30,               910, 120, 120);
        if (enemyIconRegion != null) batch.draw(enemyIconRegion, WORLD_WIDTH - 150, 910, 120, 120);

        String turnMsg = isPlayerTurn ? player.getName() + "'s Turn!" : enemy.getName() + "'s Turn...";
        font.setColor(isPlayerTurn ? Color.GREEN : Color.RED);
        GlyphLayout tL = new GlyphLayout(font, turnMsg);
        font.draw(batch, tL, (WORLD_WIDTH - tL.width) / 2f, 800);
        font.setColor(Color.WHITE);
        if (!isPaused && !isTransitioning && !showingRoundIntro && (isPlayerTurn || isPVPMode())) {
            drawHoverTooltip(uiChar);
        }

        batch.end();
    }

    private void drawSkillUI(int i, String name, Rectangle b, int cd, Character activeC, boolean disabled) {
        Texture normalTex  = (i == 0) ? skill1Btn  : (i == 1) ? skill2Btn  : skill3Btn;
        Texture pressedTex = (i == 0) ? skill1BtnP : (i == 1) ? skill2BtnP : skill3BtnP;

        boolean canAfford  = activeC.getCurrentMana() >= activeC.getSkills().get(i).getManaCost();
        boolean isTouching = !isPaused && !disabled && Gdx.input.isTouched() && b.contains(touch.x, touch.y);
        boolean usePressed = isTouching || cd > 0 || !canAfford;

        Texture toUse = safe(usePressed ? pressedTex : normalTex,
                usePressed ? fallbackBlack : fallbackWhite);
        batch.draw(toUse, b.x, b.y, 250, 70);

        font.setColor(cd > 0 || !canAfford || disabled ? Color.GRAY : Color.BLACK);
        font.draw(batch, "- " + name + (cd > 0 ? " (" + cd + ")" : ""), b.x + 280, b.y + 50);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    protected void randomizeBackground() {
        if (background != null) background.dispose();
        background = loadTexture("backgrounds/bg" + (random.nextInt(8) + 1) + ".png");
    }

    private Rectangle getBounds(int i) {
        return i == 0 ? skill1Bounds : (i == 1 ? skill2Bounds : skill3Bounds);
    }

    protected Character createCharacter(String name) {
        switch (name) {
            case "Jollibee":        return new Jollibee();
            case "Colonel Sanders": return new ColonelSanders();
            case "McDonald":        return new McDonald();
            case "Burger King":     return new BurgerKing();
            case "Wendy":           return new Wendy();
            case "Jack in the Box": return new JackInTheBox();
            case "Little Caesar":   return new LittleCaesar();
            case "Chief Khai":      return new ChiefKhai();
            case "Dev Kishanta":    return new GameDevs("Dev Kishanta");
            case "Dev Rothesa":     return new GameDevs("Dev Rothesa");
            case "Dev Wengie":      return new GameDevs("Dev Wengie");
            case "Dev Kunihiko":    return new GameDevs("Dev Kunihiko");
            case "Dev Ayella":      return new GameDevs("Dev Ayella");
            default:                return new Jollibee();
        }
    }

    private Texture loadTexture(String path) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.log("BaseBattleScreen", "Missing texture: " + path);
            return null;
        }
    }

    private Sound loadSound(String path) {
        try {
            return Gdx.audio.newSound(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.log("BaseBattleScreen", "Missing sound: " + path);
            return null;
        }
    }

    private void playRoundSound() {
        if (playerWins == 1 && enemyWins == 1) { if (finalRoundSound != null) finalRoundSound.play(); }
        else if (currentRound == 1)             { if (round1Sound     != null) round1Sound.play(); }
        else if (currentRound == 2)             { if (round2Sound     != null) round2Sound.play(); }
    }

    // =========================================================================
    // ABSTRACT / OVERRIDABLE
    // =========================================================================

    protected abstract boolean isPVPMode();
    protected abstract void executeEnemyTurn();
    protected abstract void onMatchOver(boolean playerWon);
    protected String getHUDText() { return "ROUND: " + currentRound; }

    // =========================================================================
    // tooltip
    // =========================================================================
    private void drawHoverTooltip(Character uiChar) {
        int hoveredIndex = -1;
        if      (skill1Bounds.contains(touch.x, touch.y)) hoveredIndex = 0;
        else if (skill2Bounds.contains(touch.x, touch.y)) hoveredIndex = 1;
        else if (skill3Bounds.contains(touch.x, touch.y)) hoveredIndex = 2;

        if (hoveredIndex != -1 && uiChar.getSkills() != null && uiChar.getSkills().size() > hoveredIndex) {
            Skill skill = uiChar.getSkills().get(hoveredIndex);
            font.getData().markupEnabled = true;
            String tooltipText = "[RED]"    + skill.getName()
                    + "\n[BLACK]Damage: [RED]"  + skill.getMinDmg() + " - " + skill.getMaxDmg()
                    + "\n[BLACK]Cost: [BLUE]"   + skill.getManaCost() + " MP";

            font.getData().setScale(2.0f);
            GlyphLayout layout = new GlyphLayout(font, tooltipText);

            float hPad = 50f, vPad = 40f;
            float boxW = layout.width  + (hPad * 2);
            float boxH = layout.height + (vPad * 2);
            float tipX = touch.x + 20f;
            float tipY = touch.y - 20f;

            if (tipX + boxW > WORLD_WIDTH) tipX = WORLD_WIDTH - boxW - 10f;
            if (tipY - boxH < 10f)         tipY = touch.y + boxH + 20f;

            if (dialogueBox != null) batch.draw(dialogueBox, tipX, tipY - boxH, boxW, boxH);
            font.setColor(Color.WHITE);
            font.draw(batch, tooltipText, tipX + hPad, tipY - vPad);
            font.getData().setScale(2.5f);
            font.getData().markupEnabled = false;
        }
    }
    // =========================================================================
    // SCREEN LIFECYCLE
    // =========================================================================

    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void show()    {}
    @Override public void hide()    {}
    @Override public void pause()   {}
    @Override public void resume()  {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
        if (background      != null) background.dispose();
        if (redUi           != null) redUi.dispose();
        if (yellowUi        != null) yellowUi.dispose();
        if (playerIcon      != null) playerIcon.dispose();
        if (enemyIcon       != null) enemyIcon.dispose();
        if (round1Img       != null) round1Img.dispose();
        if (round2Img       != null) round2Img.dispose();
        if (round3Img       != null) round3Img.dispose();
        if (fightImg        != null) fightImg.dispose();
        if (skill1Btn       != null) skill1Btn.dispose();
        if (skill1BtnP      != null) skill1BtnP.dispose();
        if (skill2Btn       != null) skill2Btn.dispose();
        if (skill2BtnP      != null) skill2BtnP.dispose();
        if (skill3Btn       != null) skill3Btn.dispose();
        if (skill3BtnP      != null) skill3BtnP.dispose();
        if (round1Sound     != null) round1Sound.dispose();
        if (round2Sound     != null) round2Sound.dispose();
        if (finalRoundSound != null) finalRoundSound.dispose();
        if (dialogueBox     != null) dialogueBox.dispose();
        if (playBtn         != null) playBtn.dispose();
        if (playBtnP        != null) playBtnP.dispose();
        if (muteBtn         != null) muteBtn.dispose();
        if (muteBtnP        != null) muteBtnP.dispose();
        if (unmuteBtn       != null) unmuteBtn.dispose();
        if (unmuteBtnP      != null) unmuteBtnP.dispose();
        if (exitBtn         != null) exitBtn.dispose();
        if (exitBtnP        != null) exitBtnP.dispose();
        if (fallbackWhite   != null) fallbackWhite.dispose();
        if (fallbackBlack   != null) fallbackBlack.dispose();
        animManager.dispose();
    }
}