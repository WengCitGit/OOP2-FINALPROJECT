package io.github.PASAN;

import io.github.PASAN.characters.*;
import io.github.PASAN.characters.Character;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import java.util.Random;

public abstract class BaseBattleScreen implements Screen {

    // --- CORE GDX ---
    protected Game game;
    protected SpriteBatch batch;
    protected BitmapFont font;
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected ShapeRenderer shapeRenderer;
    protected Vector3 touch;

    // --- USER DATA ---
    protected String username;
    protected String player2Name = "Player 2";

    // --- TEXTURES ---
    protected Texture background;
    protected Texture playerSprite;
    protected Texture enemySprite;
    protected TextureRegion enemyRegion;
    protected Texture playerIcon;
    protected Texture enemyIcon;
    protected TextureRegion enemyIconRegion;

    protected Texture redUi, yellowUi;
    protected Texture skill1Btn, skill1BtnP;
    protected Texture skill2Btn, skill2BtnP;
    protected Texture skill3Btn, skill3BtnP;

    // --- ROUND POP-UP IMAGES ---
    protected Texture round1Img;
    protected Texture round2Img;
    protected Texture round3Img; // Can also act as "Final Round"
    protected Texture fightImg;

    protected Rectangle skill1Bounds, skill2Bounds, skill3Bounds;

    // --- GAME LOGIC & MATH ---
    protected Character player;
    protected Character enemy;

    protected boolean isPlayerTurn = true;
    protected int pressedSkillIndex = -1;
    protected float turnDelay = 1.5f;
    protected float turnTimer = 0;

    protected int[] playerCD = {0, 0, 0};
    protected int[] enemyCD  = {0, 0, 0};
    protected Random random = new Random();

    // --- ROUND & TRANSITION SYSTEM ---
    protected int currentRound = 1;
    protected int playerWins = 0;
    protected int enemyWins = 0;
    protected boolean isTransitioning = false;
    protected boolean matchIsOver = false;
    protected float transitionTimer = 0;
    protected String transitionMessage = "";

    // --- ROUND INTRO CINEMATIC ---
    protected boolean showingRoundIntro = true;
    protected float roundIntroTimer = 0f;

    protected static final float WORLD_WIDTH  = 1920;
    protected static final float WORLD_HEIGHT = 1080;

    public BaseBattleScreen(Game game, String username, String playerCharName, String enemyCharName) {
        this.game = game;
        this.username = username;
        init(playerCharName, enemyCharName);
    }

    private void init(String playerCharName, String enemyCharName) {
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font          = new BitmapFont();
        font.getData().setScale(2.5f);

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();

        background = loadTexture("backgrounds/bg2.png");
        redUi      = loadTexture("backgrounds/red_background.png");
        yellowUi   = loadTexture("backgrounds/yellow_background.png");
        skill1Btn  = loadTexture("buttons/skill1_button.png");
        skill1BtnP = loadTexture("buttons/skill1_button_pressed.png");
        skill2Btn  = loadTexture("buttons/skill2_button.png");
        skill2BtnP = loadTexture("buttons/skill2_button_pressed.png");
        skill3Btn  = loadTexture("buttons/skill3_button.png");
        skill3BtnP = loadTexture("buttons/skill3_button_pressed.png");

        // Load your pop-up images here! (Change the file paths to match your assets folder)
        round1Img = loadTexture("backgrounds/round1.png");
        round2Img = loadTexture("backgrounds/round2.png");
        round3Img = loadTexture("backgrounds/finalround.png"); // Make sure you have these files!
        fightImg  = loadTexture("backgrounds/fighttext.png");

        playerSprite = loadTexture("characters/" + playerCharName.replace(" ", "") + ".png");
        enemySprite  = loadTexture("characters/" + enemyCharName.replace(" ", "") + ".png");

        if (enemySprite != null) {
            enemyRegion = new TextureRegion(enemySprite);
            enemyRegion.flip(true, false);
        }

        playerIcon = loadTexture("icons/" + playerCharName.replace(" ", "") + "_icon.png");
        enemyIcon  = loadTexture("icons/" + enemyCharName.replace(" ", "") + "_icon.png");

        if (enemyIcon != null) {
            enemyIconRegion = new TextureRegion(enemyIcon);
            enemyIconRegion.flip(true, false);
        }

        player = createCharacterInstance(playerCharName);
        enemy  = createCharacterInstance(enemyCharName);

        skill1Bounds = new Rectangle(555, 230, 250, 70);
        skill2Bounds = new Rectangle(555, 145, 250, 70);
        skill3Bounds = new Rectangle(555,  60, 250, 70);
    }

