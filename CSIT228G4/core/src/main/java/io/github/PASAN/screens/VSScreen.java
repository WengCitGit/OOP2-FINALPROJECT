package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;
import io.github.PASAN.ArcadeBattleScreen;

public class VSScreen implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    //private String username;
    private String mode;
    //private String playerCharacterName;
    //private String enemyCharacterName;

    private Game game;

    private String player1Name;
    private String player2Name;
    private String player1Char;
    private String player2Char;

    private String[] allCharacters = {
            "Jollibee", "McDonald", "Colonel Sanders", "Burger King",
            "Wendy", "Jack in the Box", "Little Caesar", "Chief Khai"
    };


    private Texture background;
    private Texture popupBoard;
    private Texture vsLogo;
    private Texture playerSprite;
    private Texture enemySprite;

    private Texture startBtn, startBtnP;
    private Rectangle startBounds;
    private Vector3 touch;
    private boolean startPressed = false;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public VSScreen(String p1Name, String p2Name, String mode, String p1Char, String p2Char)
    {
        this.player1Name = p1Name;
        this.player2Name = p2Name;
        this.mode = mode;
        this.player1Char = p1Char;
        this.player2Char = p2Char;

        // random enemy character
        //this.enemyCharacterName = allCharacters[MathUtils.random(0, allCharacters.length - 1)];
        //kfc for now
        //this.enemyCharacterName = "Colonel Sanders";
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();


        background = new Texture("backgrounds/temp_bg.png");
        popupBoard = new Texture("backgrounds/vsbg.png");

        startBtn = new Texture("buttons/start_button.png");
        startBtnP = new Texture("buttons/start_button_pressed.png");

        playerSprite = new Texture("characters/" + player1Char.replace(" ", "") + ".png");
        enemySprite = new Texture("characters/" + player2Char.replace(" ", "") + ".png");


        startBounds = new Rectangle((WORLD_WIDTH / 2) - 150, 150, 300, 100);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);


        float boardWidth = 1200;
        float boardHeight = 800;
        float boardX = (WORLD_WIDTH - boardWidth) / 2;
        float boardY = (WORLD_HEIGHT - boardHeight) / 2 + 50;
        batch.draw(popupBoard, boardX, boardY, boardWidth, boardHeight);

        //left character
        batch.draw(playerSprite, boardX + 150, boardY + 200, 300, 400);

        //right character
        batch.draw(enemySprite, boardX + 750, boardY + 200, 300, 400);

        //start button
        boolean isTouchingStart = Gdx.input.isTouched() && startBounds.contains(touch.x, touch.y);
        if (isTouchingStart) {
            batch.draw(startBtnP, startBounds.x, startBounds.y, startBounds.width, startBounds.height);
        } else {
            batch.draw(startBtn, startBounds.x, startBounds.y, startBounds.width, startBounds.height);
        }

        batch.end();
        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (startBounds.contains(touch.x, touch.y)) {
                startPressed = true;
            }
        }

        if (!Gdx.input.isTouched()) {
            if (startPressed && startBounds.contains(touch.x, touch.y)) {
                System.out.println("BATTLE STARTING: " + player1Char + " VS " + player2Char);

                if(mode.equalsIgnoreCase("ARCADE")) {
                    // go to temporary arcade screen instead of normal battle
                    ((Main) Gdx.app.getApplicationListener())
                            .setScreen(new ArcadeBattleScreen(game, player1Name, player1Char));
                } else {
                    // normal vs
                    ((Main) Gdx.app.getApplicationListener())
                            .setScreen(new BattleScreen(player1Name, player1Char, player2Char));
                }

            }
            startPressed = false;
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
        popupBoard.dispose();
        vsLogo.dispose();
        playerSprite.dispose();
        enemySprite.dispose();
        startBtn.dispose();
        startBtnP.dispose();
    }
}