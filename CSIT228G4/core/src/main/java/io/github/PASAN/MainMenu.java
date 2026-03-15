package io.github.PASAN;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.PASAN.screens.FirstScreen;

public class MainMenu implements Screen {

    private Game game; // <- add this

    private SpriteBatch batch;
    private Texture background;
    private Texture playBtn, playBtnPressed;
    private Texture creditsBtn, creditsBtnPressed;
    private Texture exitBtn, exitBtnPressed;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private Rectangle playBounds, creditsBounds, exitBounds;
    private Vector3 touchPoint;

    private boolean playPressed = false;
    private boolean creditsPressed = false;
    private boolean exitPressed = false;

    // Update constructor to take Game instance
    public MainMenu(Game game) {
        this.game = game;

        batch = new SpriteBatch();
        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        background = new Texture("backgrounds/mainmenu_background.jpg");

        playBtn = new Texture("buttons/play_button.png");
        playBtnPressed = new Texture("buttons/play_button_pressed.png");

        creditsBtn = new Texture("buttons/credits_button.png");
        creditsBtnPressed = new Texture("buttons/credits_button_pressed.png");

        exitBtn = new Texture("buttons/exit_button.png");
        exitBtnPressed = new Texture("buttons/exit_button_pressed.png");

        float centerX = WORLD_WIDTH / 2f;
        float centerY = WORLD_HEIGHT / 2f;

        playBounds = new Rectangle(centerX - 235, centerY + 30, 470, 130);
        creditsBounds = new Rectangle(centerX - 235, centerY - 120, 470, 130);
        exitBounds = new Rectangle(centerX - 235, centerY - 270, 470, 130);
    }


    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        drawButton(playBtn, playBtnPressed, playBounds);
        drawButton(creditsBtn, creditsBtnPressed, creditsBounds);
        drawButton(exitBtn, exitBtnPressed, exitBounds);

        batch.end();

        handleInput();
    }

    private void drawButton(Texture normal, Texture pressed, Rectangle bounds) {
        if (Gdx.input.isTouched() && bounds.contains(touchPoint.x, touchPoint.y)) {
            batch.draw(pressed, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.draw(normal, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (playBounds.contains(touchPoint.x, touchPoint.y)) playPressed = true;
            else if (creditsBounds.contains(touchPoint.x, touchPoint.y)) creditsPressed = true;
            else if (exitBounds.contains(touchPoint.x, touchPoint.y)) exitPressed = true;
        }

        if (!Gdx.input.isTouched()) {
            if (playPressed && playBounds.contains(touchPoint.x, touchPoint.y)) {
                // Example: start game on FirstScreen
                game.setScreen(new FirstScreen(game));
            } else if (creditsPressed && creditsBounds.contains(touchPoint.x, touchPoint.y)) {
                // Go to credits screen
            } else if (exitPressed && exitBounds.contains(touchPoint.x, touchPoint.y)) {
                Gdx.app.exit();
            }

            playPressed = false;
            creditsPressed = false;
            exitPressed = false;
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        playBtn.dispose(); playBtnPressed.dispose();
        creditsBtn.dispose(); creditsBtnPressed.dispose();
        exitBtn.dispose(); exitBtnPressed.dispose();
    }
}