    private Texture loadTexture(String path) {
        try { return new Texture(path); }
        catch (Exception e) { return null; }
    }

    private Character createCharacterInstance(String name) {
        switch (name) {
            case "Jollibee":        return new Jollibee();
            case "Colonel Sanders": return new ColonelSanders();
            case "McDonald":        return new McDonald();
            case "Burger King":     return new BurgerKing();
            case "Wendy":           return new Wendy();
            case "Jack in the Box": return new JackInTheBox();
            case "Little Caesar":   return new LittleCaesar();
            case "Chief Khai":      return new ChiefKhai();
            default:                return new Jollibee();
        }
    }

    // ===============================
    // RENDER & UI DRAWING
    // ===============================

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        // 1. DRAW BACKGROUND & DYNAMIC SIZED CHARACTERS
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (background != null) batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (showingRoundIntro) {
            // CINEMATIC SIZE: Massive characters while the banner is on screen
            if (playerSprite != null) batch.draw(playerSprite, 100, 100, 750, 850);
            if (enemyRegion  != null) batch.draw(enemyRegion, WORLD_WIDTH - 850, 100, 750, 850);
        } else {
            // BATTLE SIZE: Normal characters when the fight starts
            if (playerSprite != null) batch.draw(playerSprite, 200, 350, 450, 500);
            if (enemyRegion  != null) batch.draw(enemyRegion, WORLD_WIDTH - 700, 350, 450, 500);
        }
        batch.end();

