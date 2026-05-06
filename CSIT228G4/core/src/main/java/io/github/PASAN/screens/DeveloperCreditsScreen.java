
package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;

public class DeveloperCreditsScreen implements Screen {

    private Game game;
    private SpriteBatch batch;
    private Texture background;

    private int developerIndex;


    private static final String[] developerNames = {
            "Rothesa",
            "Kishanta",
            "Wengie",
            "Ayella",
            "Kunihiko"
    };


    private static final String[] developerImages = {
            "credits/rothesa_credit.jpg",
            "credits/kishanta_credit.jpg",
            "credits/wengie_credit.jpg",
            "credits/ayella_credit.jpg",
            "credits/kunihiko_credit.jpg"
    };

    private Texture creditImage;

    private Vector3 touch;
    private Texture backBtn, backBtnP;
    private Rectangle backBounds;

    private boolean backPressed = false;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public DeveloperCreditsScreen(Game game, int developerIndex) {
        this.game = game;
        this.developerIndex = developerIndex;

        batch = new SpriteBatch();
        touch = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");
        backBounds = new Rectangle(50, 50, 300, 100);

        // Load the credit image for this developer
        try {
            creditImage = new Texture(developerImages[developerIndex]);
            System.out.println("Loaded: " + developerImages[developerIndex]);
        } catch (Exception e) {
            System.out.println("FAILED to load: " + developerImages[developerIndex]);
            creditImage = createPlaceholder(Color.MAGENTA);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw the full screen credit image
        if (creditImage != null) {
            batch.draw(creditImage, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        } else {
            // Fallback if image not found
            batch.setColor(Color.BLACK);
            batch.draw(createPlaceholder(Color.BLACK), 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
            batch.setColor(Color.WHITE);
        }

        // Draw back button
        boolean isTouchingBack = Gdx.input.isTouched() && backBounds.contains(touch.x, touch.y);
        batch.draw(isTouchingBack ? backBtnP : backBtn,
                backBounds.x, backBounds.y, backBounds.width, backBounds.height);

        batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (backBounds.contains(touch.x, touch.y)) {
                backPressed = true;
                Main.clickSound.play();
            }
        }

        if (!Gdx.input.isTouched()) {
            if (backPressed && backBounds.contains(touch.x, touch.y)) {
                // Return to DevSelectorScreen
                ((Main) Gdx.app.getApplicationListener()).setScreen(new DevSelectorScreen(game));
            }
            backPressed = false;
        }
    }

    private Texture createPlaceholder(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        backBtn.dispose();
        backBtnP.dispose();
        if (creditImage != null) creditImage.dispose();
    }
}