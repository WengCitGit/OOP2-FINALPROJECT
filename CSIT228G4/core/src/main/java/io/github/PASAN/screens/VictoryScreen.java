package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.screens.FirstScreen;

public class VictoryScreen implements Screen {

    private Game game;
    private String username;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 touch;

    private Rectangle menuBounds;
    private boolean menuPressed = false;

    // Animated gold shimmer timer
    private float timer = 0f;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public VictoryScreen(Game game, String username) {
        this.game     = game;
        this.username = username;

        batch = new SpriteBatch();
        font  = new BitmapFont();
        touch = new Vector3();

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        float centerX = WORLD_WIDTH / 2f;
        menuBounds = new Rectangle(centerX - 250, 180, 500, 110);
    }

    @Override
    public void render(float delta) {
        timer += delta;

        camera.update();
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // --- VICTORY! title (pulsing gold) ---
        float pulse = 0.5f + 0.5f * (float) Math.sin(timer * 3f);
        font.getData().setScale(11f);
        font.setColor(1f, 0.84f * pulse + 0.16f, 0f, 1f);
        GlyphLayout victoryLayout = new GlyphLayout(font, "VICTORY!");
        font.draw(batch, victoryLayout,
                WORLD_WIDTH / 2f - victoryLayout.width / 2f,
                WORLD_HEIGHT / 2f + 280);

        // --- Congratulations line ---
        font.getData().setScale(4f);
        font.setColor(Color.WHITE);
        GlyphLayout congrats = new GlyphLayout(font, "Congratulations, " + username + "!");
        font.draw(batch, congrats,
                WORLD_WIDTH / 2f - congrats.width / 2f,
                WORLD_HEIGHT / 2f + 100);

        // --- Subtitle ---
        font.getData().setScale(3f);
        font.setColor(Color.LIGHT_GRAY);
        GlyphLayout sub = new GlyphLayout(font, "You defeated all 8 stages!");
        font.draw(batch, sub,
                WORLD_WIDTH / 2f - sub.width / 2f,
                WORLD_HEIGHT / 2f + 10);

        // --- Return to Menu button ---
        boolean hover = menuBounds.contains(touch.x, touch.y);
        font.getData().setScale(4f);
        font.setColor(hover ? Color.GOLD : Color.WHITE);
        GlyphLayout menuLabel = new GlyphLayout(font, "Return to Menu");
        font.draw(batch, menuLabel,
                menuBounds.x + (menuBounds.width  - menuLabel.width)  / 2f,
                menuBounds.y + (menuBounds.height + menuLabel.height) / 2f);

        // Underline
        font.getData().setScale(2.5f);
        font.setColor(Color.GRAY);
        font.draw(batch, "_______________", menuBounds.x, menuBounds.y + 10);

        font.getData().setScale(2.5f);
        batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (menuBounds.contains(touch.x, touch.y)) menuPressed = true;
        }

        if (!Gdx.input.isTouched()) {
            if (menuPressed && menuBounds.contains(touch.x, touch.y)) {
                game.setScreen(new FirstScreen(game));
            }
            menuPressed = false;
        }
    }

    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void show()   {}
    @Override public void hide()   {}
    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}