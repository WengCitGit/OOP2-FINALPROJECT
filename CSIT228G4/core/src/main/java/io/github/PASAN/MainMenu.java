package io.github.PASAN;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.PASAN.screens.FirstScreen;
import io.github.PASAN.screens.RankingsScreen;

public class MainMenu implements Screen {
    private Game game;
    private SpriteBatch batch;
    private Texture background;
    private Texture thankYouBg;
    private Texture playBtn;
    private Texture playBtnPressed;
    private Texture rankingsBtn;
    private Texture rankingsBtnPressed;
    private Texture creditsBtn;
    private Texture creditsBtnPressed;
    private Texture exitBtn;
    private Texture exitBtnPressed;
    private OrthographicCamera camera;
    private Viewport viewport;
    private static final float WORLD_WIDTH  = 1920.0F;
    private static final float WORLD_HEIGHT = 1080.0F;
    private Rectangle playBounds;
    private Rectangle rankingsBounds;
    private Rectangle creditsBounds;
    private Rectangle exitBounds;
    private Vector3 touchPoint;
    private boolean playPressed     = false;
    private boolean rankingsPressed = false;
    private boolean creditsPressed  = false;
    private boolean exitPressed     = false;

    // Thank you fade fields
    private boolean showingThankYou  = false;
    private float   thankYouTimer    = 0f;
    private static final float DISPLAY_TIME = 3f;
    private static final float FADE_START   = 2f;

    public MainMenu(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.touchPoint = new Vector3();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(1920.0F, 1080.0F, this.camera);
        this.viewport.apply();
        this.camera.position.set(960.0F, 540.0F, 0.0F);

        this.background         = new Texture("backgrounds/mainmenu_background.jpg");
        this.thankYouBg         = new Texture("backgrounds/thankyou_background.jpg");
        this.playBtn            = new Texture("buttons/play_button.png");
        this.playBtnPressed     = new Texture("buttons/play_button_pressed.png");
        this.rankingsBtn        = new Texture("buttons/rankings_button.png");
        this.rankingsBtnPressed = new Texture("buttons/rankings_button_pressed.png");
        this.creditsBtn         = new Texture("buttons/credits_button.png");
        this.creditsBtnPressed  = new Texture("buttons/credits_button_pressed.png");
        this.exitBtn            = new Texture("buttons/exit_button.png");
        this.exitBtnPressed     = new Texture("buttons/exit_button_pressed.png");

        float centerX = 960.0F;
        float centerY = 540.0F;
        this.playBounds     = new Rectangle(centerX - 235.0F, centerY +  50.0F, 470.0F, 130.0F);
        this.rankingsBounds = new Rectangle(centerX - 235.0F, centerY -  65.0F, 470.0F, 130.0F);
        this.creditsBounds  = new Rectangle(centerX - 235.0F, centerY - 180.0F, 470.0F, 130.0F);
        this.exitBounds     = new Rectangle(centerX - 235.0F, centerY - 295.0F, 470.0F, 130.0F);
    }

    public void render(float delta) {
        this.camera.update();
        Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        Gdx.gl.glClear(16384);

        // --- Thank You screen takes over render ---
        if (this.showingThankYou) {
            this.thankYouTimer += delta;

            float alpha = 1f;
            if (this.thankYouTimer >= FADE_START) {
                alpha = 1f - ((this.thankYouTimer - FADE_START) / (DISPLAY_TIME - FADE_START));
                alpha = Math.max(0f, alpha);
            }

            if (this.thankYouTimer >= DISPLAY_TIME) {
                Gdx.app.exit();
            }

            this.batch.setProjectionMatrix(this.camera.combined);
            this.batch.begin();
            this.batch.setColor(1f, 1f, 1f, alpha);
            this.batch.draw(this.thankYouBg, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
            this.batch.setColor(1f, 1f, 1f, 1f); // reset color
            this.batch.end();
            return; // skip normal menu render
        }

        // --- Normal menu render ---
        this.touchPoint.set((float) Gdx.input.getX(), (float) Gdx.input.getY(), 0.0F);
        this.viewport.unproject(this.touchPoint);
        this.batch.setProjectionMatrix(this.camera.combined);
        this.batch.begin();
        this.batch.draw(this.background, 0.0F, 0.0F, 1920.0F, 1080.0F);
        this.drawButton(this.playBtn,     this.playBtnPressed,     this.playBounds);
        this.drawButton(this.rankingsBtn, this.rankingsBtnPressed, this.rankingsBounds);
        this.drawButton(this.creditsBtn,  this.creditsBtnPressed,  this.creditsBounds);
        this.drawButton(this.exitBtn,     this.exitBtnPressed,     this.exitBounds);
        this.batch.end();
        this.handleInput();
    }

    private void drawButton(Texture normal, Texture pressed, Rectangle bounds) {
        if (Gdx.input.isTouched() && bounds.contains(this.touchPoint.x, this.touchPoint.y)) {
            this.batch.draw(pressed, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            this.batch.draw(normal, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if (this.playBounds.contains(this.touchPoint.x, this.touchPoint.y)) {
                this.playPressed = true;
            } else if (this.rankingsBounds.contains(this.touchPoint.x, this.touchPoint.y)) {
                this.rankingsPressed = true;
            } else if (this.creditsBounds.contains(this.touchPoint.x, this.touchPoint.y)) {
                this.creditsPressed = true;
            } else if (this.exitBounds.contains(this.touchPoint.x, this.touchPoint.y)) {
                this.exitPressed = true;
            }
        }

        if (!Gdx.input.isTouched()) {
            if (this.playPressed && this.playBounds.contains(this.touchPoint.x, this.touchPoint.y)) {
                this.game.setScreen(new FirstScreen(this.game));
            } else if (this.rankingsPressed && this.rankingsBounds.contains(this.touchPoint.x, this.touchPoint.y)) {
                this.game.setScreen(new RankingsScreen(this.game));
            } else if ((!this.creditsPressed || !this.creditsBounds.contains(this.touchPoint.x, this.touchPoint.y))
                    && this.exitPressed && this.exitBounds.contains(this.touchPoint.x, this.touchPoint.y)) {
                this.showingThankYou = true; // trigger the fade
            }

            this.playPressed     = false;
            this.rankingsPressed = false;
            this.creditsPressed  = false;
            this.exitPressed     = false;
        }
    }

    public void resize(int width, int height) {
        this.viewport.update(width, height);
    }

    public void show()   {}
    public void pause()  {}
    public void resume() {}
    public void hide()   {}

    public void dispose() {
        this.batch.dispose();
        this.background.dispose();
        this.thankYouBg.dispose();
        this.playBtn.dispose();
        this.playBtnPressed.dispose();
        this.rankingsBtn.dispose();
        this.rankingsBtnPressed.dispose();
        this.creditsBtn.dispose();
        this.creditsBtnPressed.dispose();
        this.exitBtn.dispose();
        this.exitBtnPressed.dispose();
    }
}