package io.github.PASAN.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.PASAN.Main;
import io.github.PASAN.ArcadeBattleScreen;

public class ArcadeIntroScreen implements Screen {
    private SpriteBatch batch;
    private Texture background;
    private Texture startBtn, startBtnPressed;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 touchPoint;

    // Matching your FirstScreen constants
    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private Rectangle startBounds;
    private boolean startPressed = false;

    private String username;
    private int characterIndex;

    public ArcadeIntroScreen(String username, int characterIndex) {
        this.username = username;
        this.characterIndex = characterIndex;

        batch = new SpriteBatch();
        touchPoint = new Vector3();

        // Camera & Viewport Setup
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Assets
        background = new Texture("backgrounds/arcade_intro_bg.png");
        startBtn = new Texture("buttons/start_button.png"); // Ensure these paths match your assets
        startBtnPressed = new Texture("buttons/start_button_pressed.png");

        // Position matching your FirstScreen button size (470x130)
        // Adjusting Y to 240 to match the visual location of the button in your JPG
        float centerX = WORLD_WIDTH / 2f;
        startBounds = new Rectangle(centerX - 235, 380, 470, 130);
    }

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update touch coordinates
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Draw Background
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // 2. Draw Button using your FirstScreen logic
        if (Gdx.input.isTouched() && startBounds.contains(touchPoint.x, touchPoint.y)) {
            batch.draw(startBtnPressed, startBounds.x, startBounds.y, startBounds.width, startBounds.height);
        } else {
            batch.draw(startBtn, startBounds.x, startBounds.y, startBounds.width, startBounds.height);
        }

        batch.end();

        handleInput();
    }

    private void handleInput() {
        // Detect initial press
        if (Gdx.input.justTouched()) {
            if (startBounds.contains(touchPoint.x, touchPoint.y)) {
                startPressed = true;
            }
        }

        // Detect release to trigger screen change
        if (!Gdx.input.isTouched()) {
            if (startPressed && startBounds.contains(touchPoint.x, touchPoint.y)) {
                startGame();
            }
            startPressed = false;
        }
    }

    private void startGame() {
        String playerChar = CharacterSelectorScreen.characterNames[characterIndex];
        Main game = (Main) Gdx.app.getApplicationListener();
        game.setScreen(new ArcadeBattleScreen(game, username, playerChar));
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        startBtn.dispose();
        startBtnPressed.dispose();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}