package io.github.PASAN;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.characters.*;
import io.github.PASAN.characters.Character;
import io.github.PASAN.screens.FirstScreen;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.screens.VictoryScreen;

import java.util.*;

public class TemporaryArcadeScreen implements Screen {

    private Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;

    // --- FIELDS ---
    private String playerName;
    private boolean isDefeated = false;

    private Texture background, redUi, yellowUi;
    private Texture skill1Btn, skill1BtnP;
    private Texture skill2Btn, skill2BtnP;
    private Texture skill3Btn, skill3BtnP;
    private Texture playerSprite, enemySprite;
    private TextureRegion enemyRegion;

    private Rectangle skill1Bounds, skill2Bounds, skill3Bounds;
    private Vector3 touch;

    // --- GAME LOGIC ---
    private int pressedSkillIndex = -1;
    private boolean playerTurn = true;
    private float turnDelay = 1.5f;
    private float turnTimer = 0;

    private int currentRound = 1;
    private int playerWins = 0;
    private int enemyWins = 0;

    private boolean isTransitioning = false;
    private boolean matchIsOver = false;
    private float transitionTimer = 0;
    private String transitionMessage = "";

    private int[] playerCD = {0, 0, 0};
    private int[] enemyCD  = {0, 0, 0};

    private Character player;
    private Character enemy;
    private Random random = new Random();

    // --- ARCADE STAGE TRACKING ---
    private String playerCharName;
    private List<String> enemyQueue;
    private int currentStage = 1;
    private static final int TOTAL_STAGES = 8;

    // --- VS INTRO POPUP ---
    private boolean showingIntro = true;
    private float introTimer = 0f;
    private static final float INTRO_DURATION = 3.0f;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private static final String[] ALL_CHARACTERS = {
            "Jollibee", "Colonel Sanders", "McDonald",
            "Burger King", "Wendy", "Jack in the Box",
            "Little Caesar", "Chief Khai"
    };

    private static final String[] FINAL_BOSSES = {
            "Dev Kishanta", "Dev Rothesa", "Dev Wengie",
            "Dev Kunihiko", "Dev Diane"
    };

    public TemporaryArcadeScreen(Game game, String playerName, String playerCharName) {
        this.game           = game;
        this.playerName     = playerName;
        this.playerCharName = playerCharName;

        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font          = new BitmapFont();
        font.getData().setScale(2.5f);

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();

        // Build shuffled enemy queue (all characters except player's choice)
        enemyQueue = new ArrayList<>();
        for (String c : ALL_CHARACTERS) {
            if (!c.equalsIgnoreCase(playerCharName)) {
                enemyQueue.add(c);
            }
        }
        Collections.shuffle(enemyQueue);

        player = createCharacterInstance(playerCharName);
        enemy  = createCharacterInstance(enemyQueue.get(0));

        loadAssetsSafely(playerCharName, enemy.getName());

        skill1Bounds = new Rectangle(555, 230, 250, 70);
        skill2Bounds = new Rectangle(555, 145, 250, 70);
        skill3Bounds = new Rectangle(555,  60, 250, 70);

        System.out.println("ARCADE START | Stage 1: "
                + player.getName() + " VS " + enemy.getName());
    }

    // ===============================
    // ASSET LOADING
    // ===============================

    private void loadAssetsSafely(String pName, String eName) {
        try { background  = new Texture("backgrounds/temp_bg.png");           } catch (Exception e) { logMissing(e); }
        try { redUi       = new Texture("backgrounds/red_background.png");    } catch (Exception e) { logMissing(e); }
        try { yellowUi    = new Texture("backgrounds/yellow_background.png"); } catch (Exception e) { logMissing(e); }
        try { skill1Btn   = new Texture("buttons/skill1_button.png");          } catch (Exception e) { logMissing(e); }
        try { skill1BtnP  = new Texture("buttons/skill1_button_pressed.png");  } catch (Exception e) { logMissing(e); }
        try { skill2Btn   = new Texture("buttons/skill2_button.png");          } catch (Exception e) { logMissing(e); }
        try { skill2BtnP  = new Texture("buttons/skill2_button_pressed.png");  } catch (Exception e) { logMissing(e); }
        try { skill3Btn   = new Texture("buttons/skill3_button.png");          } catch (Exception e) { logMissing(e); }
        try { skill3BtnP  = new Texture("buttons/skill3_button_pressed.png");  } catch (Exception e) { logMissing(e); }

        if (playerSprite != null) { playerSprite.dispose(); playerSprite = null; }
        if (enemySprite  != null) { enemySprite.dispose();  enemySprite  = null; }

        try {
            playerSprite = new Texture("characters/" + pName.replace(" ", "") + ".png");
        } catch (Exception e) { logMissing(e); }

        try {
            enemySprite = new Texture("characters/" + eName.replace(" ", "") + ".png");
            enemyRegion = new TextureRegion(enemySprite);
            enemyRegion.flip(true, false);
        } catch (Exception e) { logMissing(e); }
    }

