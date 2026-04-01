package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.*;

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

    private Texture dialogueBox;
    private Texture backBtn;
    private Texture backBtnP;

    private Rectangle dialogueBounds;
    private Rectangle menuBounds;
    private boolean menuPressed = false;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public VictoryScreen(Game game, String username) {
        this(game, username, 0, "You are the superior fighter!");
    }
    public VictoryScreen(Game game, String username, int score, String subMessage) {
        this.game     = game;
        this.username = username;
        this.score    = score;
        this.subMessage = subMessage;

        batch = new SpriteBatch();
        font  = new BitmapFont();
        touch = new Vector3();

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        dialogueBox = new Texture("backgrounds/dialogue-box.png");
        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        float cx = WORLD_WIDTH / 2f;
        float cy = WORLD_HEIGHT / 2f;

        float boxW = 1300f;
        float boxH = 750f;
        dialogueBounds = new Rectangle(cx - boxW / 2f, cy - boxH / 2f + 50f, boxW, boxH);

        float btnW = 470f;
        float btnH = 130f;
        menuBounds = new Rectangle(cx - btnW / 2f, dialogueBounds.y - btnH - 30f, btnW, btnH);
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

        if (dialogueBox != null) {
            batch.draw(dialogueBox, dialogueBounds.x, dialogueBounds.y, dialogueBounds.width, dialogueBounds.height);
        }

        boolean isTouchingBtn = menuBounds.contains(touch.x, touch.y);
        if (Gdx.input.isTouched() && isTouchingBtn) {
            if (backBtnP != null) batch.draw(backBtnP, menuBounds.x, menuBounds.y, menuBounds.width, menuBounds.height);
        } else {
            if (backBtn != null) batch.draw(backBtn, menuBounds.x, menuBounds.y, menuBounds.width, menuBounds.height);
        }

        // --- TEXT RENDERING ---

        font.getData().setScale(3.2f);
        font.setColor(Color.RED);
        GlyphLayout hailLayout = new GlyphLayout(font, "HAIL " + username.toUpperCase());
        font.draw(batch, hailLayout,
                WORLD_WIDTH / 2f - hailLayout.width / 2f,
                dialogueBounds.y + dialogueBounds.height - 100f);

        font.getData().setScale(2.2f);
        font.setColor(Color.BLACK);
        String subText = subMessage;
        GlyphLayout subLayout = new GlyphLayout(font, subText, Color.BLACK, dialogueBounds.width - 200f, Align.center, false);
        font.draw(batch, subLayout,
                dialogueBounds.x + 100f,
                dialogueBounds.y + dialogueBounds.height - 230f);

        font.getData().setScale(2.5f);
        GlyphLayout scoreLabel = new GlyphLayout(font, "Your Score:");
        font.draw(batch, scoreLabel,
                WORLD_WIDTH / 2f - scoreLabel.width / 2f,
                dialogueBounds.y + 220f);

        font.getData().setScale(4.5f);
        String scoreString = String.format("%,d", score);
        GlyphLayout scoreNum = new GlyphLayout(font, scoreString);
        font.draw(batch, scoreNum,
                WORLD_WIDTH / 2f - scoreNum.width / 2f,
                dialogueBounds.y + 160f);

        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);
        GlyphLayout btnText = new GlyphLayout();
        font.draw(batch, btnText,
                menuBounds.x + (menuBounds.width - btnText.width) / 2f,
                menuBounds.y + (menuBounds.height + btnText.height) / 2f);

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
        if (dialogueBox != null) dialogueBox.dispose();
        if (backBtn != null) backBtn.dispose();
        if (backBtnP != null) backBtnP.dispose();
    }
}