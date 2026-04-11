package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;
import io.github.PASAN.MainMenu;

public class DevSelectorScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;
    private Texture background;

    private Rectangle[] developerBoxes;

    private static final String[] developerNames = {
            "Rothesa Arcena",
            "Kishanta Siton",
            "Wengie Arriola",
            "Ayella Pausal",
            "Kunihiko Nakashima"
    };

    private static final String[] developerSprites = {
            "icons/devRothesa_icon.png",
            "icons/devKishanta_icon.png",
            "icons/devWengie_icon.png",
            "icons/devAyella_icon.png",
            "icons/devKunihiko_icon.png"
    };

    private Texture[] developerTextures;

    private Vector3 touch;
    private Texture backBtn, backBtnP;
    private Rectangle backBounds;

    private boolean backPressed = false;
    private int developerPressed = -1;
    private int selectedDeveloper = -1;

    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public DevSelectorScreen() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();

        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        background = new Texture("backgrounds/devSelector_background.jpg");
        touch = new Vector3();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        // Load developer textures
        developerTextures = new Texture[developerSprites.length];
        for (int i = 0; i < developerSprites.length; i++) {
            try {
                developerTextures[i] = new Texture(developerSprites[i]);
                System.out.println("Loaded: " + developerSprites[i]);
            } catch (Exception e) {
                System.out.println("FAILED to load: " + developerSprites[i]);
                developerTextures[i] = createPlaceholder(Color.GRAY);
            }
        }

        // Create 5 developer boxes
        developerBoxes = new Rectangle[5];

        float boxWidth = 200;
        float boxHeight = 200;
        float spacing = 120;

        float topRowTotalWidth = (boxWidth * 2) + spacing;
        float topStartX = (WORLD_WIDTH - topRowTotalWidth) / 2;
        float topY = 560;
        developerBoxes[0] = new Rectangle(topStartX, topY, boxWidth, boxHeight);
        developerBoxes[1] = new Rectangle(topStartX + boxWidth + spacing, topY, boxWidth, boxHeight);

        float bottomRowTotalWidth = (boxWidth * 3) + (spacing * 2);
        float bottomStartX = (WORLD_WIDTH - bottomRowTotalWidth) / 2;
        float bottomY = 300;
        developerBoxes[2] = new Rectangle(bottomStartX, bottomY, boxWidth, boxHeight);
        developerBoxes[3] = new Rectangle(bottomStartX + boxWidth + spacing, bottomY, boxWidth, boxHeight);
        developerBoxes[4] = new Rectangle(bottomStartX + (boxWidth + spacing) * 2, bottomY, boxWidth, boxHeight);

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
        batch.draw(isTouchingBack ? backBtnP : backBtn,
                backBounds.x, backBounds.y, backBounds.width, backBounds.height);


        for (int i = 0; i < developerBoxes.length; i++) {
            Rectangle r = developerBoxes[i];
            batch.draw(developerTextures[i], r.x, r.y, r.width, r.height);

            font.setColor(Color.RED);
            font.getData().setScale(2.0f);
            GlyphLayout nameLayout = new GlyphLayout(font, developerNames[i]);
            font.draw(batch, nameLayout, r.x + (r.width - nameLayout.width) / 2, r.y + r.height + 35);
        }

        batch.end();

        Gdx.gl.glLineWidth(3f);
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.WHITE);

        for (Rectangle r : developerBoxes) {
            shape.rect(r.x, r.y, r.width, r.height);
        }

        shape.end();

        if (selectedDeveloper != -1) {
            Rectangle r = developerBoxes[selectedDeveloper];
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(Color.YELLOW);
            float t = 8f;
            shape.rect(r.x - t,       r.y - t,        r.width + t * 2, t);
            shape.rect(r.x - t,       r.y + r.height, r.width + t * 2, t);
            shape.rect(r.x - t,       r.y,             t,              r.height);
            shape.rect(r.x + r.width, r.y,             t,              r.height);
            shape.end();
        }

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (backBounds.contains(touch.x, touch.y)) {
                backPressed = true;
                Main.clickSound.play();
            }

            for (int i = 0; i < developerBoxes.length; i++) {
                if (developerBoxes[i].contains(touch.x, touch.y)) {
                    developerPressed = i;
                    selectedDeveloper = i;
                    Main.clickSound.play();
                }
            }
        }

        if (!Gdx.input.isTouched()) {
            if (backPressed && backBounds.contains(touch.x, touch.y)) {
                ((Main) Gdx.app.getApplicationListener()).setScreen(new MainMenu((Game) Gdx.app.getApplicationListener()));
            }

            if (developerPressed != -1 && developerBoxes[developerPressed].contains(touch.x, touch.y)) {
                // Pass the selected developer index to DeveloperCreditsScreen
                ((Main) Gdx.app.getApplicationListener()).setScreen(
                        new DeveloperCreditsScreen((Game) Gdx.app.getApplicationListener(), developerPressed)
                );
            }

            backPressed = false;
            developerPressed = -1;
        }
    }

    private Texture createPlaceholder(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        font.dispose();
        shape.dispose();
        backBtn.dispose();
        backBtnP.dispose();
        for (Texture t : developerTextures) {
            if (t != null) t.dispose();
        }
    }
}