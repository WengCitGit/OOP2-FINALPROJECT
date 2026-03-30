package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.MainMenu;
import io.github.PASAN.screens.CharacterSelectorScreen;
import io.github.PASAN.screens.FirstScreen;

public class GameOverScreen implements Screen {

    private Game game;
    private String username;
    private String mode;
    private int winStreak;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 touch;

    private Texture background;
    private Texture yesBtn,  yesBtnPressed;
    private Texture noBtn,   noBtnPressed;

    private Rectangle yesBounds, noBounds, leaderboardBounds;
    private boolean yesPressed         = false;
    private boolean noPressed          = false;
    private boolean leaderboardPressed = false;

    private static final float WORLD_WIDTH  = 1920f;
    private static final float WORLD_HEIGHT = 1080f;

    // Button layout
    private static final float BTN_W  = 400f;
    private static final float BTN_H  = 130f;
    private static final float BTN_Y  = WORLD_HEIGHT / 2f - 130f; // was -230f
    private static final float YES_X  = WORLD_WIDTH  / 2f - 480f;
    private static final float NO_X   = WORLD_WIDTH  / 2f +  90f;


    // VIEW LEADERBOARD text hit-box
    private static final float LB_W = 560f;
    private static final float LB_H =  70f;
    private static final float LB_X = (WORLD_WIDTH - LB_W) / 2f;
    private static final float LB_Y = BTN_Y - 140f;

    // -------------------------------------------------------
    public GameOverScreen(Game game, String username) {
        this(game, username, "ARCADE", 0);
    }

    public GameOverScreen(Game game, String username, int winStreak) {
        this(game, username, "ENDLESS", winStreak);
    }

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
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);

        background    = new Texture("backgrounds/gameover_background.jpg");
        yesBtn        = new Texture("buttons/yes_button.png");
        yesBtnPressed = new Texture("buttons/yes_button_pressed.png");
        noBtn         = new Texture("buttons/no_button.png");
        noBtnPressed  = new Texture("buttons/no_button_pressed.png");

        yesBounds         = new Rectangle(YES_X, BTN_Y, BTN_W, BTN_H);
        noBounds          = new Rectangle(NO_X,  BTN_Y, BTN_W, BTN_H);
        leaderboardBounds = new Rectangle(LB_X,  LB_Y,  LB_W,  LB_H);
    }

    // -------------------------------------------------------
    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();


        batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        // YES button
        boolean touchingYes = Gdx.input.isTouched() && yesBounds.contains(touch.x, touch.y);
        batch.draw(touchingYes ? yesBtnPressed : yesBtn,
                yesBounds.x, yesBounds.y, yesBounds.width, yesBounds.height);

        // NO button
        boolean touchingNo = Gdx.input.isTouched() && noBounds.contains(touch.x, touch.y);
        batch.draw(touchingNo ? noBtnPressed : noBtn,
                noBounds.x, noBounds.y, noBounds.width, noBounds.height);

        // VIEW LEADERBOARD — plain text, same style as original
        boolean hoverLb = leaderboardBounds.contains(touch.x, touch.y);
        font.getData().setScale(3.5f);
        font.setColor(hoverLb ? Color.RED : Color.CYAN);
        GlyphLayout ll = new GlyphLayout(font, "VIEW LEADERBOARD");
        font.draw(batch, ll,
                WORLD_WIDTH / 2f - ll.width / 2f,
                leaderboardBounds.y + leaderboardBounds.height);

        // Endless-only streak line
        if (mode.equals("ENDLESS")) {
            font.getData().setScale(4.5f);
            font.setColor(Color.GOLD);
            String streakText = winStreak == 0
                    ? "You didn't win a single round..."
                    : "Win Streak: " + winStreak;
            GlyphLayout sl = new GlyphLayout(font, streakText);
            font.draw(batch, sl,
                    WORLD_WIDTH / 2f - sl.width / 2f,
                    WORLD_HEIGHT / 2f + 170f);
        }

        batch.end();
        handleInput();
    }

    // -------------------------------------------------------
    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (yesBounds        .contains(touch.x, touch.y)) yesPressed         = true;
            if (noBounds         .contains(touch.x, touch.y)) noPressed          = true;
            if (leaderboardBounds.contains(touch.x, touch.y)) leaderboardPressed = true;
        }

        if (!Gdx.input.isTouched()) {
            if (yesPressed && yesBounds.contains(touch.x, touch.y)) {
                game.setScreen(new CharacterSelectorScreen(username, mode, 1, "", ""));
            }
            if (noPressed && noBounds.contains(touch.x, touch.y)) {
                game.setScreen(new MainMenu(game));
            }
            if (leaderboardPressed && leaderboardBounds.contains(touch.x, touch.y)) {
                game.setScreen(new LeaderboardScreen(mode, this));
            }
            yesPressed = noPressed = leaderboardPressed = false;
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
        background.dispose();
        yesBtn.dispose();       yesBtnPressed.dispose();
        noBtn.dispose();        noBtnPressed.dispose();
    }
}