package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.modes.ArcadeBattleScreen;
import io.github.PASAN.modes.BaseBattleScreen;
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
    private Texture vsImage;

    private Texture playerSprite;
    private Texture enemySprite;

    private TextureRegion player1Region;
    private TextureRegion enemyRegion;

    private float transitionTimer = 0f;
    private static final float TRANSITION_DURATION = 3.0f;
    private boolean isStarting = false;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

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

        if (mode.equalsIgnoreCase("PVC")) {
            do {
                this.player2Char = CHARACTER_POOL[new Random().nextInt(CHARACTER_POOL.length)];
            } while (this.player2Char.equals(player1Char));
        } else {
            this.player2Char = p2Char;
        }

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        font = new BitmapFont();

        int randomBgNum = new Random().nextInt(8) + 1;
        BaseBattleScreen.currentBackgroundPath = "backgrounds/bg" + randomBgNum + ".png";
        background = new Texture(BaseBattleScreen.currentBackgroundPath);

        vsImage = new Texture("backgrounds/vs.png");

        // --- FIXED CROP (HALF BODY) ---
        // 0.55f crops exactly the top 55% of the image (waist up)
        playerSprite = new Texture("characters/" + player1Char.replace(" ", "") + ".png");
        player1Region = new TextureRegion(playerSprite, 0, 0, playerSprite.getWidth(), (int)(playerSprite.getHeight() * 0.55f));

        enemySprite = new Texture("characters/" + player2Char.replace(" ", "") + ".png");
        enemyRegion = new TextureRegion(enemySprite, 0, 0, enemySprite.getWidth(), (int)(enemySprite.getHeight() * 0.55f));
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

        // --- FIXED ASPECT RATIO MATH ---
        // Width is 1200, Height is 660. This matches the 55% crop so they don't look skinny!
        float charW = 1200f;
        float charH = 660f;

        // Pushed them closer to the center (they will overlap with the VS logo)
        float p1X = 50f;
        float p2X = WORLD_WIDTH - 50f - charW;

        // Lifted them up from the bottom so Player 1's text has room to breathe
        float spriteY = 180f;

        batch.begin();

        // Darken background
        batch.setColor(0.35f, 0.35f, 0.35f, 1f);
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.setColor(Color.WHITE);

        // Draw characters
        batch.draw(player1Region, p1X, spriteY, charW, charH);
        batch.draw(enemyRegion, p2X, spriteY, charW, charH);

        // Draw VS logo overlay
        batch.draw(vsImage, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        font.getData().setScale(4.5f);

        // PLAYER 1 NAME: Placed BELOW the character
        font.setColor(Color.RED);
        GlyphLayout p1NameLayout = new GlyphLayout(font, player1Char.toUpperCase());
        // spriteY - 40f pushes the text just below the bottom edge of the crop
        font.draw(batch, p1NameLayout, p1X + (charW / 2f) - (p1NameLayout.width / 2f) - 150f, spriteY - 40f);

        // PLAYER 2 NAME: Placed ABOVE the character
        font.setColor(Color.BLUE);
        GlyphLayout p2NameLayout = new GlyphLayout(font, player2Char.toUpperCase());
        // spriteY + charH + 60f pushes the text just above the top edge of the crop
        font.draw(batch, p2NameLayout, p2X + (charW / 2f) - (p2NameLayout.width / 2f) + 150f, spriteY + charH + 60f);

        batch.end();
    }

    private void startGame() {
        System.out.println("BATTLE starting: " + player1Char + " VS " + player2Char + " in Mode: " + mode);

        if (mode.toUpperCase().contains("ARCADE")) {
            game.setScreen(new ArcadeBattleScreen(game, player1Name, player1Char));
        } else if (mode.toUpperCase().contains("PVC")) {
            game.setScreen(new PVCBattleScreen(game, player1Name, player1Char, player2Char));
        } else if (mode.toUpperCase().contains("PVP")) {
            game.setScreen(new PVPBattleScreen(game, player1Name, player2Name, player1Char, player2Char));
        } else if (mode.toUpperCase().contains("ENDLESS")) {
            game.setScreen(new io.github.PASAN.modes.EndlessBattleScreen(game, player1Name, player1Char));
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
        if (vsImage != null) vsImage.dispose();
        playerSprite.dispose();
        enemySprite.dispose();
    }
}