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

public class GameModeScreen implements Screen {

    private SpriteBatch batch;
    private Texture background;

    // Normal and Pressed Textures
    private Texture pvpBtn, pvpBtnP, pvcBtn, pvcBtnP, arcadeBtn, arcadeBtnP, endlessBtn, endlessBtnP, backBtn, backBtnP;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private Rectangle pvpBounds, pvcBounds, arcadeBounds, endlessBounds, backBounds;
    private Vector3 touchPoint;

    public GameModeScreen() {
        batch = new SpriteBatch();
        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        background = new Texture("backgrounds/gamemode_background.jpg");

        // Load all state textures
        pvpBtn = new Texture("buttons/pvp_button.png");
        pvpBtnP = new Texture("buttons/pvp_button_pressed.png");
        pvcBtn = new Texture("buttons/pvc_button.png");
        pvcBtnP = new Texture("buttons/pvc_button_pressed.png");
        arcadeBtn = new Texture("buttons/arcade_button.png");
        arcadeBtnP = new Texture("buttons/arcade_button_pressed.png");
        endlessBtn = new Texture("buttons/endless_button.png");
        endlessBtnP = new Texture("buttons/endless_button_pressed.png");
        backBtn = new Texture("buttons/back_button.png"); // Reusing your back/exit asset
        backBtnP = new Texture("buttons/back_button_pressed.png");

        float centerX = WORLD_WIDTH / 2f;
        float centerY = WORLD_HEIGHT / 2f;

        // Coordinates for 5 buttons stacked vertically (Height reduced to 110 for fit)
        float btnW = 470;
        float btnH = 110;
        float startY = centerY + 250; // Top button position
        float spacing = 130;        // Distance between buttons

        pvpBounds = new Rectangle(centerX - btnW/2, startY, btnW, btnH);
        pvcBounds = new Rectangle(centerX - btnW/2, startY - spacing, btnW, btnH);
        arcadeBounds = new Rectangle(centerX - btnW/2, startY - (spacing * 2), btnW, btnH);
        endlessBounds = new Rectangle(centerX - btnW/2, startY - (spacing * 3), btnW, btnH);
        backBounds = new Rectangle(centerX - btnW/2, startY - (spacing * 4), btnW, btnH);
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

        // Draw buttons using the state logic
        drawButton(pvpBtn, pvpBtnP, pvpBounds);
        drawButton(pvcBtn, pvcBtnP, pvcBounds);
        drawButton(arcadeBtn, arcadeBtnP, arcadeBounds);
        drawButton(endlessBtn, endlessBtnP, endlessBounds);
        drawButton(backBtn, backBtnP, backBounds);

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
            if (pvpBounds.contains(touchPoint.x, touchPoint.y)) {
                // Start PVP logic
            } else if (pvcBounds.contains(touchPoint.x, touchPoint.y)) {
                // Start PVC logic
            } else if (arcadeBounds.contains(touchPoint.x, touchPoint.y)) {
                // Start Arcade logic
            } else if (endlessBounds.contains(touchPoint.x, touchPoint.y)) {
                // Start Endless logic
            } else if (backBounds.contains(touchPoint.x, touchPoint.y)) {
                // Go back to the screen that called this (usually FirstScreen)
                ((Main) Gdx.app.getApplicationListener()).setScreen(new FirstScreen());
            }
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
        pvpBtn.dispose(); pvpBtnP.dispose();
        pvcBtn.dispose(); pvcBtnP.dispose();
        arcadeBtn.dispose(); arcadeBtnP.dispose();
        endlessBtn.dispose(); endlessBtnP.dispose();
        backBtn.dispose(); backBtnP.dispose();
    }
}