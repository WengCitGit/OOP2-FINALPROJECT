package io.github.PASAN;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;

public class CharacterSelector implements Screen {
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;
    private Texture background;

    private String username;
    private String mode;
    private Rectangle[] characters;
    private String[] characterNames = {
            "Jollibee", "McDonald", "Colonel Sanders", "Burger King",
            "Wendy", "Jack in the Box", "Little Caesar", "Chief Khai"
    };

    private Vector3 touch;
    private Texture backBtn, backBtnP;
    private Rectangle backBounds;

    private boolean backPressed = false;
    private int characterPressed = -1;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public CharacterSelector(String username, String mode) {
        this.username = username;
        this.mode = mode;

        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();

        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");
        background = new Texture("backgrounds/characterselector_background.jpg");

        touch = new Vector3();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        characters = new Rectangle[8];
        for (int i = 0; i < 8; i++) {
            int row = i / 4;
            int col = i % 4;
            characters[i] = new Rectangle(400 + col * 300, 600 - row * 350, 200, 200);
        }

        backBounds = new Rectangle(50, 50, 300, 100);
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

        boolean isTouchingBack = Gdx.input.isTouched() && backBounds.contains(touch.x, touch.y);
        if (isTouchingBack) {
            batch.draw(backBtnP, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        } else {
            batch.draw(backBtn, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        }

        font.getData().setScale(3);
        font.draw(batch, "PLAYER: " + username, 50, 1030);
        font.draw(batch, "MODE: " + mode, 50, 970);
        //font.draw(batch, "SELECT CHARACTER", 720, 930);

        font.getData().setScale(2);
        for (int i = 0; i < characters.length; i++) {
            Rectangle r = characters[i];
            GlyphLayout layout = new GlyphLayout(font, characterNames[i]);
            font.draw(batch, layout, r.x + (r.width - layout.width) / 2, r.y + r.height + 40);
        }

        batch.end();

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.WHITE);
        for (Rectangle r : characters) {
            shape.rect(r.x, r.y, r.width, r.height);
        }
        shape.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (backBounds.contains(touch.x, touch.y)) backPressed = true;
            for (int i = 0; i < characters.length; i++) {
                if (characters[i].contains(touch.x, touch.y)) characterPressed = i;
            }
        }

        if (!Gdx.input.isTouched()) {
            if (backPressed && backBounds.contains(touch.x, touch.y)) {
                ((Main) Gdx.app.getApplicationListener()).setScreen(new UsernameScreen(mode));
            }
            if (characterPressed != -1 && characters[characterPressed].contains(touch.x, touch.y)) {
                System.out.println("Selected: " + characterNames[characterPressed]);
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
        background.dispose();
        font.dispose();
        shape.dispose();
        backBtn.dispose();
        backBtnP.dispose();
    }
}