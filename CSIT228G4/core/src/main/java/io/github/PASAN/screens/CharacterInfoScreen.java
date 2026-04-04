package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;

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

    private static final String[] characterKeys = {
            "Jollibee", "McDonald", "Colonel", "BurgerKing",
            "Wendy", "Jack", "LittleCaesar", "ChiefKhai"
    };

    private static final String[] tabKeys = { "stats", "who", "where", "why" };

    private Texture[][] tabImages;        // [characterIndex][tabIndex] — null if file missing
    private boolean[]   tabImagesLoaded; // true once a character's tab images have been loaded
    private Texture     placeholderTex;  // shown when a tab image is missing

    // Tab button PNG textures (normal + pressed), indexed 0=STATS, 1=WHO, 2=WHY, 3=WHERE
    private Texture[] tabBtn  = new Texture[4];
    private Texture[] tabBtnP = new Texture[4];

    private static final String[] tabBtnFiles = {
            "buttons/stats_button.png",
            "buttons/who_button.png",
            "buttons/where_button.png",
            "buttons/why_button.png"
    };

    private static final String[] tabBtnPressedFiles = {
            "buttons/stats_button_pressed.png",
            "buttons/who_button_pressed.png",
            "buttons/where_button_pressed.png",
            "buttons/why_button_pressed.png"
    };

    // Close button textures
    private Texture closeBtn;
    private Texture closeBtnP;

    private Vector3 touch;

    private Texture backBtn, backBtnP;
    private Rectangle backBounds;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private int selectedCharacter = -1;
    private int characterPressed  = -1;
    private boolean backPressed   = false;

    // Panel state
    private boolean showPanel = false;
    private int activeTab     = 0;   // 0=STATS, 1=WHO, 2=WHY, 3=WHERE
    private boolean closeHeld = false;
    private int tabPressed    = -1;

    // Panel position constants (used to anchor tabs and close button)
    private static final float PANEL_X = 410;
    private static final float PANEL_Y = 120;
    private static final float PANEL_W = 1520;
    private static final float PANEL_H = 780;

    private Rectangle[] tabBounds = new Rectangle[4];
    private Rectangle   closeBounds;

    public CharacterInfoScreen(Game game) {

        this.game = game;

        batch = new SpriteBatch();
        font  = new BitmapFont();
        shape = new ShapeRenderer();
        touch = new Vector3();

        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        background = new Texture("backgrounds/characterselector_background.jpg");
        backBtn    = new Texture("buttons/back_button.png");
        backBtnP   = new Texture("buttons/back_button_pressed.png");
        backBounds = new Rectangle(50, 50, 300, 100);

        // Tab button PNGs
        for (int i = 0; i < 4; i++) {
            try {
                tabBtn[i] = new Texture(tabBtnFiles[i]);
            } catch (Exception e) {
                Gdx.app.error("CharacterInfoScreen", "Missing tab button: " + tabBtnFiles[i], e);
                tabBtn[i] = createPlaceholder(Color.DARK_GRAY);
            }
            try {
                tabBtnP[i] = new Texture(tabBtnPressedFiles[i]);
            } catch (Exception e) {
                Gdx.app.error("CharacterInfoScreen", "Missing tab button pressed: " + tabBtnPressedFiles[i], e);
                tabBtnP[i] = tabBtn[i]; // fall back to normal if pressed is missing
            }
        }

        // Close button PNG (with pressed variant)
        closeBtn  = new Texture("buttons/close_button.png");
        closeBtnP = new Texture("buttons/close_button_pressed.png");

        // Character icon textures
        characterTextures = new Texture[characterFiles.length];
        for (int i = 0; i < characterFiles.length; i++) {
            try {
                characterTextures[i] = new Texture(characterFiles[i]);
            } catch (Exception e) {
                Gdx.app.error("CharacterInfoScreen", "Missing character icon: " + characterFiles[i], e);
                characterTextures[i] = createPlaceholder(Color.GRAY);
            }
        }

        // Magenta placeholder for missing tab images
        placeholderTex = createPlaceholder(Color.MAGENTA);

        // Tab photo textures — lazily loaded when a character is first selected
        // to avoid blocking the audio thread on startup
        tabImages       = new Texture[characterKeys.length][tabKeys.length];
        tabImagesLoaded = new boolean[characterKeys.length];

        // Character selector grid
        characters = new Rectangle[8];
        for (int i = 0; i < 8; i++) {
            int row = i / 4;
            int col = i % 4;
            characters[i] = new Rectangle(380 + col * 300, 600 - row * 300, 200, 200);
        }

        // Tab button rects — four tabs spaced along top of panel
        float tabW = 290, tabH = 85, tabSpacing = 30;
        float tabStartX = PANEL_X + -10;
        float tabTopY   = PANEL_Y + PANEL_H + 20;
        for (int i = 0; i < 4; i++) {
            tabBounds[i] = new Rectangle(tabStartX + i * (tabW + tabSpacing), tabTopY, tabW, tabH);
        }

        // Close button bounds
        closeBounds = new Rectangle(PANEL_X + PANEL_W - 150, PANEL_Y + PANEL_H - 10, 90, 85);
    }

    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Loads all 4 tab images for the given character index on demand.
     * Called the first time that character is selected so the cost is
     * spread out and never causes an audio stutter on the select screen.
     */
    private void loadTabImagesFor(int characterIndex) {
        if (tabImagesLoaded[characterIndex]) return;
        for (int t = 0; t < tabKeys.length; t++) {
            String path = "info/" + characterKeys[characterIndex] + "_" + tabKeys[t] + ".png";
            try {
                tabImages[characterIndex][t] = new Texture(path);
            } catch (Exception e) {
                Gdx.app.error("CharacterInfoScreen", "Missing tab image: " + path, e);
                tabImages[characterIndex][t] = null;
            }
        }
        tabImagesLoaded[characterIndex] = true;
    }

    // ──────────────────────────────────────────────────────────────────────────
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

        // Back button (hidden while panel is open)
        if (!showPanel) {
            if (Gdx.input.isTouched() && backBounds.contains(touch.x, touch.y))
                batch.draw(backBtnP, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
            else
                batch.draw(backBtn,  backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        }

        // Character icon grid + names
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
        for (int i = 0; i < characters.length; i++) {
            Rectangle r = characters[i];
            batch.draw(characterTextures[i], r.x, r.y, r.width, r.height);
            GlyphLayout gl = new GlyphLayout(font, characterNames[i]);
            font.draw(batch, gl,
                    r.x + (r.width  - gl.width) / 2,
                    r.y + r.height + 40);
        }

        batch.end();

        // Selection highlight border
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.WHITE);
        for (int i = 0; i < characters.length; i++) {
            if (i != selectedCharacter)
                shape.rect(characters[i].x, characters[i].y, characters[i].width, characters[i].height);
        }
        shape.end();

        if (selectedCharacter != -1) {
            float t = 16f;
            Rectangle r = characters[selectedCharacter];
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(Color.YELLOW);
            shape.rect(r.x - t,       r.y - t,        r.width + t * 2, t);
            shape.rect(r.x - t,       r.y + r.height, r.width + t * 2, t);
            shape.rect(r.x - t,       r.y,             t,              r.height);
            shape.rect(r.x + r.width, r.y,             t,              r.height);
            shape.end();
        }

        if (showPanel && selectedCharacter != -1) {
            drawPanel();
        }

        handleInput();
    }

    // ──────────────────────────────────────────────────────────────────────────
    private void drawPanel() {

        int idx = selectedCharacter;

        // 1. Semi-transparent screen dim
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.55f);
        shape.rect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 2. Draw the tab PNG — it IS the full panel body
        Texture photo = tabImages[idx][activeTab];
        if (photo == null) photo = placeholderTex;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(photo, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // 3. Draw tab button PNGs on top
        // Active tab always shows pressed texture; inactive tabs show normal texture
        for (int i = 0; i < 4; i++) {
            Rectangle tb = tabBounds[i];
            Texture tex = (i == activeTab) ? tabBtnP[i] : tabBtn[i];
            batch.draw(tex, tb.x, tb.y, tb.width, tb.height);
        }

        // 4. Draw close button PNG on top
        boolean isClosePressed = Gdx.input.isTouched() && closeBounds.contains(touch.x, touch.y);
        Texture closeTex = isClosePressed ? closeBtnP : closeBtn;
        batch.draw(closeTex,
                closeBounds.x, closeBounds.y,
                closeBounds.width, closeBounds.height);

        // 5. "Image Missing" label if the texture was not loaded
        if (tabImages[idx][activeTab] == null) {
            font.getData().setScale(3f);
            font.setColor(Color.WHITE);
            String msg = "Image Missing: info/" + characterKeys[idx] + "_" + tabKeys[activeTab] + ".png";
            GlyphLayout gl = new GlyphLayout(font, msg);
            font.draw(batch, gl,
                    (WORLD_WIDTH  - gl.width)  / 2f,
                    (WORLD_HEIGHT + gl.height) / 2f);
        }

        batch.end();
    }

    // ──────────────────────────────────────────────────────────────────────────
    private void handleInput()
    {
        if (Gdx.input.justTouched())
        {
            if (!showPanel)
            {
                if (backBounds.contains(touch.x, touch.y))
                {
                    backPressed = true;
                    Main.clickSound.play();
                }
                for (int i = 0; i < characters.length; i++)
                {
                    if (characters[i].contains(touch.x, touch.y))
                    {
                        characterPressed = i;
                        Main.clickSound.play();
                    }
                }

            }
            else
            {
                for (int i = 0; i < 4; i++)
                {
                    if (tabBounds[i].contains(touch.x, touch.y))
                    {
                        tabPressed = i;
                        Main.clickSound.play();
                    }
                }
                if (closeBounds.contains(touch.x, touch.y))
                {
                    closeHeld = true;
                    Main.clickSound.play();
                }
            }
        }

        if (!Gdx.input.isTouched()) {

            if (!showPanel) {
                if (backPressed && backBounds.contains(touch.x, touch.y))
                    game.setScreen(new GameLoreScreen(game));

                if (characterPressed != -1 && characters[characterPressed].contains(touch.x, touch.y)) {
                    selectedCharacter = characterPressed;
                    loadTabImagesFor(selectedCharacter); // load only this character's 4 images
                    showPanel = true;
                    activeTab = 0;
                }

            } else {
                // Confirm tab switch on release
                if (tabPressed != -1 && tabBounds[tabPressed].contains(touch.x, touch.y))
                    activeTab = tabPressed;

                // Confirm close on release
                if (closeHeld && closeBounds.contains(touch.x, touch.y))
                    showPanel = false;

                tabPressed = -1;
                closeHeld  = false;
            }

            backPressed      = false;
            characterPressed = -1;
        }
    }

    /** Creates a 1×1 solid-colour fallback texture. */
    private Texture createPlaceholder(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    // ──────────────────────────────────────────────────────────────────────────
    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void show()   {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shape.dispose();
        background.dispose();
        backBtn.dispose();
        backBtnP.dispose();
        closeBtn.dispose();
        closeBtnP.dispose();
        placeholderTex.dispose();
        for (int i = 0; i < 4; i++) {
            if (tabBtn[i]  != null) tabBtn[i].dispose();
            if (tabBtnP[i] != null && tabBtnP[i] != tabBtn[i]) tabBtnP[i].dispose();
        }
        for (Texture t : characterTextures) if (t != null) t.dispose();
        for (Texture[] row : tabImages)
            for (Texture t : row) if (t != null) t.dispose();
    }
}