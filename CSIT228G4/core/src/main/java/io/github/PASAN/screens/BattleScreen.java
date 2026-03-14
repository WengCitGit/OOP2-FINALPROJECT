package io.github.PASAN.screens;

import io.github.PASAN.characters.*;
import io.github.PASAN.characters.Character;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.modes.ArcadeMode;

public class BattleScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;

    private String username;
    private Texture background;
    private Texture playerSprite;
    private Texture enemySprite;
    private TextureRegion enemyRegion;

    private Texture redUi;
    private Texture yellowUi;

    private Texture skill1Btn, skill1BtnP;
    private Texture skill2Btn, skill2BtnP;
    private Texture skill3Btn, skill3BtnP;

    private Character player;
    private Character enemy;

    private Rectangle skill1Bounds, skill2Bounds, skill3Bounds;
    private Vector3 touch;

    // ArcadeMode reference — null when using the simple constructor
    private ArcadeMode arcadeMode;
    private Game game;
    private int currentStage;

    private boolean isPlayerTurn = true;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    // -------------------------------------------------------
    // Constructor 1: simple/standalone battle (no ArcadeMode)
    // -------------------------------------------------------
    public BattleScreen(String username, String playerCharName, String enemyCharName) {
        this.username   = username;
        this.arcadeMode = null;
        this.game       = null;
        this.currentStage = 0;
        init(playerCharName, enemyCharName);
    }

    // -------------------------------------------------------
    // Constructor 2: arcade mode battle
    // -------------------------------------------------------
    public BattleScreen(Game game, ArcadeMode arcadeMode, String username,
                        String playerCharName, String enemyCharName, int currentStage) {
        this.game         = game;
        this.arcadeMode   = arcadeMode;
        this.username     = username;
        this.currentStage = currentStage;
        init(playerCharName, enemyCharName);
    }

    // -------------------------------------------------------
    // Shared initialisation — called by both constructors
    // -------------------------------------------------------
    private void init(String playerCharName, String enemyCharName) {
        batch = new SpriteBatch();
        font  = new BitmapFont();
        font.getData().setScale(2.5f);

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();

        // Textures — wrapped in try/catch so a missing asset doesn't crash everything
        background   = loadTexture("backgrounds/battle_bg1.png");
        playerSprite = loadTexture("characters/" + playerCharName.replace(" ", "") + ".png");
        enemySprite  = loadTexture("characters/" + enemyCharName.replace(" ", "")  + ".png");

        if (enemySprite != null) {
            enemyRegion = new TextureRegion(enemySprite);
            enemyRegion.flip(true, false);
        }

        redUi    = loadTexture("backgrounds/red_background.png");
        yellowUi = loadTexture("backgrounds/yellow_background.png");

        skill1Btn  = loadTexture("buttons/skill1_button.png");
        skill1BtnP = loadTexture("buttons/skill1_button_pressed.png");
        skill2Btn  = loadTexture("buttons/skill2_button.png");
        skill2BtnP = loadTexture("buttons/skill2_button_pressed.png");
        skill3Btn  = loadTexture("buttons/skill3_button.png");
        skill3BtnP = loadTexture("buttons/skill3_button_pressed.png");

        player = createCharacterInstance(playerCharName);
        enemy  = createCharacterInstance(enemyCharName);

        skill1Bounds = new Rectangle(555, 230, 800, 70);
        skill2Bounds = new Rectangle(555, 145, 800, 70);
        skill3Bounds = new Rectangle(555,  60, 800, 70);
    }

    private Texture loadTexture(String path) {
        try {
            return new Texture(path);
        } catch (Exception e) {
            Gdx.app.error("BattleScreen", "Missing texture: " + path);
            return null;
        }
    }

    // -------------------------------------------------------
    // Character factory
    // -------------------------------------------------------
    private Character createCharacterInstance(String name) {
        switch (name) {
            case "Jollibee":        return new Jollibee();
            case "Burger King":     return new BurgerKing();
            case "Jack in the Box": return new JackInTheBox();
            case "Wendy":           return new Wendy();
            case "McDonald":        return new McDonald();
            case "Little Caesar":   return new LittleCaesar();
            case "Colonel Sanders": return new ColonelSanders();
            default:                return new Jollibee();
        }
    }

    // -------------------------------------------------------
    // Render
    // -------------------------------------------------------
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        boolean isTouchingSkill1 = Gdx.input.isTouched() && skill1Bounds.contains(touch.x, touch.y);
        boolean isTouchingSkill2 = Gdx.input.isTouched() && skill2Bounds.contains(touch.x, touch.y);
        boolean isTouchingSkill3 = Gdx.input.isTouched() && skill3Bounds.contains(touch.x, touch.y);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (background   != null) batch.draw(background,   0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        if (playerSprite != null) batch.draw(playerSprite, 200, 350, 300, 450);
        if (enemyRegion  != null) batch.draw(enemyRegion,  WORLD_WIDTH - 500, 350, 300, 450);

        if (yellowUi != null) batch.draw(yellowUi, 40,   20, 1420, 320);
        if (redUi    != null) {
            batch.draw(redUi, 140,  60,  350, 250);
            batch.draw(redUi, 1480, 20,  400, 300);
        }

        // Skill buttons
        drawButton(isTouchingSkill1, skill1Btn, skill1BtnP, skill1Bounds);
        drawButton(isTouchingSkill2, skill2Btn, skill2BtnP, skill2Bounds);
        drawButton(isTouchingSkill3, skill3Btn, skill3BtnP, skill3Bounds);

        // HUD text
        font.setColor(Color.WHITE);
        font.draw(batch, "What will\n" + player.getName() + "\ndo?",   200,  240);
        font.draw(batch, "HP   - " + player.getHealth()      + "/" + player.getMaxHealth(),  1545, 220);
        font.draw(batch, "Mana - " + player.getCurrentMana() + "/" + player.getMaxMana(),    1545, 140);
        font.draw(batch, player.getName() + " HP: " + player.getHealth(),                     50,  1030);
        font.draw(batch, enemy.getName()  + " HP: " + enemy.getHealth(),  WORLD_WIDTH - 450, 1030);

        // Show stage number when coming from ArcadeMode
        if (arcadeMode != null) {
            font.draw(batch, "Stage: " + currentStage, WORLD_WIDTH / 2 - 100, 1030);
        }

        // Skill labels
        if (player.getSkills() != null && player.getSkills().size() >= 3) {
            font.setColor(Color.BLACK);
            font.draw(batch, "- " + player.getSkills().get(0).getName(), skill1Bounds.x + 280, skill1Bounds.y + 50);
            font.draw(batch, "- " + player.getSkills().get(1).getName(), skill2Bounds.x + 280, skill2Bounds.y + 50);
            font.draw(batch, "- " + player.getSkills().get(2).getName(), skill3Bounds.x + 280, skill3Bounds.y + 50);
        }

        batch.end();

        handleInput(delta);
    }

    private void drawButton(boolean pressing, Texture normal, Texture pressed, Rectangle bounds) {
        if (normal == null) return;
        Texture tex = (pressing && pressed != null) ? pressed : normal;
        batch.draw(tex, bounds.x, bounds.y, 250, 70);
    }

    // -------------------------------------------------------
    // Input / game logic
    // -------------------------------------------------------
    private void handleInput(float delta) {
        if (!isPlayerTurn) {
            // If coming from ArcadeMode, delegate the update so enemy AI runs there
            if (arcadeMode != null) {
                arcadeMode.update(delta, false);
            } else {
                // Simple mode: immediate enemy retaliation
                doSimpleEnemyTurn();
                isPlayerTurn = true;
            }
            return;
        }

        if (!Gdx.input.justTouched()) return;

        boolean actionTaken = false;

        if (skill1Bounds.contains(touch.x, touch.y)) {
            player.basicAttack(enemy);
            System.out.println(player.getName() + " used " + player.getSkills().get(0).getName()
                    + " | Enemy HP: " + enemy.getHealth());
            actionTaken = true;
        } else if (skill2Bounds.contains(touch.x, touch.y)) {
            player.secondarySkill(enemy);
            System.out.println(player.getName() + " used " + player.getSkills().get(1).getName()
                    + " | Enemy HP: " + enemy.getHealth());
            actionTaken = true;
        } else if (skill3Bounds.contains(touch.x, touch.y)) {
            player.ultimateSkill(enemy);
            System.out.println(player.getName() + " used " + player.getSkills().get(2).getName()
                    + " | Enemy HP: " + enemy.getHealth());
            actionTaken = true;
        }

        if (actionTaken) {
            isPlayerTurn = false;

            // In ArcadeMode, pass the skill press to the mode's update loop
            if (arcadeMode != null) {
                arcadeMode.update(delta, true);
            }

            checkBattleOver();
        }
    }

    private void doSimpleEnemyTurn() {
        if (!enemy.isAlive()) return;
        // Enemy picks a random available skill
        if (enemy.getSkills() == null || enemy.getSkills().isEmpty()) return;
        int pick = (int) (Math.random() * enemy.getSkills().size());
        switch (pick) {
            case 0: enemy.basicAttack(player);    break;
            case 1: enemy.secondarySkill(player); break;
            case 2: enemy.ultimateSkill(player);  break;
        }
        System.out.println(enemy.getName() + " attacked | Player HP: " + player.getHealth());
        checkBattleOver();
    }

    private void checkBattleOver() {
        if (!enemy.isAlive()) {
            System.out.println(enemy.getName() + " defeated!");
            if (arcadeMode != null && game != null) {
                arcadeMode.nextStage();   // ArcadeMode handles screen transition
            }
        } else if (!player.isAlive()) {
            System.out.println(player.getName() + " defeated!");
            if (arcadeMode != null && game != null) {
                arcadeMode.reset();       // or handle retry/game-over
            }
        } else {
            isPlayerTurn = true;
        }
    }

    // -------------------------------------------------------
    // Screen lifecycle
    // -------------------------------------------------------
    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void show()   {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();

        if (background   != null) background.dispose();
        if (playerSprite != null) playerSprite.dispose();
        if (enemySprite  != null) enemySprite.dispose();
        if (redUi        != null) redUi.dispose();
        if (yellowUi     != null) yellowUi.dispose();
        if (skill1Btn    != null) skill1Btn.dispose();
        if (skill1BtnP   != null) skill1BtnP.dispose();
        if (skill2Btn    != null) skill2Btn.dispose();
        if (skill2BtnP   != null) skill2BtnP.dispose();
        if (skill3Btn    != null) skill3Btn.dispose();
        if (skill3BtnP   != null) skill3BtnP.dispose();
    }
}