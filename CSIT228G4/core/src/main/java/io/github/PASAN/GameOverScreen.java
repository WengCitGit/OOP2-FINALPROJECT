package io.github.PASAN;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import io.github.PASAN.modes.ArcadeMode;

public class GameOverScreen implements Screen {

    private Game game;
    private ArcadeMode arcadeMode;
    private SpriteBatch batch;
    private BitmapFont font;

    public GameOverScreen(Game game, ArcadeMode arcadeMode) {
        this.game = game;
        this.arcadeMode = arcadeMode;

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(3f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        font.draw(batch, "GAME OVER", 400, 600);
        font.draw(batch, "Press [R] to Play Again", 300, 500);
        font.draw(batch, "Press [M] to go to Main Menu", 250, 400);

        batch.end();

        // Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            arcadeMode.reset(); // reset arcade mode to start over
            arcadeMode.nextStage(); // start from first stage
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MainMenu(game));
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}