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


public class GameLoreScreen implements Screen {

    private Game game;
    private SpriteBatch batch;
    private Texture background;

    // Buttons
    private Texture backBtn, backBtnP;
    private Texture mainStoryImg, mainStoryImgP;
    private Texture characterInfoImg, characterInfoImgP;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    // Rectangles
    private Rectangle backBounds;
    private Rectangle mainStoryBounds;
    private Rectangle characterInfoBounds;

    private Vector3 touchPoint;

    // Press states
    private boolean backPressed = false;
    private boolean mainStoryPressed = false;
    private boolean characterPressed = false;

    public GameLoreScreen(Game game) {
        this.game = game;

        batch = new SpriteBatch();
        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Background
        background = new Texture("backgrounds/gamelore_background.jpg");

        // Buttons
        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        mainStoryImg = new Texture("buttons/main_story_button.png");
        mainStoryImgP = new Texture("buttons/main_story_button_pressed.png");

        characterInfoImg = new Texture("buttons/character_info_button.png");
        characterInfoImgP = new Texture("buttons/character_info_button_pressed.png");

        float centerX = WORLD_WIDTH / 2f;
        float centerY = WORLD_HEIGHT / 2f;

        float boxW = 400;
        float boxH = 600;

        // Lore option rectangles
        mainStoryBounds = new Rectangle(centerX - boxW - 50, centerY - 250, boxW, boxH);
        characterInfoBounds = new Rectangle(centerX + 50, centerY - 250, boxW, boxH);

        // Back button
        float btnW = 410;
        float btnH = 110;
        float startY = 100;

        backBounds = new Rectangle(centerX - btnW / 2, startY, btnW, btnH);
    }

    @Override
    public void render(float delta) {
        camera.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // Main Story Button
        if (Gdx.input.isTouched() && mainStoryBounds.contains(touchPoint.x, touchPoint.y)) {
            batch.draw(mainStoryImgP, mainStoryBounds.x, mainStoryBounds.y, mainStoryBounds.width, mainStoryBounds.height);
        } else {
            batch.draw(mainStoryImg, mainStoryBounds.x, mainStoryBounds.y, mainStoryBounds.width, mainStoryBounds.height);
        }

        // Character Info Button
        if (Gdx.input.isTouched() && characterInfoBounds.contains(touchPoint.x, touchPoint.y)) {
            batch.draw(characterInfoImgP, characterInfoBounds.x, characterInfoBounds.y, characterInfoBounds.width, characterInfoBounds.height);
        } else {
            batch.draw(characterInfoImg, characterInfoBounds.x, characterInfoBounds.y, characterInfoBounds.width, characterInfoBounds.height);
        }

        // Back Button
        if (Gdx.input.isTouched() && backBounds.contains(touchPoint.x, touchPoint.y)) {
            batch.draw(backBtnP, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        } else {
            batch.draw(backBtn, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        }

        batch.end();

        handleInput();
    }

    private void handleInput() {

        if (Gdx.input.justTouched()) {
            if (backBounds.contains(touchPoint.x, touchPoint.y)) backPressed = true;
            else if (mainStoryBounds.contains(touchPoint.x, touchPoint.y)) mainStoryPressed = true;
            else if (characterInfoBounds.contains(touchPoint.x, touchPoint.y)) characterPressed = true;
        }

        if (!Gdx.input.isTouched()) {

            if (backPressed && backBounds.contains(touchPoint.x, touchPoint.y)) {
                game.setScreen(new FirstScreen(game));
            }

            else if (mainStoryPressed && mainStoryBounds.contains(touchPoint.x, touchPoint.y)) {
                System.out.println("Main Story Clicked");
                game.setScreen(new MainBackstoryScreen(game));
            }

            else if (characterPressed && characterInfoBounds.contains(touchPoint.x, touchPoint.y)) {
                System.out.println("Character Info Clicked");
                game.setScreen(new CharacterInfoScreen(game));
            }

            backPressed = false;
            mainStoryPressed = false;
            characterPressed = false;
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        backBtn.dispose();
        backBtnP.dispose();
        mainStoryImg.dispose();
        mainStoryImgP.dispose();
        characterInfoImg.dispose();
        characterInfoImgP.dispose();
    }
}

