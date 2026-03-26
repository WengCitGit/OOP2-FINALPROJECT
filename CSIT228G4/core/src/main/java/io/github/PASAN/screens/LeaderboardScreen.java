package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;
import io.github.PASAN.leaderboard.Leaderboard;
import io.github.PASAN.leaderboard.PlayerScore;

import java.util.ArrayList;

public class LeaderboardScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture background;
    private Texture backBtn, backBtnP;
    private Rectangle backBounds;
    private Vector3 touchPoint;

    private Leaderboard leaderboardManager;
    private ArrayList<PlayerScore> topScores;
    private String modeTitle;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;


    public LeaderboardScreen(String mode) {
        batch = new SpriteBatch();
        font = new BitmapFont();
        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);


        background = new Texture("backgrounds/temp_bg.png");
        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        backBounds = new Rectangle(50, 50, 250, 100);


        if (mode.equalsIgnoreCase("ARCADE")) {
            modeTitle = "ARCADE MODE TOP 10";
            leaderboardManager = new Leaderboard("arcade_scores.txt");
        } else {
            modeTitle = "ENDLESS MODE TOP 10";
            leaderboardManager = new Leaderboard("endless_scores.txt");
        }
        topScores = leaderboardManager.getTopScores();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        boolean isTouchingBack = Gdx.input.isTouched() && backBounds.contains(touchPoint.x, touchPoint.y);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();


        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);


        font.getData().setScale(4f);
        font.setColor(Color.GOLD);
        font.draw(batch, modeTitle, (WORLD_WIDTH / 2) - 300, 1000);

        font.getData().setScale(2.5f);
        font.setColor(Color.YELLOW);

        float startY = 850; // Starting Y position for the first score
        float rowSpacing = 70; // How much space between each row

        if (topScores.isEmpty()) {
            font.draw(batch, "No scores recorded yet! Be the first!", (WORLD_WIDTH / 2) - 300, startY);
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                PlayerScore ps = topScores.get(i);

                String rankText = (i + 1) + ". " + ps.getPlayer();
                String scoreText = String.valueOf(ps.getScore());

                font.draw(batch, rankText, (WORLD_WIDTH / 2) - 400, startY - (i * rowSpacing));
                font.draw(batch, scoreText, (WORLD_WIDTH / 2) + 300, startY - (i * rowSpacing));
            }
        }

        if (isTouchingBack) {
            batch.draw(backBtnP, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        } else {
            batch.draw(backBtn, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        }

        batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (backBounds.contains(touchPoint.x, touchPoint.y)) {
                ((Main) Gdx.app.getApplicationListener()).setScreen(new GameModeScreen(null));
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
        font.dispose();
        background.dispose();
        backBtn.dispose();
        backBtnP.dispose();
    }
}