        // 2. ROUND INTRO CINEMATIC OVERLAY
        if (showingRoundIntro) {
            roundIntroTimer += delta;
            batch.begin();

            // Your custom 1920x275 image dimensions
            float popUpWidth = WORLD_WIDTH; // 1920
            float popUpHeight = 275;
            float popUpY = (WORLD_HEIGHT - popUpHeight) / 2f; // Vertically centers the 275px image

            if (roundIntroTimer < 1.5f) {
                Texture currentPopUp = null;

                if (playerWins == 1 && enemyWins == 1) {
                    currentPopUp = round3Img; // Final Round tie-breaker
                } else if (currentRound == 1) {
                    currentPopUp = round1Img;
                } else if (currentRound == 2) {
                    currentPopUp = round2Img;
                } else {
                    currentPopUp = round3Img; // Fallback
                }

                if (currentPopUp != null) {
                    batch.draw(currentPopUp, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                }

            } else if (roundIntroTimer < 2.5f) {
                if (fightImg != null) {
                    batch.draw(fightImg, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
                }

            } else {
                // Intro is over! Shrink characters and start the fight!
                showingRoundIntro = false;
                roundIntroTimer = 0f;
                turnTimer = 0f;
            }

            batch.end();
            return; // EXIT EARLY: Do not draw UI yet!
        }

        // 3. NORMAL BATTLE UI (Only draws after intro finishes)
        drawHealthBars();

        batch.begin();
        if (yellowUi != null) batch.draw(yellowUi, 40, 20, 1420, 320);
        if (redUi != null) {
            batch.draw(redUi, 140, 60, 350, 250);
            batch.draw(redUi, 1480, 20, 400, 300);
        }

        Character uiCharacter = (isPVPMode() && !isPlayerTurn) ? enemy : player;
        int[] uiCD = (isPVPMode() && !isPlayerTurn) ? enemyCD : playerCD;
        boolean disableButtons = !isPlayerTurn && !isPVPMode();

        if (uiCharacter.getSkills() != null && uiCharacter.getSkills().size() >= 3) {
            drawSkillUI(0, uiCharacter.getSkills().get(0).getName(), skill1Bounds, uiCD[0], uiCharacter, disableButtons);
            drawSkillUI(1, uiCharacter.getSkills().get(1).getName(), skill2Bounds, uiCD[1], uiCharacter, disableButtons);
            drawSkillUI(2, uiCharacter.getSkills().get(2).getName(), skill3Bounds, uiCD[2], uiCharacter, disableButtons);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "What will\n" + uiCharacter.getName() + "\ndo?", 180, 240);
        font.draw(batch, "HP  - " + uiCharacter.getHealth()      + "/" + uiCharacter.getMaxHealth(), 1540, 220);
        font.draw(batch, "Mana- " + uiCharacter.getCurrentMana() + "/" + uiCharacter.getMaxMana(),   1540, 140);

        String p1Display = username + " (" + player.getName() + ")";
        font.draw(batch, p1Display, 160, 1040);

        String p2Display = (isPVPMode() ? player2Name : "CPU") + " (" + enemy.getName() + ")";
        GlyphLayout p2Layout = new GlyphLayout(font, p2Display);
        font.draw(batch, p2Layout, WORLD_WIDTH - 160 - p2Layout.width, 1040);

        GlyphLayout topHUDLayout = new GlyphLayout(font, getTopHUDText());
        font.draw(batch, topHUDLayout, (WORLD_WIDTH - topHUDLayout.width) / 2f, 1040);

        if (playerIcon != null) batch.draw(playerIcon, 30, 910, 120, 120);
        if (enemyIconRegion != null) batch.draw(enemyIconRegion, WORLD_WIDTH - 150, 910, 120, 120);

        if (isTransitioning) {
            font.getData().setScale(5.0f);
            font.setColor(Color.GOLD);
            GlyphLayout transLayout = new GlyphLayout(font, transitionMessage);
            font.draw(batch, transLayout, (WORLD_WIDTH - transLayout.width) / 2f, WORLD_HEIGHT / 2 + 100);
            font.getData().setScale(2.5f);
        } else {
            String turnMsg = isPlayerTurn ? player.getName() + "'s Turn!" : enemy.getName() + "'s Turn...";
            font.setColor(isPlayerTurn ? Color.YELLOW : Color.RED);
            GlyphLayout turnLayout = new GlyphLayout(font, turnMsg);
            font.draw(batch, turnLayout, (WORLD_WIDTH - turnLayout.width) / 2f, 800);
        }

        batch.end();

        // 4. GAME FLOW LOGIC
        if (isTransitioning) {
            transitionTimer += delta;
            if (transitionTimer >= 2.0f) {
                if (matchIsOver) {
                    onMatchOver(playerWins >= 2);
                } else {
                    isTransitioning = false;
                    transitionTimer = 0;
                    resetRound();
                }
            }
        } else {
            handleGameLogic(delta);
        }
    }

    private void drawHealthBars() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(160, 950, 450, 50);
        float pPercent = (float) player.getHealth() / player.getMaxHealth();
        shapeRenderer.setColor(pPercent > 0.3f ? Color.GREEN : Color.RED);
        shapeRenderer.rect(165, 955, 440 * pPercent, 40);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(WORLD_WIDTH - 610, 950, 450, 50);
        float ePercent = (float) enemy.getHealth() / enemy.getMaxHealth();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(WORLD_WIDTH - 605, 955, 440 * ePercent, 40);

        for (int i = 0; i < 2; i++) {
            shapeRenderer.setColor(i < playerWins ? Color.GOLD : Color.DARK_GRAY);
            shapeRenderer.rect(580 - (i * 35), 915, 25, 25);
        }

        for (int i = 0; i < 2; i++) {
            shapeRenderer.setColor(i < enemyWins ? Color.GOLD : Color.DARK_GRAY);
            shapeRenderer.rect(1315 + (i * 35), 915, 25, 25);
        }

        shapeRenderer.end();
    }

