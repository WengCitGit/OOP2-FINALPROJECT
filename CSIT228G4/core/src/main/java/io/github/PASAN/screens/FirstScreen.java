package io.github.PASAN.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.PASAN.Main;
import io.github.PASAN.MainMenu;

public class FirstScreen implements Screen {

    private Game game;

    private SpriteBatch batch;
    private Texture background;

    // Normal textures
    private Texture modeBtn, loreBtn, backBtn;
    // Pressed textures
    private Texture modeBtnPressed, loreBtnPressed, backBtnPressed;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private Rectangle modeBounds, loreBounds, backBounds;
    private Vector3 touchPoint;

    private boolean modePressed = false;
    private boolean lorePressed = false;
    private boolean backPressed = false;

    public FirstScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        background = new Texture("backgrounds/mainmenu_background.jpg");

        // Load Normal States
        modeBtn = new Texture("buttons/game_mode_button.png");
        loreBtn = new Texture("buttons/game_lore_button.png");
        backBtn = new Texture("buttons/back_button.png"); // Using this for the "BACK" button

        // Load Pressed States
        modeBtnPressed = new Texture("buttons/game_mode_button_pressed.png");
        loreBtnPressed = new Texture("buttons/game_lore_button_pressed.png");
        backBtnPressed = new Texture("buttons/back_button_pressed.png");

        float centerX = WORLD_WIDTH / 2f;
        float centerY = WORLD_HEIGHT / 2f;

        modeBounds = new Rectangle(centerX - 235, centerY + 30, 470, 140);
        loreBounds = new Rectangle(centerX - 235, centerY - 120, 470, 140);
        backBounds = new Rectangle(centerX - 235, centerY - 270, 470, 140);
    }

    @Override
    public void render(float delta) {
        camera.update();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update touchPoint coordinates for unprojecting
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // Helper method to draw based on state
        drawButton(modeBtn, modeBtnPressed, modeBounds);
        drawButton(loreBtn, loreBtnPressed, loreBounds);
        drawButton(backBtn, backBtnPressed, backBounds);

        batch.end();

        handleInput();
    }

    private void drawButton(Texture normal, Texture pressed, Rectangle bounds) {
        // As long as the mouse/finger is down inside the rectangle, show the pressed PNG
        if (Gdx.input.isTouched() && bounds.contains(touchPoint.x, touchPoint.y)) {
            batch.draw(pressed, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.draw(normal, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private void handleInput()
    {
        // Detect initial press
        if (Gdx.input.justTouched())
        {
            if (modeBounds.contains(touchPoint.x, touchPoint.y))
            {
                modePressed = true;
                Main.clickSound.play();
            }
            else if (loreBounds.contains(touchPoint.x, touchPoint.y))
            {
                lorePressed = true;
                Main.clickSound.play();
            }
            else if (backBounds.contains(touchPoint.x, touchPoint.y))
            {
                backPressed = true;
                Main.clickSound.play();
            }
        }

        // Detect release
        if (!Gdx.input.isTouched())
        {
            if (modePressed && modeBounds.contains(touchPoint.x, touchPoint.y)) game.setScreen(new GameModeScreen(game));
            else if (lorePressed && loreBounds.contains(touchPoint.x, touchPoint.y)){
                game.setScreen(new GameLoreScreen(game));
            }
            else if (backPressed && backBounds.contains(touchPoint.x, touchPoint.y)) game.setScreen(new MainMenu(game));

            modePressed = false;
            lorePressed = false;
            backPressed = false;
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
        background.dispose();
        modeBtn.dispose(); modeBtnPressed.dispose();
        loreBtn.dispose(); loreBtnPressed.dispose();
        backBtn.dispose(); backBtnPressed.dispose();
    }
}