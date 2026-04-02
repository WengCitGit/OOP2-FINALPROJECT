package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.modes.ArcadeBattleScreen;
import io.github.PASAN.modes.PVCBattleScreen;
import io.github.PASAN.modes.PVPBattleScreen;
import java.util.Random;

public class VSScreen implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;

    private String mode;
    private Game game;

    private String player1Name;
    private String player2Name;
    private String player1Char;
    private String player2Char;

    private Texture background;
    private Texture popupBoard;
    private Texture playerSprite;
    private Texture enemySprite;
    private TextureRegion enemyRegion;

    private float transitionTimer = 0f;
    private static final float TRANSITION_DURATION = 3.0f;
    private boolean isStarting = false;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    // List of available characters for randomization
    private static final String[] CHARACTER_POOL = {
            "Jollibee", "Colonel Sanders", "McDonald", "Burger King",
            "Wendy", "Jack in the Box", "Little Caesar", "Chief Khai"
    };

    public VSScreen(String p1Name, String p2Name, String mode, String p1Char, String p2Char) {
        this.game = (Game) Gdx.app.getApplicationListener();

        this.player1Name = p1Name;
        this.player2Name = p2Name;
        this.mode = mode;
        this.player1Char = p1Char;

        // --- RANDOMIZATION LOGIC ---
        // If mode is PVC, we ignore the passed p2Char and pick a random one
        if (mode.equalsIgnoreCase("PVC")) {
            this.player2Char = CHARACTER_POOL[new Random().nextInt(CHARACTER_POOL.length)];
        } else {
            this.player2Char = p2Char;
        }

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        font = new BitmapFont();

        background = new Texture("backgrounds/temp_bg.png");
        popupBoard = new Texture("backgrounds/vsbg.png");

        playerSprite = new Texture("characters/" + player1Char.replace(" ", "") + ".png");

        // Now uses the (potentially randomized) player2Char
        enemySprite = new Texture("characters/" + player2Char.replace(" ", "") + ".png");
        enemyRegion = new TextureRegion(enemySprite);
        enemyRegion.flip(true, false);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (!isStarting) {
            transitionTimer += delta;
            if (transitionTimer >= TRANSITION_DURATION) {
                isStarting = true;
                startGame();
                return;
            }
        }

        float boardW = 1400f;
        float boardH = 750f;
        float boardX = (WORLD_WIDTH  - boardW) / 2f;
        float boardY = (WORLD_HEIGHT - boardH) / 2f - 30f;

        float charW = 410f;
        float charH = 480f;

        float playerSpriteX = boardX + 160f;
        float enemySpriteX  = boardX + boardW - 130f - charW;
        float spriteY       = boardY + 160f;

        float nameLabelY  = spriteY - 20f;
        float titleLabelY = boardY + boardH - 55f;

        batch.begin();
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(popupBoard, boardX, boardY, boardW, boardH);

        font.getData().setScale(4.5f);
        font.setColor(Color.RED);
        String titleText = mode.toUpperCase() + " BATTLE";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        font.draw(batch, titleLayout, WORLD_WIDTH / 2f - titleLayout.width / 2f, titleLabelY);

        batch.draw(playerSprite, playerSpriteX, spriteY, charW, charH);
        batch.draw(enemyRegion, enemySpriteX, spriteY, charW, charH);

        font.getData().setScale(2.8f);
        font.setColor(Color.WHITE);

        GlyphLayout p1NameLayout = new GlyphLayout(font, player1Char);
        font.draw(batch, p1NameLayout, playerSpriteX + (charW / 3f) - p1NameLayout.width / 2f, nameLabelY);

        GlyphLayout p2NameLayout = new GlyphLayout(font, player2Char);
        font.draw(batch, p2NameLayout, enemySpriteX + (charW / 1.5f) - p2NameLayout.width / 2f, nameLabelY);

        batch.end();
    }

    private void startGame() {
        System.out.println("BATTLE starting: " + player1Char + " VS " + player2Char + " in Mode: " + mode);

        if (mode.toUpperCase().contains("ARCADE")) {
            game.setScreen(new ArcadeBattleScreen(game, player1Name, player1Char));
        } else if (mode.toUpperCase().contains("PVC")) {
            // PASSING THE RANDOMIZED CHARACTER:
            // Note: Your PVCBattleScreen constructor must be updated to accept this 4th argument
            game.setScreen(new PVCBattleScreen(game, player1Name, player1Char, player2Char));
        } else if (mode.toUpperCase().contains("PVP")) {
            game.setScreen(new PVPBattleScreen(game, player1Name, player2Name, player1Char, player2Char));
        }

        this.dispose();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        background.dispose();
        popupBoard.dispose();
        playerSprite.dispose();
        enemySprite.dispose();
    }
}