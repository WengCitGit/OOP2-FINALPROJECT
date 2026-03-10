package io.github.PASAN;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;

public class UsernameScreen implements Screen {

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

    public UsernameScreen(String mode) {
        this.gameMode = mode;

        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();

        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");
        background = new Texture("backgrounds/usernameScreen_background.jpg");
        enterBtn = new Texture("buttons/enter_button.png");
        enterBtnP = new Texture("buttons/enter_button_pressed.png");

        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        float centerX = WORLD_WIDTH / 2f;

        textFieldBounds = new Rectangle(centerX - 320, 650, 620, 80);
        enterBounds = new Rectangle(centerX + 40, 450, 250, 100);
        backBounds = new Rectangle(centerX - 300, 450, 250, 100);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
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

        boolean isTouchingBack = Gdx.input.isTouched() && backBounds.contains(touchPoint.x, touchPoint.y);
        boolean isTouchingEnter = Gdx.input.isTouched() && enterBounds.contains(touchPoint.x, touchPoint.y);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (isTouchingBack) {
            batch.draw(backBtnP, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        } else {
            batch.draw(backBtn, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        }
        if (isTouchingEnter) {
            batch.draw(enterBtnP, enterBounds.x, enterBounds.y, enterBounds.width, enterBounds.height);
        } else {
            batch.draw(enterBtn, enterBounds.x, enterBounds.y, enterBounds.width, enterBounds.height);
        }
        font.getData().setScale(3);
        //font.draw(batch, "ENTER USERNAME", 820, 750);

        String displayName = username;
        if (showCursor) displayName += "|";
        font.draw(batch, displayName, textFieldBounds.x + 20, textFieldBounds.y + 60);



        batch.end();

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.WHITE);
        shape.rect(textFieldBounds.x, textFieldBounds.y, textFieldBounds.width, textFieldBounds.height);

        shape.end();

        handleMouse();
    }

    private void handleMouse() {
        if (Gdx.input.justTouched()) {
            if (enterBounds.contains(touchPoint.x, touchPoint.y)) confirm();
            if (backBounds.contains(touchPoint.x, touchPoint.y)) backPressed = true;
        }

        if (backPressed && !Gdx.input.isTouched()) {
            if (backBounds.contains(touchPoint.x, touchPoint.y)) cancel();
            backPressed = false;
        }
    }

    private void confirm() {
        if (username.length() == 0) return;
        if (gameMode == null || gameMode.isEmpty()) gameMode = "DEFAULT";

        try {
            ((Main) Gdx.app.getApplicationListener()).setScreen(new CharacterSelector(username, gameMode));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancel() {
        ((Main) Gdx.app.getApplicationListener()).setScreen(new GameModeScreen());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shape.dispose();
        backBtn.dispose();
        backBtnP.dispose();
        background.dispose();
        enterBtn.dispose();
        enterBtnP.dispose();
    }
}