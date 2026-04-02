package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;
import io.github.PASAN.leaderboard.Leaderboard;
import io.github.PASAN.leaderboard.PlayerScore;

import java.util.ArrayList;

public class LeaderboardScreen implements Screen {

    private Screen returnScreen;

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
    private Stage stage;
    private ScrollPane scrollPane;

    private boolean backPressed = false;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public LeaderboardScreen(String mode, Screen returnScreen) {
        this.returnScreen = returnScreen;

        batch      = new SpriteBatch();
        font       = new BitmapFont();
        touchPoint = new Vector3();

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        viewport.apply();

        background = new Texture("backgrounds/leaderboard_bg.png");
        backBtn    = new Texture("buttons/back_button.png");
        backBtnP   = new Texture("buttons/back_button_pressed.png");

        backBounds = new Rectangle(50, 50, 250, 100);
        if (mode.equalsIgnoreCase("ARCADE")) {
            modeTitle          = "ARCADE MODE TOP 10";
            leaderboardManager = new Leaderboard("arcade_scores.txt");

        } else if (mode.equalsIgnoreCase("PVC")) {
            modeTitle          = "PVC MODE TOP 10";
            leaderboardManager = new Leaderboard("pvc_scores.txt");

        } else if (mode.equalsIgnoreCase("PVP")) {
            modeTitle          = "PVP MODE TOP 10";
            leaderboardManager = new Leaderboard("pvp_scores.txt");

        } else {
            modeTitle          = "ENDLESS MODE TOP 10";
            leaderboardManager = new Leaderboard("endless_scores.txt");
        }

        topScores = leaderboardManager.getTopScores();
        setupScrollableLeaderboard();
    }

    private void setupScrollableLeaderboard() {
        stage = new Stage(viewport, batch);

        font.getData().setScale(2.5f);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Table innerTable = new Table();
        innerTable.top();

        if (topScores.isEmpty()) {
            Label emptyLabel = new Label("NO SCORES RECORDED YET! BE THE FIRST!", labelStyle);
            innerTable.add(emptyLabel).padTop(50);
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                PlayerScore ps = topScores.get(i);
                String rankText  = (i + 1) + ". " + ps.getPlayer();
                String scoreText = String.valueOf(ps.getScore());

                Label rankLabel  = new Label(rankText,  labelStyle);
                Label scoreLabel = new Label(scoreText, labelStyle);

                innerTable.add(rankLabel) .left() .width(400).padBottom(30);
                innerTable.add(scoreLabel).right().width(200).padBottom(30);
                innerTable.row();
            }
        }

        scrollPane = new ScrollPane(innerTable);
        scrollPane.setScrollingDisabled(true, false);

        float scrollWidth  = 750;
        float scrollHeight = 400;
        scrollPane.setBounds(
                (WORLD_WIDTH / 2) - (scrollWidth / 2) + 45,
                150, scrollWidth, scrollHeight);

        stage.addActor(scrollPane);
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPoint.set(screenX, screenY, 0);
                viewport.unproject(touchPoint);
                if (backBounds.contains(touchPoint.x, touchPoint.y)) {
                    backPressed = true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                touchPoint.set(screenX, screenY, 0);
                viewport.unproject(touchPoint);
                if (backPressed && backBounds.contains(touchPoint.x, touchPoint.y)) {
                    ((Main) Gdx.app.getApplicationListener()).setScreen(returnScreen);
                }
                backPressed = false;
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

    // -------------------------------------------------------

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // Update touchPoint for visual hover only (navigation is in InputAdapter)
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        boolean isTouchingBack = Gdx.input.isTouched()
                && backBounds.contains(touchPoint.x, touchPoint.y);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        font.getData().setScale(3.5f);
        font.setColor(Color.WHITE);
        GlyphLayout titleLayout = new GlyphLayout(font, modeTitle);
        font.draw(batch, titleLayout,
                (WORLD_WIDTH / 2) - (titleLayout.width / 2), 650);

        batch.draw(
                isTouchingBack ? backBtnP : backBtn,
                backBounds.x, backBounds.y, backBounds.width, backBounds.height);

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    // -------------------------------------------------------

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        background.dispose();
        backBtn.dispose();
        backBtnP.dispose();
        stage.dispose();
    }
}