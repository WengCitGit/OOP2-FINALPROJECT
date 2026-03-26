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
    private String mode;       // "ARCADE" or "ENDLESS"
    private int winStreak;     // only meaningful in ENDLESS; 0 for ARCADE

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 touch;

    private Rectangle yesBounds, noBounds, leaderboardBounds;
    private boolean yesPressed = false;
    private boolean noPressed  = false;
    private boolean leaderboardPressed = false;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    // -------------------------------------------------------
    // ARCADE constructor — no streak to show
    // -------------------------------------------------------
    public GameOverScreen(Game game, String username) {
        this(game, username, "ARCADE", 0);
    }

    // -------------------------------------------------------
    // ENDLESS constructor — streak displayed on screen
    // -------------------------------------------------------
    public GameOverScreen(Game game, String username, int winStreak) {
        this(game, username, "ENDLESS", winStreak);
    }

    // -------------------------------------------------------
    // Shared setup
    // -------------------------------------------------------
    private GameOverScreen(Game game, String username, String mode, int winStreak) {
        this.game      = game;
        this.username  = username;
        this.mode      = mode;
        this.winStreak = winStreak;

        batch = new SpriteBatch();
        font  = new BitmapFont();
        touch = new Vector3();

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        float centerX = WORLD_WIDTH  / 2f;
        float centerY = WORLD_HEIGHT / 2f;

        // Buttons sit a little lower when the streak line is visible
        float buttonY = mode.equals("ENDLESS") ? centerY - 230 : centerY - 180;
        yesBounds = new Rectangle(centerX - 350, buttonY, 280, 100);
        noBounds  = new Rectangle(centerX +  70, buttonY, 280, 100);
        leaderboardBounds = new Rectangle(centerX - 300, buttonY - 140, 600, 100);
    }

    // -------------------------------------------------------
    // RENDER
    // -------------------------------------------------------
    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // --- GAME OVER title ---
        font.getData().setScale(9f);
        font.setColor(Color.RED);
        GlyphLayout titleLayout = new GlyphLayout(font, "GAME OVER");
        font.draw(batch, titleLayout,
                WORLD_WIDTH / 2f - titleLayout.width / 2f,
                WORLD_HEIGHT / 2f + 280);

        // --- Endless-only: win streak line ---
        if (mode.equals("ENDLESS")) {
            font.getData().setScale(4.5f);
            font.setColor(Color.GOLD);
            String streakText = winStreak == 0
                    ? "You didn't win a single round..."
                    : "Win Streak: " + winStreak;
            GlyphLayout streakLayout = new GlyphLayout(font, streakText);
            font.draw(batch, streakLayout,
                    WORLD_WIDTH / 2f - streakLayout.width / 2f,
                    WORLD_HEIGHT / 2f + 170);
        }

        // --- "Play again?" question ---
        font.getData().setScale(4f);
        font.setColor(Color.WHITE);
        GlyphLayout questionLayout = new GlyphLayout(font, "Do you want to play again?");
        font.draw(batch, questionLayout,
                WORLD_WIDTH / 2f - questionLayout.width / 2f,
                WORLD_HEIGHT / 2f + 50);

        // --- YES button ---
        boolean hoverYes = yesBounds.contains(touch.x, touch.y);
        font.getData().setScale(5f);
        font.setColor(hoverYes ? Color.GOLD : Color.GREEN);
        GlyphLayout yesLayout = new GlyphLayout(font, "YES");
        font.draw(batch, yesLayout,
                yesBounds.x + (yesBounds.width  - yesLayout.width)  / 2f,
                yesBounds.y + (yesBounds.height + yesLayout.height) / 2f);

        // --- NO button ---
        boolean hoverNo = noBounds.contains(touch.x, touch.y);
        font.setColor(hoverNo ? Color.GOLD : Color.RED);
        GlyphLayout noLayout = new GlyphLayout(font, "NO");
        font.draw(batch, noLayout,
                noBounds.x + (noBounds.width  - noLayout.width)  / 2f,
                noBounds.y + (noBounds.height + noLayout.height) / 2f);
        boolean hoverLeaderboard = leaderboardBounds.contains(touch.x, touch.y);
        font.getData().setScale(3.5f); // Slightly smaller than YES/NO so it fits nicely
        font.setColor(hoverLeaderboard ? Color.GOLD : Color.CYAN); // Cyan default, turns Gold when hovered
        GlyphLayout leaderboardLayout = new GlyphLayout(font, "VIEW LEADERBOARD");
        font.draw(batch, leaderboardLayout,
                leaderboardBounds.x + (leaderboardBounds.width  - leaderboardLayout.width)  / 2f,
                leaderboardBounds.y + (leaderboardBounds.height + leaderboardLayout.height) / 2f);

        font.getData().setScale(2.5f);
        batch.end();

        handleInput();
    }

    // -------------------------------------------------------
    // INPUT
    // -------------------------------------------------------
    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (yesBounds.contains(touch.x, touch.y)) yesPressed = true;
            if (noBounds.contains(touch.x, touch.y))  noPressed  = true;
            if (leaderboardBounds.contains(touch.x, touch.y)) leaderboardPressed = true;
        }

        if (!Gdx.input.isTouched()) {
            if (yesPressed && yesBounds.contains(touch.x, touch.y)) {
                // Route back to the correct character select mode
                game.setScreen(new CharacterSelectorScreen(username, mode, 1, "", ""));
            }
            if (noPressed && noBounds.contains(touch.x, touch.y)) {
                game.setScreen(new FirstScreen(game));
            }
            if (leaderboardPressed && leaderboardBounds.contains(touch.x, touch.y)) {
                game.setScreen(new LeaderboardScreen(mode));
            }
            yesPressed = false;
            noPressed  = false;
            leaderboardPressed = false;
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