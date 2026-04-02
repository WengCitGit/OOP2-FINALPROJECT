package io.github.PASAN.modes;

import io.github.PASAN.MainMenu;
import io.github.PASAN.characters.*;
import io.github.PASAN.characters.Character;
import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import java.util.Random;

public abstract class BaseBattleScreen implements Screen {
    public static String currentBackgroundPath = "backgrounds/bg1.png";
    // --- CORE GDX ---
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
    protected Texture background, playerSprite, enemySprite, playerIcon, enemyIcon;
    protected TextureRegion enemyRegion, enemyIconRegion;
    protected Texture redUi, yellowUi, skill1Btn, skill1BtnP, skill2Btn, skill2BtnP, skill3Btn, skill3BtnP;
    protected Texture round1Img, round2Img, round3Img, fightImg;
    protected Rectangle skill1Bounds, skill2Bounds, skill3Bounds;

    protected boolean isPaused = false, playPressed = false, mutePressed = false, exitPressed = false;
    protected Texture dialogueBox, playBtn, playBtnP, muteBtn, muteBtnP, unmuteBtn, unmuteBtnP, exitBtn, exitBtnP;
    protected Rectangle dialogueBounds, playBounds, muteBounds, exitBounds;
    protected boolean isMuted = false;

    // --- CHARACTERS ---
    protected Character player, enemy;

    // --- FLOATING TEXTS ---
    protected class FloatingText
    {
        String text;
        float x, y, timer;
        Color color;
        public FloatingText(String text, float x, float y, Color color)
        {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.timer = 1.5f;
        }
    }
    protected java.util.ArrayList<FloatingText> floatingTexts = new java.util.ArrayList<>();
    // --- STATE ---
    protected boolean isPlayerTurn = true;
    protected int pressedSkillIndex = -1;
    protected float turnDelay = 1.5f, turnTimer = 0;
    protected int[] playerCD = {0, 0, 0}, enemyCD = {0, 0, 0};
    protected Random random = new Random();

    protected int currentRound = 1, playerWins = 0, enemyWins = 0;
    protected boolean isTransitioning = false, matchIsOver = false, showingRoundIntro = true;
    protected float transitionTimer = 0, roundIntroTimer = 0f;
    protected String transitionMessage = "";

    protected static final float WORLD_WIDTH = 1920, WORLD_HEIGHT = 1080;

    // --- AUDIO ---
    protected Sound round1Sound, round2Sound, finalRoundSound;

    public BaseBattleScreen(Game game, String username, String playerCharName, String enemyCharName) {
        this.game = game;
        this.username = username;
        init(playerCharName, enemyCharName);
    }

    private void init(String playerCharName, String enemyCharName) {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2.5f);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();
        background = loadTexture(currentBackgroundPath);

        // Load Textures
        redUi = loadTexture("backgrounds/red_background.png");
        yellowUi = loadTexture("backgrounds/yellow_background.png");
        skill1Btn = loadTexture("buttons/skill1_button.png");
        skill1BtnP = loadTexture("buttons/skill1_button_pressed.png");
        skill2Btn = loadTexture("buttons/skill2_button.png");
        skill2BtnP = loadTexture("buttons/skill2_button_pressed.png");
        skill3Btn = loadTexture("buttons/skill3_button.png");
        skill3BtnP = loadTexture("buttons/skill3_button_pressed.png");
        round1Img = loadTexture("backgrounds/round1.png");
        round2Img = loadTexture("backgrounds/round2.png");
        round3Img = loadTexture("backgrounds/finalround.png");
        fightImg = loadTexture("backgrounds/fighttext.png");

        playerSprite = loadTexture("characters/" + playerCharName.replace(" ", "") + ".png");
        enemySprite = loadTexture("characters/" + enemyCharName.replace(" ", "") + ".png");
        if (enemySprite != null) {
            enemyRegion = new TextureRegion(enemySprite);
            enemyRegion.flip(true, false);
        }

        playerIcon = loadTexture("icons/" + playerCharName.replace(" ", "") + "_icon.png");
        enemyIcon = loadTexture("icons/" + enemyCharName.replace(" ", "") + "_icon.png");
        if (enemyIcon != null) {
            enemyIconRegion = new TextureRegion(enemyIcon);
            enemyIconRegion.flip(true, false);
        }

