package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.screens.CharacterSelectorScreen;
import io.github.PASAN.screens.FirstScreen;

public class GameOverScreen implements Screen {

    private Game game;
    private String username;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 touch;

    private Rectangle yesBounds, noBounds;
    private boolean yesPressed = false;
    private boolean noPressed  = false;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public GameOverScreen(Game game, String username) {
        this.game     = game;
        this.username = username;

        batch = new SpriteBatch();
        font  = new BitmapFont();
        touch = new Vector3();

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        float centerX = WORLD_WIDTH  / 2f;
        float centerY = WORLD_HEIGHT / 2f;

        yesBounds = new Rectangle(centerX - 350, centerY - 180, 280, 100);
        noBounds  = new Rectangle(centerX +  70, centerY - 180, 280, 100);
    }

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // GAME OVER title
        font.getData().setScale(9f);
        font.setColor(Color.RED);
        GlyphLayout titleLayout = new GlyphLayout(font, "GAME OVER");
        font.draw(batch, titleLayout,
                WORLD_WIDTH / 2f - titleLayout.width / 2f,
                WORLD_HEIGHT / 2f + 220);

        // Question
        font.getData().setScale(4f);
        font.setColor(Color.WHITE);
        GlyphLayout questionLayout = new GlyphLayout(font, "Do you want to play again?");
        font.draw(batch, questionLayout,
                WORLD_WIDTH / 2f - questionLayout.width / 2f,
                WORLD_HEIGHT / 2f + 50);

        // YES button
        boolean hoverYes = yesBounds.contains(touch.x, touch.y);
        font.getData().setScale(5f);
        font.setColor(hoverYes ? Color.GOLD : Color.GREEN);
        GlyphLayout yesLayout = new GlyphLayout(font, "YES");
        font.draw(batch, yesLayout,
                yesBounds.x + (yesBounds.width  - yesLayout.width)  / 2f,
                yesBounds.y + (yesBounds.height + yesLayout.height) / 2f);

        // NO button
        boolean hoverNo = noBounds.contains(touch.x, touch.y);
        font.setColor(hoverNo ? Color.GOLD : Color.RED);
        GlyphLayout noLayout = new GlyphLayout(font, "NO");
        font.draw(batch, noLayout,
                noBounds.x + (noBounds.width  - noLayout.width)  / 2f,
                noBounds.y + (noBounds.height + noLayout.height) / 2f);

        font.getData().setScale(2.5f);
        batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (yesBounds.contains(touch.x, touch.y)) yesPressed = true;
            if (noBounds.contains(touch.x, touch.y))  noPressed  = true;
        }

        if (!Gdx.input.isTouched()) {
            if (yesPressed && yesBounds.contains(touch.x, touch.y)) {
                // Back to character select for Arcade
                game.setScreen(new CharacterSelectorScreen(username, "ARCADE", 1, "", ""));
            }
            if (noPressed && noBounds.contains(touch.x, touch.y)) {
                // Back to main menu
                game.setScreen(new FirstScreen(game));
            }
            yesPressed = false;
            noPressed  = false;
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