    private void drawSkillUI(int i, String name, Rectangle bounds, int cd, Character activeCharacter, boolean disabled) {
        Texture normal  = (i == 0) ? skill1Btn  : (i == 1) ? skill2Btn  : skill3Btn;
        Texture pressed = (i == 0) ? skill1BtnP : (i == 1) ? skill2BtnP : skill3BtnP;
        if (normal == null) return;

        int cost           = activeCharacter.getSkills().get(i).getManaCost();
        boolean canAfford  = activeCharacter.getCurrentMana() >= cost;
        boolean onCD       = cd > 0;

        boolean isTouching = !disabled && Gdx.input.isTouched() && bounds.contains(touch.x, touch.y);
        boolean usePressed = !canAfford || onCD || disabled || isTouching;
        Texture toUse = (usePressed && pressed != null) ? pressed : normal;

        batch.draw(toUse, bounds.x, bounds.y, 250, 70);

        font.setColor(onCD || !canAfford || disabled ? Color.GRAY : Color.BLACK);
        String label = "- " + name + (onCD ? " (" + cd + ")" : "");
        font.draw(batch, label, bounds.x + 280, bounds.y + 50);
    }

    // ===============================
    // CORE TURN LOGIC
    // ===============================

    protected String getTopHUDText() {
        return "ROUND: " + currentRound;
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
            if (turnTimer >= turnDelay) {
                turnTimer = 0;
                executeEnemyTurn();
            }
        }
    }

    protected void executeSkill(int index) {
        if (index < 0 || index > 2) return;

        Character attacker = isPlayerTurn ? player : enemy;
        Character defender = isPlayerTurn ? enemy : player;
        int[] activeCD     = isPlayerTurn ? playerCD : enemyCD;

        int cost = attacker.getSkills().get(index).getManaCost();
        if (attacker.getCurrentMana() < cost || activeCD[index] > 0) return;

        switch (index) {
            case 0: attacker.basicAttack(defender);    break;
            case 1: attacker.secondarySkill(defender); activeCD[1] = 3; break;
            case 2: attacker.ultimateSkill(defender);  activeCD[2] = 5; break;
        }

        if(!isPlayerTurn || isPVPMode()) {
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

            if (player.isAlive()) {
                playerWins++;
                transitionMessage = "YOU WIN ROUND " + currentRound + "!";
            } else {
                enemyWins++;
                transitionMessage = enemy.getName() + " WINS ROUND " + currentRound + "!";
            }

            if (playerWins == 2 || enemyWins == 2) {
                matchIsOver = true;
                transitionMessage = (playerWins == 2) ? "VICTORY!" : "DEFEATED!";
            } else {
                currentRound++;
            }
        }
    }

    protected void resetRound() {
        player.restoreHP();
        player.restoreMana();
        enemy.restoreHP();
        enemy.restoreMana();
        playerCD = new int[]{0, 0, 0};
        enemyCD  = new int[]{0, 0, 0};
        isPlayerTurn = true;

        showingRoundIntro = true;
        roundIntroTimer = 0f;
    }

    private Rectangle getBounds(int i) {
        if (i == 0) return skill1Bounds;
        if (i == 1) return skill2Bounds;
        return skill3Bounds;
    }

    protected abstract boolean isPVPMode();
    protected abstract void executeEnemyTurn();
    protected abstract void onMatchOver(boolean playerWon);

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void show()   {}
    @Override public void hide()   {}
    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();

        if (background   != null) background.dispose();
        if (redUi        != null) redUi.dispose();
        if (yellowUi     != null) yellowUi.dispose();
        if (playerSprite != null) playerSprite.dispose();
        if (enemySprite  != null) enemySprite.dispose();
        if (playerIcon   != null) playerIcon.dispose();
        if (enemyIcon    != null) enemyIcon.dispose();

        // Dispose your new pop-up images!
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
    }
}