        player = createCharacter(playerCharName);
        enemy = createCharacter(enemyCharName);

        skill1Bounds = new Rectangle(555, 230, 250, 70);
        skill2Bounds = new Rectangle(555, 145, 250, 70);
        skill3Bounds = new Rectangle(555, 60, 250, 70);

        // Audio
        round1Sound = loadSound("audio/round1_audio.wav");
        round2Sound = loadSound("audio/round2_audio.wav");
        finalRoundSound = loadSound("audio/finalround_audio.wav");

        // --- PAUSE MENU INIT ---
        dialogueBox = loadTexture("backgrounds/dialogue-box.png");
        playBtn = loadTexture("buttons/resume_button.png");
        playBtnP = loadTexture("buttons/resume_button_pressed.png");
        muteBtn = loadTexture("buttons/mute_button.png");
        muteBtnP = loadTexture("buttons/mute_button_pressed.png");
        unmuteBtn = loadTexture("buttons/unmute_button.png");
        unmuteBtnP = loadTexture("buttons/unmute_button_pressed.png");
        exitBtn = loadTexture("buttons/exit_button.png");
        exitBtnP = loadTexture("buttons/exit_button_pressed.png");

        float cx = WORLD_WIDTH / 2f;
        float cy = WORLD_HEIGHT / 2f;

        float boxW = 800f;
        float boxH = 600f;
        dialogueBounds = new Rectangle(cx - boxW / 2f, cy - boxH / 2f, boxW, boxH);

