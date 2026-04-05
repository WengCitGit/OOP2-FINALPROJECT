package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;

public class VictoryScreen implements Screen {

    private Game game;
    private String username;
    private int score;
    private String subMessage;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 touch;

    private Texture victoryBackground;
    private Texture backBtn;
    private Texture backBtnP;

    private Rectangle menuBounds;
    private boolean menuPressed = false;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public VictoryScreen(Game game, String username) {
        this(game, username, 0, "You are the superior fighter!");
    }

    public VictoryScreen(Game game, String username, int score, String subMessage) {
        this.game       = game;
        this.username   = username;
        this.score      = score;
        this.subMessage = subMessage;

        batch = new SpriteBatch();
        font  = new BitmapFont();
        touch = new Vector3();

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        victoryBackground = new Texture("backgrounds/victory_background.jpg");
        backBtn  = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        float cx = WORLD_WIDTH / 2f;

        float btnW = 470f;
        float btnH = 130f;
        menuBounds = new Rectangle(cx - btnW / 2f, 80f, btnW, btnH);
    }

    @Override
    public void render(float delta) {
        camera.update();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw victory background covering the full screen
        if (victoryBackground != null) {
            batch.draw(victoryBackground, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        }

        // Draw back button (pressed or normal)
        boolean isTouchingBtn = menuBounds.contains(touch.x, touch.y);
        if (Gdx.input.isTouched() && isTouchingBtn) {
            if (backBtnP != null) batch.draw(backBtnP, menuBounds.x, menuBounds.y, menuBounds.width, menuBounds.height);
        } else {
            if (backBtn != null) batch.draw(backBtn, menuBounds.x, menuBounds.y, menuBounds.width, menuBounds.height);
        }

        // --- TEXT RENDERING ---

        font.getData().setScale(5.2f);
        font.setColor(Color.RED);
        GlyphLayout hailLayout = new GlyphLayout(font, "HAIL , " + username.toUpperCase() + "!");
        font.draw(batch, hailLayout,
                WORLD_WIDTH / 2f - hailLayout.width / 2f,
                WORLD_HEIGHT - 250f);

        font.getData().setScale(3.3f);
        font.setColor(Color.BLACK);
        GlyphLayout subLayout = new GlyphLayout(font, subMessage, Color.BLACK, 1100f, Align.center, false);
        font.draw(batch, subLayout,
                WORLD_WIDTH / 2f - 550f,
                WORLD_HEIGHT - 350f);

        font.getData().setScale(3f);
        font.setColor(Color.BLUE);
        GlyphLayout scoreLabel = new GlyphLayout(font, "Your Score:");
        font.draw(batch, scoreLabel,
                WORLD_WIDTH / 2f - scoreLabel.width / 2f,
                550f);

        font.getData().setScale(6.5f);
        font.setColor(Color.BLUE);
        String scoreString = String.format("%,d", score);
        GlyphLayout scoreNum = new GlyphLayout(font, scoreString);
        font.draw(batch, scoreNum,
                WORLD_WIDTH / 2f - scoreNum.width / 2f,
                500f);

        batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (menuBounds.contains(touch.x, touch.y)) {
                menuPressed = true;
                Main.clickSound.play();
            }
        }

        if (!Gdx.input.isTouched()) {
            if (menuPressed && menuBounds.contains(touch.x, touch.y)) {
                game.setScreen(new FirstScreen(game));
                dispose();
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
        if (victoryBackground != null) victoryBackground.dispose();
        if (backBtn != null) backBtn.dispose();
        if (backBtnP != null) backBtnP.dispose();
    }
}