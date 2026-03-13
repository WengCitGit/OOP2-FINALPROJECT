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
import java.util.*;

public class TemporaryArcadeScreen implements Screen {

    private Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;

    private Texture background, redUi, yellowUi;
    private Texture skill1Btn, skill1BtnP, skill2Btn, skill2BtnP, skill3Btn, skill3BtnP;
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

    private int[] playerCD = {0, 0};
    private int[] enemyCD = {0, 0};

    private Character player;
    private Character enemy;
    private Random random = new Random();

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public TemporaryArcadeScreen(Game game, String playerName, String playerCharName) {
        this.game = game;
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2.5f);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();

        // 1. Setup Player
        player = createCharacterInstance(playerCharName);

        // 2. Setup Randomized Enemy (that isn't the player)
        String[] possibleEnemies = {"Jollibee", "Colonel Sanders", "McDonald", "Burger King", "Wendy", "Jack in the Box", "Little Caesar"};
        String randomEnemyName;
        do {
            randomEnemyName = possibleEnemies[random.nextInt(possibleEnemies.length)];
        } while (randomEnemyName.equals(playerCharName));

        enemy = createCharacterInstance(randomEnemyName);

        // 3. Load Assets
        loadAssetsSafely(playerCharName, enemy.getName());

        skill1Bounds = new Rectangle(555, 230, 250, 70);
        skill2Bounds = new Rectangle(555, 145, 250, 70);
        skill3Bounds = new Rectangle(555, 60, 250, 70);