        float btnW = 350f;
        float btnH = 100f;
        playBounds = new Rectangle(cx - btnW / 2f, cy + 100f, btnW, btnH);
        muteBounds = new Rectangle(cx - btnW / 2f, cy - 20f, btnW, btnH);
        exitBounds = new Rectangle(cx - btnW / 2f, cy - 140f, btnW, btnH);
    }

    protected void randomizeBackground() {
        if (background != null) {
            background.dispose();
        }
        int randomBgNum = random.nextInt(8) + 1;
        currentBackgroundPath = "backgrounds/bg" + randomBgNum + ".png";
        background = loadTexture(currentBackgroundPath);
    }
    private void playRoundSound() {
        if (playerWins == 1 && enemyWins == 1) { if (finalRoundSound != null) finalRoundSound.play(); }
        else if (currentRound == 1) { if (round1Sound != null) round1Sound.play(); }
        else if (currentRound == 2) { if (round2Sound != null) round2Sound.play(); }
    }

    protected Character createCharacter(String name) {
        switch (name) {
            case "Jollibee": return new Jollibee();
            case "Colonel Sanders": return new ColonelSanders();
            case "McDonald": return new McDonald();
            case "Burger King": return new BurgerKing();
            case "Wendy": return new Wendy();
            case "Jack in the Box": return new JackInTheBox();
            case "Little Caesar": return new LittleCaesar();
            case "Chief Khai": return new ChiefKhai();
            case "Dev Kishanta": return new GameDevs("Dev Kishanta");
            case "Dev Rothesa":  return new GameDevs("Dev Rothesa");
            case "Dev Wengie":   return new GameDevs("Dev Wengie");
            case "Dev Kunihiko": return new GameDevs("Dev Kunihiko");
            case "Dev Ayella":   return new GameDevs("Dev Ayella");
            default: return new Jollibee();
        }
    }

    private Texture loadTexture(String path) {
        try { return new Texture(path); } catch (Exception e) { return null; }
    }

    private Sound loadSound(String path) {
        try { return Gdx.audio.newSound(Gdx.files.internal(path)); } catch (Exception e) { return null; }
    }

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            isPaused = !isPaused;
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (background != null) batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (showingRoundIntro) {
            if (playerSprite != null) batch.draw(playerSprite, 100, 100, 750, 850);
            if (enemyRegion != null) batch.draw(enemyRegion, WORLD_WIDTH - 850, 100, 750, 850);
        } else {
            if (playerSprite != null) batch.draw(playerSprite, 200, 350, 450, 500);
            if (enemyRegion != null) batch.draw(enemyRegion, WORLD_WIDTH - 700, 350, 450, 500);
        }
        batch.end();

        if (showingRoundIntro) {
            if(!isPaused)
            {
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
                roundIntroTimer = 0f;
            }
            batch.end();
            return;
        }

        drawHealthBars();
        drawBattleUI();

        // --- RENDER FLOATING TEXT ---
        batch.begin();
        for (int i = floatingTexts.size() - 1; i >= 0; i--)
        {
            FloatingText ft = floatingTexts.get(i);
            ft.y += 100 * delta;
            ft.timer -= delta;

            font.setColor(ft.color.r, ft.color.g, ft.color.b, Math.max(0, ft.timer / 1.5f));
            GlyphLayout layout = new GlyphLayout(font, ft.text);
            font.draw(batch, ft.text, ft.x - layout.width / 2f, ft.y);

            if (ft.timer <= 0) {

                floatingTexts.remove(i);
            }
        }
        font.setColor(Color.WHITE);
        batch.end();

        if (isPaused) {
            drawPauseMenu();
            handlePauseInput();
        } else if (!showingRoundIntro) {
            if (isTransitioning) {
                transitionTimer += delta;
                if (transitionTimer >= 2.0f) {
                    if (matchIsOver) onMatchOver(playerWins >= 2);
                    else {
                        isTransitioning = false;
                        transitionTimer = 0;
                        resetRound();
                    }
                }
            } else {
                handleGameLogic(delta);
            }
        }
    }

    private Texture getRoundIntroTexture() {
        if (playerWins == 1 && enemyWins == 1) return round3Img;
        if (currentRound == 1) return round1Img;
        if (currentRound == 2) return round2Img;
        return round3Img;
    }

    private void drawBattleUI() {
        batch.begin();
        if (yellowUi != null) batch.draw(yellowUi, 40, 20, 1420, 320);
        if (redUi != null) {
            batch.draw(redUi, 140, 60, 350, 250);
            batch.draw(redUi, 1480, 20, 400, 300);
        }

        Character uiChar = (isPVPMode() && !isPlayerTurn) ? enemy : player;
        int[] uiCD = (isPVPMode() && !isPlayerTurn) ? enemyCD : playerCD;
        boolean disableBtns = !isPlayerTurn && !isPVPMode();

        if (uiChar.getSkills() != null && uiChar.getSkills().size() >= 3) {
            drawSkillUI(0, uiChar.getSkills().get(0).getName(), skill1Bounds, uiCD[0], uiChar, disableBtns);
            drawSkillUI(1, uiChar.getSkills().get(1).getName(), skill2Bounds, uiCD[1], uiChar, disableBtns);
            drawSkillUI(2, uiChar.getSkills().get(2).getName(), skill3Bounds, uiCD[2], uiChar, disableBtns);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "What will\n" + uiChar.getName() + "\ndo?", 180, 240);
        font.draw(batch, "HP  - " + uiChar.getHealth() + "/" + uiChar.getMaxHealth(), 1540, 220);
        font.draw(batch, "Mana- " + uiChar.getCurrentMana() + "/" + uiChar.getMaxMana(), 1540, 140);

        font.draw(batch, username + " (" + player.getName() + ")", 160, 1040);
        String p2Disp = (isPVPMode() ? player2Name : "CPU") + " (" + enemy.getName() + ")";
        GlyphLayout p2Layout = new GlyphLayout(font, p2Disp);
        font.draw(batch, p2Disp, WORLD_WIDTH - 160 - p2Layout.width, 1040);

        GlyphLayout hudLayout = new GlyphLayout(font, getHUDText());
        font.draw(batch, hudLayout, (WORLD_WIDTH - hudLayout.width) / 2f, 1040);

        if (playerIcon != null) batch.draw(playerIcon, 30, 910, 120, 120);
        if (enemyIconRegion != null) batch.draw(enemyIconRegion, WORLD_WIDTH - 150, 910, 120, 120);

        if (isTransitioning) {
            font.getData().setScale(5.0f);
            font.setColor(Color.GOLD);
            GlyphLayout tl = new GlyphLayout(font, transitionMessage);
            font.draw(batch, tl, (WORLD_WIDTH - tl.width) / 2, WORLD_HEIGHT / 2 + 100);
            font.getData().setScale(2.5f);
        } else {
            // Turn indicator
            String turnMsg = isPlayerTurn ? player.getName() + "'s Turn!" : enemy.getName() + "'s Turn...";
            font.setColor(isPlayerTurn ? Color.YELLOW : Color.RED);
            GlyphLayout turnLayout = new GlyphLayout(font, turnMsg);
            font.draw(batch, turnLayout, (WORLD_WIDTH - turnLayout.width) / 2f, 800);
        }

        //--- HP INDICATORS ---
        font.setColor(Color.WHITE);
        String pHealth = player.getHealth()+" / "+player.getMaxHealth();
        String eHealth = enemy.getHealth()+" / "+enemy.getMaxHealth();

        GlyphLayout pHealthLayout = new GlyphLayout(font, pHealth);
        GlyphLayout eHealthLayout = new GlyphLayout(font, eHealth);

        font.draw(batch, pHealth, 160 + (450 - pHealthLayout.width) / 2f, 950 + 38);
        font.draw(batch, eHealth, (WORLD_WIDTH - 610) + (450 - eHealthLayout.width) / 2f, 950 + 38);

        batch.end();
    }

    private void drawHealthBars() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Player HP Bar — turns red when low
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(160, 950, 450, 50);
        float pPercent = (float) player.getHealth() / player.getMaxHealth();
        shapeRenderer.setColor(pPercent > 0.3f ? Color.GREEN : Color.RED);
        shapeRenderer.rect(165, 955, 440 * pPercent, 40);

        // Enemy HP Bar
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(WORLD_WIDTH - 610, 950, 450, 50);
        float ePercent = (float) enemy.getHealth() / enemy.getMaxHealth();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(WORLD_WIDTH - 605, 955, 440 * ePercent, 40);

        // Win Orbs
        for (int i = 0; i < 2; i++) {
            shapeRenderer.setColor(i < playerWins ? Color.GOLD : Color.DARK_GRAY);
            shapeRenderer.rect(580 - (i * 35), 915, 25, 25);
            shapeRenderer.setColor(i < enemyWins ? Color.GOLD : Color.DARK_GRAY);
            shapeRenderer.rect(1315 + (i * 35), 915, 25, 25);
        }
        shapeRenderer.end();
    }

    private void drawSkillUI(int i, String name, Rectangle bounds, int cd, Character activeChar, boolean disabled) {
        Texture normal = (i == 0) ? skill1Btn : (i == 1) ? skill2Btn : skill3Btn;
        Texture pressed = (i == 0) ? skill1BtnP : (i == 1) ? skill2BtnP : skill3BtnP;

        boolean canAfford = activeChar.getCurrentMana() >= activeChar.getSkills().get(i).getManaCost();
        boolean isTouching = !isPaused && !disabled && Gdx.input.isTouched() && bounds.contains(touch.x, touch.y);

        Texture toUse = (isTouching || cd > 0 || !canAfford) ? pressed : normal;
        batch.draw(toUse, bounds.x, bounds.y, 250, 70);

        font.setColor(cd > 0 || !canAfford || disabled ? Color.GRAY : Color.BLACK);
        font.draw(batch, "- " + name + (cd > 0 ? " (" + cd + ")" : ""), bounds.x + 280, bounds.y + 50);
    }

    protected void handleGameLogic(float delta) {
        if (isPlayerTurn || isPVPMode()) {
            if (Gdx.input.justTouched()) {
                if      (skill1Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 0;
                else if (skill2Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 1;
                else if (skill3Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 2;
            }
            if (!Gdx.input.isTouched() && pressedSkillIndex != -1) {
                if (getBounds(pressedSkillIndex).contains(touch.x, touch.y)) {
                    executeSkill(pressedSkillIndex);
                }
                pressedSkillIndex = -1;
            }
        } else {
            turnTimer += delta;
            if (turnTimer >= turnDelay) { turnTimer = 0; executeEnemyTurn(); }
        }
    }

    protected void executeSkill(int index) {
        if (index < 0 || index > 2) return;
        Character attacker = isPlayerTurn ? player : enemy;
        Character defender = isPlayerTurn ? enemy : player;
        int[] activeCD = isPlayerTurn ? playerCD : enemyCD;

        int cost = attacker.getSkills().get(index).getManaCost();
        if (attacker.getCurrentMana() < cost || activeCD[index] > 0) return;

        // --- CAPTURE OLD HP ---
        int oldHp = defender.getHealth();

        switch (index) {
            case 0: attacker.basicAttack(defender);    break;
            case 1: attacker.secondarySkill(defender); activeCD[1] = 3; break;
            case 2: attacker.ultimateSkill(defender);  activeCD[2] = 5; break;
        }

        // --- CALCULATE DAMAGE & SPAWN TEXT ---
        int damage = oldHp - defender.getHealth();

        // Calculate center top of character sprites
        float pSpriteX = 200 + (450 / 2f);
        float eSpriteX = (WORLD_WIDTH - 700) + (450 / 2f);
        float yPos = 850f; // Above their heads

        if (isPlayerTurn)
        {
            if (damage > 0) floatingTexts.add(new FloatingText("-" + damage, eSpriteX, yPos, Color.RED));
            if (cost > 0) floatingTexts.add(new FloatingText("-" + cost + " MP", pSpriteX, yPos - 50, Color.CYAN));
        } else
        {
            if (damage > 0) floatingTexts.add(new FloatingText("-" + damage, pSpriteX, yPos, Color.RED));
            if (cost > 0) floatingTexts.add(new FloatingText("-" + cost + " MP", eSpriteX, yPos - 50, Color.CYAN));
        }

        if (!isPlayerTurn || isPVPMode()) {
            endOfRound();
        }
        isPlayerTurn = !isPlayerTurn;
        turnTimer = 0;
        checkMatchState();
    }

    protected void endOfRound() {
        for (int i = 0; i < 3; i++) {
            if (playerCD[i] > 0) playerCD[i]--;
            if (enemyCD[i]  > 0) enemyCD[i]--;
        }
        player.addMana(random.nextInt(6) + 5);
        enemy.addMana(random.nextInt(6) + 5);
    }

    protected void checkMatchState() {
        if (!player.isAlive() || !enemy.isAlive()) {
            isTransitioning = true;
            transitionTimer = 0;

            boolean p1WonRound = player.isAlive();
            if (p1WonRound) {
                playerWins++;
                if (isPVPMode()) {
                    transitionMessage = username.toUpperCase() + " WINS ROUND " + currentRound + "!";
                } else {
                    transitionMessage = "YOU WIN ROUND " + currentRound + "!";
                }
            } else {
                enemyWins++;
                if (isPVPMode()) {
                    transitionMessage = player2Name.toUpperCase() + " WINS ROUND " + currentRound + "!";
                } else {
                    transitionMessage = enemy.getName().toUpperCase() + " WINS ROUND " + currentRound + "!";
                }
            }

            if (playerWins == 2 || enemyWins == 2) {
                matchIsOver = true;

                if (isPVPMode()) {
                    transitionMessage = (playerWins == 2) ?
                            username.toUpperCase() + " WINS THE MATCH!" :
                            player2Name.toUpperCase() + " WINS THE MATCH!";
                } else {
                    transitionMessage = (playerWins == 2) ? "VICTORY!" : "DEFEATED!";
                }
            } else {
                currentRound++;
            }
        }
    }

    private Rectangle getBounds(int i) {
        if (i == 0) return skill1Bounds;
        if (i == 1) return skill2Bounds;
        return skill3Bounds;
    }

    protected void resetRound() {
        player.restoreHP(); player.restoreMana();
        enemy.restoreHP(); enemy.restoreMana();
        playerCD = new int[]{0,0,0}; enemyCD = new int[]{0,0,0};
        isPlayerTurn = true;
        showingRoundIntro = true;
        roundIntroTimer = 0f;
    }

    protected abstract boolean isPVPMode();
    protected abstract void executeEnemyTurn();
    protected abstract void onMatchOver(boolean playerWon);

    // Subclasses override getHUDText() to customise the centre HUD label.
    // getTopHUDText() is kept as an alias so both naming conventions work.
    protected String getHUDText() { return "ROUND: " + currentRound; }
    protected String getTopHUDText() { return getHUDText(); }

    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void show()   {}
    @Override public void hide()   {}
    @Override public void pause()  {}
    @Override public void resume() {}


    private void drawPauseMenu() {
        // 1. Draw a dark transparent overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();

        // 2. Draw the Dialogue Box background first
        if (dialogueBox != null) {
            batch.draw(dialogueBox, dialogueBounds.x, dialogueBounds.y, dialogueBounds.width, dialogueBounds.height);
        }

        // 3. Draw Resume Button
        if (Gdx.input.isTouched() && playBounds.contains(touch.x, touch.y)) {
            if (playBtnP != null) batch.draw(playBtnP, playBounds.x, playBounds.y, playBounds.width, playBounds.height);
        } else {
            if (playBtn != null) batch.draw(playBtn, playBounds.x, playBounds.y, playBounds.width, playBounds.height);
        }

        // 4. Draw Mute/Unmute Button (displays mute button if unmuted, unmute button if muted)
        Texture muteButtonToShow = isMuted ? unmuteBtn : muteBtn;
        Texture muteButtonPressed = isMuted ? unmuteBtnP : muteBtnP;

        if (Gdx.input.isTouched() && muteBounds.contains(touch.x, touch.y)) {
            if (muteButtonPressed != null) batch.draw(muteButtonPressed, muteBounds.x, muteBounds.y, muteBounds.width, muteBounds.height);
        } else {
            if (muteButtonToShow != null) batch.draw(muteButtonToShow, muteBounds.x, muteBounds.y, muteBounds.width, muteBounds.height);
        }

        // 5. Draw Exit Button
        if (Gdx.input.isTouched() && exitBounds.contains(touch.x, touch.y)) {
            if (exitBtnP != null) batch.draw(exitBtnP, exitBounds.x, exitBounds.y, exitBounds.width, exitBounds.height);
        } else {
            if (exitBtn != null) batch.draw(exitBtn, exitBounds.x, exitBounds.y, exitBounds.width, exitBounds.height);
        }

        batch.end();
    }

    private void handlePauseInput() {
        if (Gdx.input.justTouched()) {
            if (playBounds.contains(touch.x, touch.y)) playPressed = true;
            if (muteBounds.contains(touch.x, touch.y)) mutePressed = true;
            if (exitBounds.contains(touch.x, touch.y)) exitPressed = true;
        }

        if (!Gdx.input.isTouched()) {
            if (playPressed && playBounds.contains(touch.x, touch.y)) {
                isPaused = false;
            } else if (mutePressed && muteBounds.contains(touch.x, touch.y)) {
                isMuted = !isMuted;
                // TODO: Implement actual mute/unmute logic for audio
                // Gdx.audio.setVolume(isMuted ? 0.0f : 1.0f);
            } else if (exitPressed && exitBounds.contains(touch.x, touch.y)) {
                game.setScreen(new MainMenu(game));
                dispose();
            }
            playPressed = false;
            mutePressed = false;
            exitPressed = false;
        }
    }

    @Override
    public void dispose() {
        batch.dispose(); font.dispose(); shapeRenderer.dispose();

        if (background   != null) background.dispose();
        if (redUi        != null) redUi.dispose();
        if (yellowUi     != null) yellowUi.dispose();
        if (playerSprite != null) playerSprite.dispose();
        if (enemySprite  != null) enemySprite.dispose();
        if (playerIcon   != null) playerIcon.dispose();
        if (enemyIcon    != null) enemyIcon.dispose();

        if (round1Img != null) round1Img.dispose();
        if (round2Img != null) round2Img.dispose();
        if (round3Img != null) round3Img.dispose();
        if (fightImg  != null) fightImg.dispose();

        if (skill1Btn  != null) skill1Btn.dispose();
        if (skill1BtnP != null) skill1BtnP.dispose();
        if (skill2Btn  != null) skill2Btn.dispose();
        if (skill2BtnP != null) skill2BtnP.dispose();
        if (skill3Btn  != null) skill3Btn.dispose();
        if (skill3BtnP != null) skill3BtnP.dispose();

        if (round1Sound    != null) round1Sound.dispose();
        if (round2Sound    != null) round2Sound.dispose();
        if (finalRoundSound != null) finalRoundSound.dispose();

        if (dialogueBox != null) dialogueBox.dispose();
        if (playBtn != null) playBtn.dispose();
        if (playBtnP != null) playBtnP.dispose();
        if (muteBtn != null) muteBtn.dispose();
        if (muteBtnP != null) muteBtnP.dispose();
        if (unmuteBtn != null) unmuteBtn.dispose();
        if (unmuteBtnP != null) unmuteBtnP.dispose();
        if (exitBtn != null) exitBtn.dispose();
        if (exitBtnP != null) exitBtnP.dispose();
    }
}