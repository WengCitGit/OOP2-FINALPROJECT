package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.modes.EndlessBattleScreen;
import io.github.PASAN.Main;

public class CharacterSelectorScreen implements Screen {

    private Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;
    private Texture background;

    private String username;
    private String mode;

    private Rectangle[] characters;

    public static String[] characterNames = {
            "Jollibee", "McDonald", "Colonel Sanders", "Burger King",
            "Wendy", "Jack in the Box", "Little Caesar", "Chief Khai"
    };

    private static final String[] characterFiles = {
            "icons/Jollibee_icon.png",
            "icons/McDonald_icon.png",
            "icons/ColonelSanders_icon.png",
            "icons/BurgerKing_icon.png",
            "icons/Wendy_icon.png",
            "icons/JackInTheBox_icon.png",
            "icons/LittleCaesar_icon.png",
            "icons/ChiefKhai_icon.png"
    };

    private Texture[] characterTextures;

    private Vector3 touch;

    private Texture backBtn, backBtnP;
    private Rectangle backBounds;

    private Texture confirmBtn, confirmBtnP;
    private Rectangle confirmBounds;

    private boolean confirmPressed = false;
    private boolean backPressed = false;
    private int characterPressed = -1;

    private OrthographicCamera camera;
    private Viewport viewport;

    private int selectedCharacter = -1;

    private int playerNum;
    private String p1Name;
    private String p1Char;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public CharacterSelectorScreen(Game game, String username, String mode,
                                   int playerNum, String p1Name, String p1Char) {

        this.game = game;
        this.username = username;
        this.mode = (mode == null || mode.isEmpty()) ? "DEFAULT" : mode;

        this.playerNum = playerNum;
        this.p1Name = p1Name;
        this.p1Char = p1Char;

        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();

        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        confirmBtn = new Texture("buttons/enter_button.png");
        confirmBtnP = new Texture("buttons/enter_button_pressed.png");

        background = new Texture("backgrounds/characterselector_background.jpg");

        touch = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);

        characterTextures = new Texture[characterFiles.length];
        for (int i = 0; i < characterFiles.length; i++) {
            characterTextures[i] = new Texture(characterFiles[i]);
        }

        characters = new Rectangle[8];
        for (int i = 0; i < 8; i++) {
            int row = i / 4;
            int col = i % 4;
            characters[i] = new Rectangle(400 + col * 300, 600 - row * 300, 200, 200);
        }

        backBounds = new Rectangle(50, 50, 300, 100);
        confirmBounds = new Rectangle(WORLD_WIDTH - 350, 50, 300, 100);
    }

    @Override
    public void render(float delta) {

        camera.update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        boolean touchBack = Gdx.input.isTouched() && backBounds.contains(touch.x, touch.y);
        batch.draw(touchBack ? backBtnP : backBtn,
                backBounds.x, backBounds.y, backBounds.width, backBounds.height);

        boolean touchConfirm = selectedCharacter != -1 &&
                Gdx.input.isTouched() &&
                confirmBounds.contains(touch.x, touch.y);

        batch.draw(touchConfirm ? confirmBtnP : confirmBtn,
                confirmBounds.x, confirmBounds.y, confirmBounds.width, confirmBounds.height);

        font.setColor(Color.WHITE);
        font.getData().setScale(3);
        font.draw(batch, "PLAYER: " + username, 50, 1030);
        font.draw(batch, "MODE: " + mode, 50, 970);

        font.getData().setScale(2);

        for (int i = 0; i < characters.length; i++) {

            Rectangle r = characters[i];

            boolean locked = (playerNum == 2 && characterNames[i].equals(p1Char));

            batch.setColor(locked ? new Color(0.2f, 0.2f, 0.2f, 1f) : Color.WHITE);
            batch.draw(characterTextures[i], r.x, r.y, r.width, r.height);
            batch.setColor(Color.WHITE);

            GlyphLayout layout = new GlyphLayout(font, characterNames[i]);

            font.setColor(locked ? Color.GRAY : Color.WHITE);
            font.draw(batch, layout,
                    r.x + (r.width - layout.width) / 2f,
                    r.y + r.height + 40);
        }

        batch.end();

        drawBorders();
        handleInput();
    }

    private void drawBorders() {

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);

        for (int i = 0; i < characters.length; i++) {

            boolean locked = (playerNum == 2 && characterNames[i].equals(p1Char));

            if (locked) {
                shape.setColor(Color.DARK_GRAY);
                shape.rect(characters[i].x, characters[i].y,
                        characters[i].width, characters[i].height);
            } else if (i == selectedCharacter) {
                shape.setColor(Color.WHITE);
                shape.rect(characters[i].x, characters[i].y,
                        characters[i].width, characters[i].height);
            }
        }

        shape.end();
    }

    private void handleInput() {

        if (Gdx.input.justTouched()) {

            if (backBounds.contains(touch.x, touch.y)) {
                backPressed = true;
                Main.clickSound.play();
            }

            if (selectedCharacter != -1 &&
                    confirmBounds.contains(touch.x, touch.y)) {
                confirmPressed = true;
                Main.clickSound.play();
            }

            for (int i = 0; i < characters.length; i++) {
                if (characters[i].contains(touch.x, touch.y)) {
                    if (!(playerNum == 2 && characterNames[i].equals(p1Char))) {
                        characterPressed = i;
                        Main.clickSound.play();
                    }
                }
            }
        }

        if (!Gdx.input.isTouched()) {

            if (backPressed) {

                if (playerNum == 2) {
                    game.setScreen(new UsernameScreen(game, mode, 2, p1Name, p1Char));
                } else {
                    game.setScreen(new UsernameScreen(game, mode));
                }

                resetInput();
                return;
            }

            if (characterPressed != -1 &&
                    characters[characterPressed].contains(touch.x, touch.y)) {
                selectedCharacter = characterPressed;
            }

            if (confirmPressed &&
                    selectedCharacter != -1 &&
                    confirmBounds.contains(touch.x, touch.y)) {

                String chosenCharacter = characterNames[selectedCharacter];

                if (mode.contains("PVP") && playerNum == 1) {
                    game.setScreen(new UsernameScreen(game, mode, 2, username, chosenCharacter));

                } else if (mode.equalsIgnoreCase("ARCADE")) {
                    game.setScreen(new ArcadeIntroScreen(username, selectedCharacter));

                } else if (mode.equalsIgnoreCase("ENDLESS")) {
                    game.setScreen(new EndlessBattleScreen(game, username, chosenCharacter));

                } else {
                    String p1 = (playerNum == 1) ? username : p1Name;
                    String p2 = (playerNum == 1) ? "CPU" : username;
                    String c1 = (playerNum == 1) ? chosenCharacter : p1Char;
                    String c2 = (playerNum == 1) ? "Colonel Sanders" : chosenCharacter;

                    game.setScreen(new VSScreen(p1, p2, mode, c1, c2));
                }
            }

            resetInput();
        }
    }

    private void resetInput() {
        backPressed = false;
        confirmPressed = false;
        characterPressed = -1;
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
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
        confirmBtn.dispose();
        confirmBtnP.dispose();

        for (Texture t : characterTextures) {
            if (t != null) t.dispose();
        }
    }
}