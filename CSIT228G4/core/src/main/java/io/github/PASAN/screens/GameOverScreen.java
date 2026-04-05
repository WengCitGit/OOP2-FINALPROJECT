package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;
import io.github.PASAN.MainMenu;

public class GameOverScreen implements Screen {

    private Game game;
    private String username;
    private String mode;
    private int winStreak;
    private boolean pvcPlayerWon;

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

    // --- Repositioned Layout Constants ---
    private static final float BTN_W  = 400f;
    private static final float BTN_H  = 130f;
    // Buttons are moved down significantly to the bottom black area
    private static final float BTN_Y  = WORLD_HEIGHT / 2f - 100f;
    private static final float YES_X  = WORLD_WIDTH  / 2f - 480f;
    private static final float NO_X   = WORLD_WIDTH  / 2f +  90f;

    // LEADERBOARD text moved further down below buttons
    private static final float LB_W = 560f;
    private static final float LB_H =  70f;
    private static final float LB_X = (WORLD_WIDTH - LB_W) / 2f;
    private static final float LB_Y = 330f;

    // Endless/PVP/PVC Sub-message position - placed in dark center black space
    private static final float MSG_Y = WORLD_HEIGHT / 2f - -140f;

    // -------------------------------------------------------
    // Constructor 1: ARCADE (Defaults on Arcade Loss)
    public GameOverScreen(Game game, String username) {
        this(game, username, "ARCADE", 0, false);
    }

    // Constructor 2: ENDLESS (Loss passes streak)
    public GameOverScreen(Game game, String username, int winStreak) {
        this(game, username, "ENDLESS", winStreak, false);
    }

    // Constructor 3: PVP (Winner name, dummyPVP used for overloading)
    public GameOverScreen(Game game, String winnerName, boolean dummyPVP) {
        // Here, 'username' will hold the winner's specific name.
        this(game, winnerName, "PVP", 0, false);
    }

    // Constructor 4: PVC (Username, who won, dummyPVC used for overloading)
    public GameOverScreen(Game game, String username, boolean playerWon, boolean dummyPVC) {
        this(game, username, "PVC", 0, playerWon);
    }

    // Private Master Constructor
    private GameOverScreen(Game game, String username, String mode, int winStreak, boolean pvcPlayerWon) {
        this.game      = game;
        this.username  = username;
        this.mode      = mode;
        this.winStreak = winStreak;
        this.pvcPlayerWon = pvcPlayerWon;

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

        if (background != null) batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        // Draw YES button with hover/press logic
        boolean touchingYes = Gdx.input.isTouched() && yesBounds.contains(touch.x, touch.y);
        batch.draw(touchingYes ? yesBtnPressed : yesBtn,
                yesBounds.x, yesBounds.y, yesBounds.width, yesBounds.height);

        // Draw NO button with hover/press logic
        boolean touchingNo = Gdx.input.isTouched() && noBounds.contains(touch.x, touch.y);
        batch.draw(touchingNo ? noBtnPressed : noBtn,
                noBounds.x, noBounds.y, noBounds.width, noBounds.height);

        // Draw VIEW LEADERBOARD text
        boolean hoverLb = leaderboardBounds.contains(touch.x, touch.y);
        font.getData().setScale(3.5f);
        font.setColor(hoverLb ? Color.RED : Color.CYAN);
        GlyphLayout ll = new GlyphLayout(font, "VIEW LEADERBOARD");
        font.draw(batch, ll,
                WORLD_WIDTH / 2f - ll.width / 2f,
                leaderboardBounds.y + leaderboardBounds.height);

        // --- DYNAMIC SUB-MESSAGE RENDERING (ENDLESS, PVP, PVC) ---
        // DRAWN IN BLACK SPACE AT MSG_Y
        font.getData().setScale(4.5f);
        font.setColor(Color.GOLD);
        String finalMsg = "";

        if (mode.equals("ENDLESS")) {
            finalMsg = winStreak == 0
                    ? "You didn't win a single round..."
                    : "Win Streak: " + winStreak;
        } else if (mode.equals("PVP")) {
            // Username holds the winner's specific name from the constructor.
            finalMsg = username + " WINS!";
        } else if (mode.equals("PVC")) {
            // Check Boolean state from PVC constructor
            if (pvcPlayerWon) {
                font.setColor(Color.LIME);
                finalMsg = "CONGRATULATIONS, " + username + "!";
            } else {
                font.setColor(Color.RED);
                finalMsg = "CPU WINS!";
            }
        }

        // Draw the calculated message
        if (!finalMsg.equals("")) {
            GlyphLayout ml = new GlyphLayout(font, finalMsg);
            font.draw(batch, ml, WORLD_WIDTH / 2f - ml.width / 2f, MSG_Y);
        }

        font.setColor(Color.WHITE); // Reset font color
        batch.end();
        handleInput();
    }

    // -------------------------------------------------------
    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (yesBounds.contains(touch.x, touch.y)) {
                yesPressed = true;
                Main.clickSound.play();
            }
            if (noBounds.contains(touch.x, touch.y)) {
                noPressed = true;
                Main.clickSound.play();
            }
            if (leaderboardBounds.contains(touch.x, touch.y)) {
                leaderboardPressed = true;
                Main.clickSound.play();
            }
        }

        if (!Gdx.input.isTouched()) {
            if (yesPressed && yesBounds.contains(touch.x, touch.y)) {
                // Return directly to Character Select, keeping the current mode and username!
                game.setScreen(new CharacterSelectorScreen(username, mode, 1, "", ""));
            }
            if (noPressed && noBounds.contains(touch.x, touch.y)) {
                // Exit to Main Menu.
                ((Game)Gdx.app.getApplicationListener()).setScreen(new MainMenu(game));
            }
            if (leaderboardPressed && leaderboardBounds.contains(touch.x, touch.y)) {
                 game.setScreen(new LeaderboardScreen(mode, this));
            }
            yesPressed = noPressed = leaderboardPressed = false;
        }
    }

    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void show()   {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (background != null) background.dispose();
        if (yesBtn != null) yesBtn.dispose();
        if (yesBtnPressed != null) yesBtnPressed.dispose();
        if (noBtn != null) noBtn.dispose();
        if (noBtnPressed != null) noBtnPressed.dispose();
    }
}