        System.out.println("MATCH START: " + player.getName() + " VS " + enemy.getName());
    }

    private void loadAssetsSafely(String pName, String eName) {
        try {
            background = new Texture("backgrounds/temp_bg.png");
            redUi = new Texture("backgrounds/red_background.png");
            yellowUi = new Texture("backgrounds/yellow_background.png");
            skill1Btn = new Texture("buttons/skill1_button.png");
            skill1BtnP = new Texture("buttons/skill1_button_pressed.png");
            skill2Btn = new Texture("buttons/skill2_button.png");
            skill2BtnP = new Texture("buttons/skill2_button_pressed.png");
            skill3Btn = new Texture("buttons/skill3_button.png");
            skill3BtnP = new Texture("buttons/skill3_button_pressed.png");

            playerSprite = new Texture("characters/" + pName.replace(" ", "") + ".png");
            enemySprite = new Texture("characters/" + eName.replace(" ", "") + ".png");
            enemyRegion = new TextureRegion(enemySprite);
            enemyRegion.flip(true, false);
        } catch (Exception e) {
            Gdx.app.error("ASSETS", "Missing file: " + e.getMessage());
        }
    }

    private Character createCharacterInstance(String name) {
        switch (name) {
            case "Jollibee": return new Jollibee();
            case "Colonel Sanders": return new ColonelSanders();
            case "McDonald": return new McDonald();
            case "Burger King": return new BurgerKing();
            case "Wendy": return new Wendy();
            case "Jack in the Box": return new JackInTheBox();
            case "Little Caesar": return new LittleCaesar();
            default: return new Jollibee();
        }
    }

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (background != null) batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.end();

        drawHealthBars();

        batch.begin();
        if (playerSprite != null) batch.draw(playerSprite, 200, 350, 300, 450);
        if (enemyRegion != null) batch.draw(enemyRegion, WORLD_WIDTH - 500, 350, 300, 450);

        if (yellowUi != null) batch.draw(yellowUi, 40, 20, 1420, 320);
        if (redUi != null) {
            batch.draw(redUi, 140, 60, 350, 250);
            batch.draw(redUi, 1480, 20, 400, 300);
        }

        if (player.getSkills() != null && player.getSkills().size() >= 3) {
            drawSkillUI(0, player.getSkills().get(0).getName(), skill1Bounds, 0);
            drawSkillUI(1, player.getSkills().get(1).getName(), skill2Bounds, playerCD[0]);
            drawSkillUI(2, player.getSkills().get(2).getName(), skill3Bounds, playerCD[1]);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "What will\n" + player.getName() + "\ndo?", 170, 240);
        font.draw(batch, "HP  - " + player.getHealth() + "/" + player.getMaxHealth(), 1510, 220);
        font.draw(batch, "Mana- " + player.getCurrentMana() + "/" + player.getMaxMana(), 1510, 140);
        font.draw(batch, "ROUND: " + currentRound + " | Score: " + playerWins + "-" + enemyWins, WORLD_WIDTH/2 - 200, 1030);

        if (isTransitioning) {
            font.getData().setScale(5.0f);
            font.setColor(Color.GOLD);
            font.draw(batch, transitionMessage, WORLD_WIDTH/2 - 450, WORLD_HEIGHT/2 + 100);
            font.getData().setScale(2.5f);
        } else {
            String turnMsg = playerTurn ? player.getName() + "'s Turn!" : enemy.getName() + "'s Turn...";
            font.setColor(playerTurn ? Color.YELLOW : Color.RED);
            font.draw(batch, turnMsg, WORLD_WIDTH/2 - 150, 800);
        }
        batch.end();

        if (isTransitioning) {
            transitionTimer += delta;
            if (transitionTimer >= 3.0f) {
                if (matchIsOver) game.setScreen(new FirstScreen(game));
                else { isTransitioning = false; transitionTimer = 0; resetRound(); }
            }
        } else {
            handleGameLogic(delta);
        }
    }

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

    private void drawSkillUI(int i, String name, Rectangle bounds, int cd) {
        Texture n = (i==0)? skill1Btn : (i==1)? skill2Btn : skill3Btn;
        Texture p = (i==0)? skill1BtnP : (i==1)? skill2BtnP : skill3BtnP;
        if (n == null) return;
        int cost = player.getSkills().get(i).getManaCost();
        boolean canAfford = player.getCurrentMana() >= cost;
        boolean onCD = cd > 0;
        batch.draw((playerTurn && !onCD && canAfford && Gdx.input.isTouched() && bounds.contains(touch.x, touch.y)) ? p : (onCD || !canAfford ? p : n), bounds.x, bounds.y, 250, 70);
        font.setColor(Color.BLACK);
        font.draw(batch, "- " + name + (onCD ? " (" + cd + ")" : ""), bounds.x + 280, bounds.y + 50);
    }

    private void handleGameLogic(float delta) {
        if (playerTurn) {
            if (Gdx.input.justTouched()) {
                if (skill1Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 0;
                else if (skill2Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 1;
                else if (skill3Bounds.contains(touch.x, touch.y)) pressedSkillIndex = 2;
            }
            if (!Gdx.input.isTouched() && pressedSkillIndex != -1) {
                if (getBounds(pressedSkillIndex).contains(touch.x, touch.y)) processPlayerAction(pressedSkillIndex);
                pressedSkillIndex = -1;
            }
        } else {
            turnTimer += delta;
            if (turnTimer >= turnDelay) processEnemyTurn();
        }
    }

    private void processPlayerAction(int index) {
        int cost = player.getSkills().get(index).getManaCost();
        int cd = (index == 0) ? 0 : (index == 1) ? playerCD[0] : playerCD[1];
        if (player.getCurrentMana() >= cost && cd <= 0) {
            int oldHP = enemy.getHealth();
            if (index == 0) player.basicAttack(enemy);
            else if (index == 1) { player.secondarySkill(enemy); playerCD[0] = 3; }
            else if (index == 2) { player.ultimateSkill(enemy); playerCD[1] = 5; }

            System.out.println("\n[PLAYER TURN] " + player.getName() + " used " + player.getSkills().get(index).getName());
            System.out.println("> DMG dealt: " + (oldHP - enemy.getHealth()));

            playerTurn = false;
            turnTimer = 0;
        }
    }

    private void processEnemyTurn() {
        if (enemy.isAlive()) {
            int oldHP = player.getHealth();
            // Enemy Logic: Use best available
            if (enemyCD[1] == 0 && enemy.getCurrentMana() >= enemy.getSkills().get(2).getManaCost()) {
                enemy.ultimateSkill(player); enemyCD[1] = 5;
                System.out.println("\n[ENEMY TURN] " + enemy.getName() + " used ULTIMATE!");
            } else if (enemyCD[0] == 0 && enemy.getCurrentMana() >= enemy.getSkills().get(1).getManaCost()) {
                enemy.secondarySkill(player); enemyCD[0] = 3;
                System.out.println("\n[ENEMY TURN] " + enemy.getName() + " used SECONDARY!");
            } else {
                enemy.basicAttack(player);
                System.out.println("\n[ENEMY TURN] " + enemy.getName() + " used BASIC ATTACK!");
            }
            System.out.println("> DMG received: " + (oldHP - player.getHealth()));
        }

        // Mana Regen & CDs
        if (playerCD[0] > 0) playerCD[0]--; if (playerCD[1] > 0) playerCD[1]--;
        if (enemyCD[0] > 0) enemyCD[0]--; if (enemyCD[1] > 0) enemyCD[1]--;

        int pRegen = random.nextInt(6) + 5;
        int eRegen = random.nextInt(6) + 5;
        player.addMana(pRegen);
        enemy.addMana(eRegen);
        System.out.println("> Mana Regen: Player +" + pRegen + " | Enemy +" + eRegen);

        playerTurn = true;
        checkMatchState();
    }

    private void checkMatchState() {
        if (!player.isAlive() || !enemy.isAlive()) {
            isTransitioning = true;
            transitionTimer = 0;
            if (player.isAlive()) { playerWins++; transitionMessage = "YOU WIN ROUND " + currentRound + "!"; }
            else { enemyWins++; transitionMessage = enemy.getName() + " WINS ROUND " + currentRound + "!"; }

            if (playerWins == 2 || enemyWins == 2) {
                matchIsOver = true;
                transitionMessage = (playerWins == 2 ? "MATCH OVER: VICTORY!" : "MATCH OVER: DEFEATED!");
            } else { currentRound++; }
            System.out.println("\n*** " + transitionMessage + " ***");
        }
    }

    private void resetRound() {
        player.restoreHP(); player.restoreMana();
        enemy.restoreHP(); enemy.restoreMana();
        playerCD = new int[]{0,0}; enemyCD = new int[]{0,0};
        playerTurn = true;
    }

    private Rectangle getBounds(int i) { return (i == 0) ? skill1Bounds : (i == 1) ? skill2Bounds : skill3Bounds; }
    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void dispose() {
        batch.dispose(); font.dispose(); shapeRenderer.dispose();
        if(background != null) background.dispose();
        if(playerSprite != null) playerSprite.dispose();
        if(enemySprite != null) enemySprite.dispose();
    }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}