    private void logMissing(Exception e) {
        Gdx.app.error("ASSETS", "Missing file: " + e.getMessage());
    }

    // ===============================
    // CHARACTER FACTORY
    // ===============================

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
            default:
                Gdx.app.log("CHARACTER", "Unknown character: " + name + " — falling back to Jollibee.");
                return new Jollibee();
        }
    }

    // ===============================
    // RENDER
    // ===============================

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);

        // Always draw background first
        batch.begin();
        if (background != null) batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.end();

        // VS INTRO POPUP — blocks gameplay until done
        if (showingIntro) {
            introTimer += delta;
            drawIntroPopup();
            if (introTimer >= INTRO_DURATION) {
                showingIntro = false;
                introTimer   = 0f;
            }
            return;
        }

        // NORMAL BATTLE RENDER
        drawHealthBars();

        batch.begin();

        if (playerSprite != null) batch.draw(playerSprite, 200, 350, 450, 500);
        if (enemyRegion  != null) batch.draw(enemyRegion, WORLD_WIDTH - 700, 350, 450, 500);

        if (yellowUi != null) batch.draw(yellowUi, 40, 20, 1420, 320);
        if (redUi != null) {
            batch.draw(redUi, 140, 60, 350, 250);
            batch.draw(redUi, 1480, 20, 400, 300);
        }

        if (player.getSkills() != null && player.getSkills().size() >= 3) {
            drawSkillUI(0, player.getSkills().get(0).getName(), skill1Bounds, playerCD[0]);
            drawSkillUI(1, player.getSkills().get(1).getName(), skill2Bounds, playerCD[1]);
            drawSkillUI(2, player.getSkills().get(2).getName(), skill3Bounds, playerCD[2]);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "What will\n" + player.getName() + "\ndo?", 170, 240);
        font.draw(batch, "HP  - " + player.getHealth()      + "/" + player.getMaxHealth(), 1510, 220);
        font.draw(batch, "Mana- " + player.getCurrentMana() + "/" + player.getMaxMana(),   1510, 140);
        font.draw(batch,
                "STAGE: " + currentStage + "/" + TOTAL_STAGES
                        + "  |  ROUND: " + currentRound
                        + "  |  Score: " + playerWins + "-" + enemyWins,
                WORLD_WIDTH / 2 - 350, 1030);

        if (isTransitioning) {
            font.getData().setScale(5.0f);
            font.setColor(Color.GOLD);
            font.draw(batch, transitionMessage, WORLD_WIDTH / 2 - 450, WORLD_HEIGHT / 2 + 100);
            font.getData().setScale(2.5f);
        } else {
            String turnMsg = playerTurn
                    ? player.getName() + "'s Turn!"
                    : enemy.getName() + "'s Turn...";
            font.setColor(playerTurn ? Color.YELLOW : Color.RED);
            font.draw(batch, turnMsg, WORLD_WIDTH / 2 - 150, 800);
        }

        batch.end();

        // Transition timer logic
        if (isTransitioning) {
            transitionTimer += delta;
            if (transitionTimer >= 3.0f) {
                if (matchIsOver) {
                    if (isDefeated) {
                        game.setScreen(new GameOverScreen(game, playerName));
                    } else if (currentStage < TOTAL_STAGES) {
                        loadNextStage();
                    } else {
                        game.setScreen(new VictoryScreen(game, playerName));
                    }
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

    // ===============================
    // VS INTRO POPUP DRAWING
    // ===============================

    private void drawIntroPopup() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.82f);
        shapeRenderer.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        shapeRenderer.end();

        batch.begin();

        // Stage label — centered
        font.getData().setScale(3.5f);
        font.setColor(Color.ORANGE);
        String stageLabel = currentStage < TOTAL_STAGES ? "STAGE " + currentStage : "FINAL STAGE";
        GlyphLayout stageLayout = new GlyphLayout(font, stageLabel);
        font.draw(batch, stageLayout,
                WORLD_WIDTH / 2f - stageLayout.width / 2f,
                WORLD_HEIGHT / 2f + 280);

        // Player sprite on left
        if (playerSprite != null)
            batch.draw(playerSprite, 250, WORLD_HEIGHT / 2 - 180, 450, 500);

        // Enemy sprite on right (flipped)
        if (enemyRegion != null)
            batch.draw(enemyRegion, WORLD_WIDTH - 550, WORLD_HEIGHT / 2 - 180, 450, 500);

        // Player name
        font.getData().setScale(4f);
        font.setColor(Color.WHITE);
        font.draw(batch, player.getName(), 200, WORLD_HEIGHT / 2 + 170);

        // VS — centered
        font.getData().setScale(6f);
        font.setColor(Color.RED);
        GlyphLayout vsLayout = new GlyphLayout(font, "VS");
        font.draw(batch, vsLayout,
                WORLD_WIDTH / 2f - vsLayout.width / 2f,
                WORLD_HEIGHT / 2f + 170);

        // Enemy name — right side
        font.getData().setScale(4f);
        font.setColor(Color.WHITE);
        GlyphLayout enemyNameLayout = new GlyphLayout(font, enemy.getName());
        font.draw(batch, enemyNameLayout,
                WORLD_WIDTH - 250 - enemyNameLayout.width,
                WORLD_HEIGHT / 2f + 170);

        // "Starting..." after 1.5s — centered
        if (introTimer >= 1.5f) {
            font.getData().setScale(3f);
            font.setColor(Color.YELLOW);
            GlyphLayout startLayout = new GlyphLayout(font, "Starting...");
            font.draw(batch, startLayout,
                    WORLD_WIDTH / 2f - startLayout.width / 2f,
                    WORLD_HEIGHT / 2f - 230);
        }

        font.getData().setScale(2.5f);
        batch.end();
    }

    // ===============================
    // LOAD NEXT STAGE
    // ===============================

    private void loadNextStage() {
        currentStage++;
        currentRound    = 1;
        playerWins      = 0;
        enemyWins       = 0;
        isTransitioning = false;
        transitionTimer = 0;
        matchIsOver     = false;
        isDefeated      = false;

        player.restoreHP();
        player.restoreMana();
        playerCD   = new int[]{0, 0, 0};
        enemyCD    = new int[]{0, 0, 0};
        playerTurn = true;
        turnTimer  = 0;

        String nextEnemyName;
        if (currentStage <= 7) {
            nextEnemyName = enemyQueue.get(currentStage - 1);
        } else {
            nextEnemyName = FINAL_BOSSES[random.nextInt(FINAL_BOSSES.length)];
        }

        enemy = createCharacterInstance(nextEnemyName);

        if (enemySprite != null) { enemySprite.dispose(); enemySprite = null; }
        try {
            enemySprite = new Texture("characters/" + nextEnemyName.replace(" ", "") + ".png");
            enemyRegion = new TextureRegion(enemySprite);
            enemyRegion.flip(true, false);
        } catch (Exception e) {
            logMissing(e);
            enemyRegion = null;
        }

        showingIntro = true;
        introTimer   = 0f;

        System.out.println("STAGE " + currentStage + ": "
                + player.getName() + " VS " + enemy.getName());
    }

    // ===============================
    // HEALTH BARS
    // ===============================

    private void drawHealthBars() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(50, 950, 450, 50);
        float pPercent = (float) player.getHealth() / player.getMaxHealth();
        shapeRenderer.setColor(pPercent > 0.3f ? Color.GREEN : Color.RED);
        shapeRenderer.rect(55, 955, 440 * pPercent, 40);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(WORLD_WIDTH - 500, 950, 450, 50);
        float ePercent = (float) enemy.getHealth() / enemy.getMaxHealth();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(WORLD_WIDTH - 495, 955, 440 * ePercent, 40);

        shapeRenderer.end();
    }

    // ===============================
    // SKILL BUTTON DRAWING
    // ===============================

    private void drawSkillUI(int i, String name, Rectangle bounds, int cd) {
        Texture normal  = (i == 0) ? skill1Btn  : (i == 1) ? skill2Btn  : skill3Btn;
        Texture pressed = (i == 0) ? skill1BtnP : (i == 1) ? skill2BtnP : skill3BtnP;

        if (normal == null) return;

        int cost           = player.getSkills().get(i).getManaCost();
        boolean canAfford  = player.getCurrentMana() >= cost;
        boolean onCD       = cd > 0;
        boolean isTouching = Gdx.input.isTouched() && bounds.contains(touch.x, touch.y);

        boolean usePressed = !canAfford || onCD || (playerTurn && isTouching);
        Texture toUse = (usePressed && pressed != null) ? pressed : normal;

        batch.draw(toUse, bounds.x, bounds.y, 250, 70);

        font.setColor(onCD || !canAfford ? Color.GRAY : Color.BLACK);
        String label = "- " + name + (onCD ? " (" + cd + ")" : "");
        font.draw(batch, label, bounds.x + 280, bounds.y + 50);
    }

    // ===============================
    // GAME LOGIC
    // ===============================

    private void handleGameLogic(float delta) {
        if (playerTurn) {
            if (Gdx.input.justTouched()) {
                if      (skill1Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 0;
                else if (skill2Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 1;
                else if (skill3Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 2;
            }
            if (!Gdx.input.isTouched() && pressedSkillIndex != -1) {
                if (getBounds(pressedSkillIndex).contains(touch.x, touch.y)) {
                    processPlayerAction(pressedSkillIndex);
                }
                pressedSkillIndex = -1;
            }
        } else {
            turnTimer += delta;
            if (turnTimer >= turnDelay) {
                turnTimer = 0;
                processEnemyTurn();
            }
        }
    }

    // ===============================
    // PLAYER ACTION
    // ===============================

    private void processPlayerAction(int index) {
        if (index < 0 || index > 2) return;

        int cost = player.getSkills().get(index).getManaCost();
        int cd   = playerCD[index];

        if (player.getCurrentMana() < cost || cd > 0) return;

        int oldHP = enemy.getHealth();

        switch (index) {
            case 0: player.basicAttack(enemy);    break;
            case 1: player.secondarySkill(enemy); playerCD[1] = 3; break;
            case 2: player.ultimateSkill(enemy);  playerCD[2] = 5; break;
        }

        System.out.println("\n[PLAYER TURN] " + player.getName()
                + " used " + player.getSkills().get(index).getName());
        System.out.println("> DMG dealt: " + (oldHP - enemy.getHealth()));

        playerTurn = false;
        turnTimer  = 0;

        checkMatchState();
    }

    // ===============================
    // ENEMY TURN
    // ===============================

    private void processEnemyTurn() {
        if (!enemy.isAlive()) {
            playerTurn = true;
            return;
        }

        int oldHP = player.getHealth();

        if (enemyCD[2] == 0 && enemy.getCurrentMana() >= enemy.getSkills().get(2).getManaCost()) {
            enemy.ultimateSkill(player);
            enemyCD[2] = 5;
            System.out.println("\n[ENEMY TURN] " + enemy.getName() + " used ULTIMATE!");
        } else if (enemyCD[1] == 0 && enemy.getCurrentMana() >= enemy.getSkills().get(1).getManaCost()) {
            enemy.secondarySkill(player);
            enemyCD[1] = 3;
            System.out.println("\n[ENEMY TURN] " + enemy.getName() + " used SECONDARY!");
        } else {
            enemy.basicAttack(player);
            System.out.println("\n[ENEMY TURN] " + enemy.getName() + " used BASIC ATTACK!");
        }

        System.out.println("> DMG received: " + (oldHP - player.getHealth()));

        endOfRound();

        playerTurn = true;
        checkMatchState();
    }

    // ===============================
    // END OF ROUND
    // ===============================

    private void endOfRound() {
        for (int i = 0; i < 3; i++) {
            if (playerCD[i] > 0) playerCD[i]--;
            if (enemyCD[i]  > 0) enemyCD[i]--;
        }

        int pRegen = random.nextInt(6) + 5;
        int eRegen = random.nextInt(6) + 5;
        player.addMana(pRegen);
        enemy.addMana(eRegen);

        System.out.println("> Mana Regen: Player +" + pRegen + " | Enemy +" + eRegen);
    }

    // ===============================
    // CHECK WIN/LOSS STATE
    // ===============================

    private void checkMatchState() {
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

            if (playerWins == 2) {
                matchIsOver = true;
                if (currentStage >= TOTAL_STAGES) {
                    transitionMessage = "ARCADE COMPLETE! VICTORY!";
                } else {
                    transitionMessage = "STAGE " + currentStage + " CLEARED!";
                }
            } else if (enemyWins == 2) {
                matchIsOver       = true;
                isDefeated        = true;
                transitionMessage = "DEFEATED! GAME OVER!";
            } else {
                currentRound++;
            }

            System.out.println("\n*** " + transitionMessage + " ***");
        }
    }

    // ===============================
    // RESET ROUND (within same stage)
    // ===============================

    private void resetRound() {
        player.restoreHP();
        player.restoreMana();
        enemy.restoreHP();
        enemy.restoreMana();

        playerCD   = new int[]{0, 0, 0};
        enemyCD    = new int[]{0, 0, 0};
        playerTurn = true;
        turnTimer  = 0;
    }

    // ===============================
    // HELPERS
    // ===============================

    private Rectangle getBounds(int i) {
        if (i == 0) return skill1Bounds;
        if (i == 1) return skill2Bounds;
        return skill3Bounds;
    }

    // ===============================
    // SCREEN LIFECYCLE
    // ===============================

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

        if (skill1Btn  != null) skill1Btn.dispose();
        if (skill1BtnP != null) skill1BtnP.dispose();
        if (skill2Btn  != null) skill2Btn.dispose();
        if (skill2BtnP != null) skill2BtnP.dispose();
        if (skill3Btn  != null) skill3Btn.dispose();
        if (skill3BtnP != null) skill3BtnP.dispose();
    }
}