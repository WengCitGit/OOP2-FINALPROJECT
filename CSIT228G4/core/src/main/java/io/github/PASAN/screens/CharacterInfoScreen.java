package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;

public class CharacterInfoScreen implements Screen {

    private Game game;

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;

    private Texture background;

    private Rectangle[] characters;

    private String[] characterNames = {
            "Jollibee", "McDonald", "Colonel Sanders", "Burger King",
            "Wendy", "Jack in the Box", "Little Caesar", "Chief Khai"
    };

    private String[] characterDescriptions = {
            "A joyful fighter fueled by happiness and fried chicken.",
            "A fast and aggressive fighter with combo attacks.",
            "A tactical strategist with powerful chicken strikes.",
            "A flame master with heavy grilled attacks.",
            "A balanced fighter with clever tricks.",
            "A chaotic fighter using surprise attacks.",
            "A speedy pizza warrior with spinning moves.",
            "The legendary leader of the food empire."
    };

    private Vector3 touch;

    private Texture backBtn, backBtnP;
    private Rectangle backBounds;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private int selectedCharacter = -1;
    private int characterPressed = -1;
    private boolean backPressed = false;

    public CharacterInfoScreen(Game game) {

        this.game = game;

        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();
        touch = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        background = new Texture("backgrounds/characterselector_background.jpg");

        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        backBounds = new Rectangle(50, 50, 300, 100);

        characters = new Rectangle[8];

        for (int i = 0; i < 8; i++) {

            int row = i / 4;
            int col = i % 4;

            characters[i] = new Rectangle(
                    350 + col * 300,
                    650 - row * 300,
                    200,
                    200
            );
        }
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

        // Back button
        if (Gdx.input.isTouched() && backBounds.contains(touch.x, touch.y))
            batch.draw(backBtnP, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        else
            batch.draw(backBtn, backBounds.x, backBounds.y, backBounds.width, backBounds.height);

        font.getData().setScale(2);

        // Draw character names
        for (int i = 0; i < characters.length; i++) {

            Rectangle r = characters[i];

            GlyphLayout layout = new GlyphLayout(font, characterNames[i]);

            font.draw(batch,
                    layout,
                    r.x + (r.width - layout.width) / 2,
                    r.y + r.height + 40
            );
        }

        // Draw preview panel
        if (selectedCharacter != -1) {

            font.getData().setScale(3);

            font.draw(batch,
                    "Character Info",
                    1300,
                    850
            );

            font.getData().setScale(2);

            font.draw(batch,
                    "Name: " + characterNames[selectedCharacter],
                    1300,
                    780
            );

            font.draw(batch,
                    characterDescriptions[selectedCharacter],
                    1300,
                    730
            );
        }

        batch.end();

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);

        for (int i = 0; i < characters.length; i++) {

            if (i == selectedCharacter)
                shape.setColor(Color.YELLOW);
            else
                shape.setColor(Color.WHITE);

            shape.rect(
                    characters[i].x,
                    characters[i].y,
                    characters[i].width,
                    characters[i].height
            );
        }

        shape.end();

        handleInput();
    }

    private void handleInput() {

        if (Gdx.input.justTouched()) {

            if (backBounds.contains(touch.x, touch.y))
                backPressed = true;

            for (int i = 0; i < characters.length; i++) {

                if (characters[i].contains(touch.x, touch.y))
                    characterPressed = i;
            }
        }

        if (!Gdx.input.isTouched()) {

            if (backPressed && backBounds.contains(touch.x, touch.y)) {

                game.setScreen(new GameLoreScreen(game));
            }

            if (characterPressed != -1 &&
                    characters[characterPressed].contains(touch.x, touch.y)) {

                selectedCharacter = characterPressed;
            }

            backPressed = false;
            characterPressed = -1;
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
        font.dispose();
        shape.dispose();
        background.dispose();
        backBtn.dispose();
        backBtnP.dispose();
    }
}

