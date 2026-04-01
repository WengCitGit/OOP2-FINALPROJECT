package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.EndlessBattleScreen;
import io.github.PASAN.Main;

public class CharacterSelectorScreen implements Screen {
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

    private boolean confirmPressed  = false;
    private boolean backPressed     = false;
    private int characterPressed    = -1;

    private OrthographicCamera camera;
    private Viewport viewport;

    private int selectedCharacter = -1;

    private int playerNum;
    private String p1Name;
    private String p1Char;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public CharacterSelectorScreen(String username, String mode, int playerNum, String p1Name, String p1Char) {
        this.username  = username;
        this.mode      = mode;
        this.playerNum = playerNum;
        this.p1Name    = p1Name;
        this.p1Char    = p1Char;

        batch = new SpriteBatch();
        font  = new BitmapFont();
        shape = new ShapeRenderer();

        backBtn    = new Texture("buttons/back_button.png");
        backBtnP   = new Texture("buttons/back_button_pressed.png");
        confirmBtn = new Texture("buttons/enter_button.png");
        confirmBtnP= new Texture("buttons/enter_button_pressed.png");

        background = new Texture("backgrounds/characterselector_background.jpg");
        touch      = new Vector3();
        camera     = new OrthographicCamera();
        viewport   = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

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

        backBounds    = new Rectangle(50, 50, 300, 100);
        confirmBounds = new Rectangle(WORLD_WIDTH - 350, 50, 300, 100);
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
        batch.draw(isTouchingBack ? backBtnP : backBtn,
                backBounds.x, backBounds.y, backBounds.width, backBounds.height);

        if (selectedCharacter != -1) {
            boolean isTouchingConfirm = Gdx.input.isTouched() && confirmBounds.contains(touch.x, touch.y);
            batch.draw(isTouchingConfirm ? confirmBtnP : confirmBtn,
                    confirmBounds.x, confirmBounds.y, confirmBounds.width, confirmBounds.height);
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(3);
        font.draw(batch, "PLAYER: " + username, 50, 1030);
        font.draw(batch, "MODE: " + mode, 50, 970);

        font.getData().setScale(2);
        for (int i = 0; i < characters.length; i++) {
            Rectangle r = characters[i];
            batch.draw(characterTextures[i], r.x, r.y, r.width, r.height);
            GlyphLayout layout = new GlyphLayout(font, characterNames[i]);
            font.draw(batch, layout, r.x + (r.width - layout.width) / 2, r.y + r.height + 40);
        }

        batch.end();

        shape.setProjectionMatrix(camera.combined);
        Gdx.gl.glLineWidth(1f);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.WHITE);
        for (int i = 0; i < characters.length; i++) {
            if (i != selectedCharacter)
                shape.rect(characters[i].x, characters[i].y, characters[i].width, characters[i].height);
        }
        shape.end();

        if (selectedCharacter != -1) {
            Rectangle r = characters[selectedCharacter];
            float border = 12f;
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(Color.YELLOW);
            shape.rect(r.x - border, r.y + r.height,         r.width + border * 2, border);
            shape.rect(r.x - border, r.y - border,           r.width + border * 2, border);
            shape.rect(r.x - border, r.y - border,           border, r.height + border * 2);
            shape.rect(r.x + r.width, r.y - border,          border, r.height + border * 2);
            shape.end();
        }

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (backBounds.contains(touch.x, touch.y)) backPressed = true;
            if (selectedCharacter != -1 && confirmBounds.contains(touch.x, touch.y)) confirmPressed = true;
            for (int i = 0; i < characters.length; i++) {
                if (characters[i].contains(touch.x, touch.y)) characterPressed = i;
            }
        }

        if (!Gdx.input.isTouched()) {

            // Back button
            if (backPressed && backBounds.contains(touch.x, touch.y)) {
                if (playerNum == 2)
                    ((Main) Gdx.app.getApplicationListener()).setScreen(
                            new UsernameScreen(mode, 2, p1Name, p1Char));
                else
                    ((Main) Gdx.app.getApplicationListener()).setScreen(
                            new UsernameScreen(mode));
            }

            // Character selection highlight
            if (characterPressed != -1 && characters[characterPressed].contains(touch.x, touch.y)) {
                selectedCharacter = characterPressed;
                System.out.println("Selected: " + characterNames[selectedCharacter]);
            }

            // Confirm button — route based on mode
            if (confirmPressed && confirmBounds.contains(touch.x, touch.y) && selectedCharacter != -1) {
                String chosenCharacter = characterNames[selectedCharacter];
                Main main = (Main) Gdx.app.getApplicationListener();

                if (mode.contains("PVP") && playerNum == 1) {
                    main.setScreen(new UsernameScreen(mode, 2, username, chosenCharacter));

                } else if (mode.equalsIgnoreCase("ARCADE")) {
                    main.setScreen(new ArcadeIntroScreen(username, selectedCharacter));

                } else if (mode.equalsIgnoreCase("ENDLESS")) {
                    main.setScreen(new EndlessBattleScreen(
                            (Game) Gdx.app.getApplicationListener(),
                            username,
                            chosenCharacter));

                } else {
                    // PVP / CPU modes
                    String finalP1Name = (playerNum == 1) ? username       : p1Name;
                    String finalP2Name = (playerNum == 1) ? "CPU"          : username;
                    String finalP1Char = (playerNum == 1) ? chosenCharacter : p1Char;
                    String finalP2Char = (playerNum == 1) ? "Colonel Sanders" : chosenCharacter;

                    main.setScreen(new VSScreen(finalP1Name, finalP2Name, mode, finalP1Char, finalP2Char));
                }
            }

            backPressed      = false;
            confirmPressed   = false;
            characterPressed = -1;
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void show()   {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        font.dispose();
        shape.dispose();
        backBtn.dispose();
        backBtnP.dispose();
        confirmBtn.dispose();
        confirmBtnP.dispose();
        for (Texture t : characterTextures) {
            if (t != null) t.dispose();
        }
    }
}