package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.ArcadeBattleScreen;
import io.github.PASAN.Main;
import io.github.PASAN.PVCBattleScreen;
import io.github.PASAN.PVPBattleScreen;
// Make sure your battle screens are imported if they are in different packages!

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
    private TextureRegion enemyRegion; // Used to flip the enemy sprite

    // --- Timer Variables ---
    private float transitionTimer = 0f;
    private static final float TRANSITION_DURATION = 3.0f; // 3 seconds before auto-start
    private boolean isStarting = false;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public VSScreen(String p1Name, String p2Name, String mode, String p1Char, String p2Char) {
        this.game = (Game) Gdx.app.getApplicationListener();

        this.player1Name = p1Name;
        this.player2Name = p2Name;
        this.mode = mode;
        this.player1Char = p1Char;
        this.player2Char = p2Char;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        font = new BitmapFont();

        // Load Textures
        background = new Texture("backgrounds/temp_bg.png");
        popupBoard = new Texture("backgrounds/vsbg.png"); // Assuming this acts like arcade_board.png

        playerSprite = new Texture("characters/" + player1Char.replace(" ", "") + ".png");

        enemySprite = new Texture("characters/" + player2Char.replace(" ", "") + ".png");
        enemyRegion = new TextureRegion(enemySprite);
        enemyRegion.flip(true, false); // Make enemy face left
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Update the timer
        if (!isStarting) {
            transitionTimer += delta;
            if (transitionTimer >= TRANSITION_DURATION) {
                isStarting = true;
                startGame();
                return;
            }
        }

        // --- Layout Math (Matching ArcadeBattleScreen) ---
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

        // 1. Draw Background
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // 2. Draw Board
        batch.draw(popupBoard, boardX, boardY, boardW, boardH);

        // 3. Draw Mode Title at the top
        font.getData().setScale(4.5f);
        font.setColor(Color.RED);
        String titleText = mode.toUpperCase() + " BATTLE";
        GlyphLayout titleLayout = new GlyphLayout(font, titleText);
        font.draw(batch, titleLayout, WORLD_WIDTH / 2f - titleLayout.width / 2f, titleLabelY);

        // 4. Draw Characters
        batch.draw(playerSprite, playerSpriteX, spriteY, charW, charH);
        batch.draw(enemyRegion, enemySpriteX, spriteY, charW, charH);

        // 5. Draw Character Names
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
            game.setScreen(new PVCBattleScreen(game, player1Name, player1Char, player2Char));
        } else if (mode.toUpperCase().contains("PVP")) {
            game.setScreen(new PVPBattleScreen(game, player1Name,player2Name, player1Char, player2Char));
        }

        // Clean up this screen's memory before leaving
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
        // TextureRegions don't need to be disposed directly, the Texture it relies on (enemySprite) is enough.
    }
}