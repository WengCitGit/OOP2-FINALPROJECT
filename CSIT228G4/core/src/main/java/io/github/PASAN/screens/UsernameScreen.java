package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;

public class UsernameScreen implements Screen {

    private Game game;

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;

    private String username = "";
    private String gameMode;

    private Rectangle enterBounds;
    private Rectangle textFieldBounds;
    private Rectangle backBounds;

    private Vector3 touchPoint;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture backBtn, backBtnP;
    private Texture enterBtn, enterBtnP;
    private Texture background;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private float cursorTimer = 0;
    private boolean showCursor = true;
    private boolean backPressed = false;

    private int playerNum = 1;
    private String p1Name = "";
    private String p1Char = "";
    private boolean showNameError = false;

    // MAIN constructor
    public UsernameScreen(Game game, String mode) {
        this.game = game;
        this.gameMode = mode;

        init();
        loadAssets(mode);
    }

    // PVP constructor
    public UsernameScreen(Game game, String mode, int playerNum, String p1Name, String p1Char) {
        this(game, mode);
        this.playerNum = playerNum;
        this.p1Name = p1Name;
        this.p1Char = p1Char;

        if (playerNum == 2) {
            background.dispose();
            background = new Texture("backgrounds/usernameScreenPlayer2_background.jpg");
        }
    }

    private void init() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();

        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        float centerX = WORLD_WIDTH / 2f;

        textFieldBounds = new Rectangle(centerX - 320, 650, 620, 80);
        enterBounds = new Rectangle(centerX + 40, 450, 250, 100);
        backBounds = new Rectangle(centerX - 300, 450, 250, 100);
    }

    private void loadAssets(String mode) {
        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        enterBtn = new Texture("buttons/enter_button.png");
        enterBtnP = new Texture("buttons/enter_button_pressed.png");

        background = new Texture(
                (mode != null && mode.contains("PVP"))
                        ? "backgrounds/usernameScreenPlayer1_background.jpg"
                        : "backgrounds/usernameScreen_background.jpg"
        );
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyTyped(char character) {
                showNameError = false;

                if (character == '\b' && username.length() > 0) {
                    username = username.substring(0, username.length() - 1);
                } else if (Character.isLetterOrDigit(character) && username.length() < 12) {
                    username += character;
                }
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) cancel();
                if (keycode == Input.Keys.ENTER) confirm();
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {

        camera.update();

        cursorTimer += delta;
        if (cursorTimer > 0.5f) {
            showCursor = !showCursor;
            cursorTimer = 0;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        boolean backTouch = Gdx.input.isTouched() && backBounds.contains(touchPoint.x, touchPoint.y);
        boolean enterTouch = Gdx.input.isTouched() && enterBounds.contains(touchPoint.x, touchPoint.y);

        batch.draw(backTouch ? backBtnP : backBtn,
                backBounds.x, backBounds.y, backBounds.width, backBounds.height);

        batch.draw(enterTouch ? enterBtnP : enterBtn,
                enterBounds.x, enterBounds.y, enterBounds.width, enterBounds.height);

        font.getData().setScale(3);
        font.setColor(Color.WHITE);

        String display = username + (showCursor ? "|" : "");
        font.draw(batch, display,
                textFieldBounds.x + 20,
                textFieldBounds.y + 60);

        if (showNameError) {
            font.getData().setScale(2f);
            font.setColor(Color.RED);

            GlyphLayout gl = new GlyphLayout(font, "NAME ALREADY TAKEN BY PLAYER 1!");
            font.draw(batch, gl,
                    textFieldBounds.x + (textFieldBounds.width - gl.width) / 2f,
                    textFieldBounds.y - 20);

            font.setColor(Color.WHITE);
        }

        batch.end();

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.WHITE);
        shape.rect(textFieldBounds.x, textFieldBounds.y,
                textFieldBounds.width, textFieldBounds.height);
        shape.end();

        handleMouse();
    }

    private void handleMouse() {

        if (Gdx.input.justTouched()) {
            if (enterBounds.contains(touchPoint.x, touchPoint.y)) {
                confirm();
                Main.clickSound.play();
            }

            if (backBounds.contains(touchPoint.x, touchPoint.y)) {
                backPressed = true;
                Main.clickSound.play();
            }
        }

        if (backPressed && !Gdx.input.isTouched()) {
            if (backBounds.contains(touchPoint.x, touchPoint.y)) {
                cancel();
            }
            backPressed = false;
        }
    }

    private void confirm() {
        if (username.isEmpty()) return;

        if (playerNum == 2 && username.equalsIgnoreCase(p1Name)) {
            showNameError = true;
            return;
        }

        game.setScreen(new CharacterSelectorScreen(
                game, username, gameMode, playerNum, p1Name, p1Char
        ));
    }

    private void cancel() {
        ((Main) Gdx.app.getApplicationListener())
                .setScreen(new GameModeScreen((Game) Gdx.app.getApplicationListener()));
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shape.dispose();

        backBtn.dispose();
        backBtnP.dispose();
        enterBtn.dispose();
        enterBtnP.dispose();
        background.dispose();
    }
}