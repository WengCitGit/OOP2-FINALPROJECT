package io.github.PASAN;

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

public class GameLoreScreen implements Screen {

    private Main game;
    private SpriteBatch batch;
    private Texture background;
    private Texture backBtn, backBtnP;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private Rectangle backBounds;
    private Vector3 touchPoint;
    private boolean backPressed = false;

    public GameLoreScreen(Main game) {
        this.game = game;

        batch = new SpriteBatch();
        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Background for lore screen
        background = new Texture("backgrounds/gamemode_background.jpg");

        // Back button
        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        // Place back button at bottom-center
        float btnW = 470;
        float btnH = 110;
        float centerX = WORLD_WIDTH / 2f;
        float startY = 100; // Distance from bottom
        backBounds = new Rectangle(centerX - btnW / 2, startY, btnW, btnH);
    }

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update touch position
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw background
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // Draw back button
        if (Gdx.input.isTouched() && backBounds.contains(touchPoint.x, touchPoint.y)) {
            batch.draw(backBtnP, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        } else {
            batch.draw(backBtn, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        }

        batch.end();

        handleInput();
    }

    private void handleInput() {
        // Detect initial press
        if (Gdx.input.justTouched()) {
            if (backBounds.contains(touchPoint.x, touchPoint.y)) backPressed = true;
        }

        // Detect release
        if (!Gdx.input.isTouched()) {
            if (backPressed && backBounds.contains(touchPoint.x, touchPoint.y)) {
                // Go back to first screen or menu
                game.setScreen(new FirstScreen());
            }
            backPressed = false;
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
        backBtn.dispose();
        backBtnP.dispose();
    }
}
