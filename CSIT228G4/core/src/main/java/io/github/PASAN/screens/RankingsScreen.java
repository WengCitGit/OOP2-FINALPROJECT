package io.github.PASAN.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.PASAN.Main;
import io.github.PASAN.MainMenu;
import io.github.PASAN.leaderboard.Leaderboard;
import io.github.PASAN.leaderboard.PlayerScore;

import java.util.ArrayList;

public class RankingsScreen implements Screen {

    private Game game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH  = 1920.0F;
    private static final float WORLD_HEIGHT = 1080.0F;

    // Background
    private Texture background;

    // Tab buttons (normal + pressed)
    private Texture pvpBtn,      pvpBtnPressed;
    private Texture pvcBtn,      pvcBtnPressed;
    private Texture endlessBtn,  endlessBtnPressed;
    private Texture arcadeBtn,   arcadeBtnPressed;
    private Texture backBtn,     backBtnPressed;

    // Hit-boxes
    private Rectangle pvpBounds;
    private Rectangle pvcBounds;
    private Rectangle endlessBounds;
    private Rectangle arcadeBounds;
    private Rectangle backBounds;

    // Press-state flags
    private boolean pvpPressed     = false;
    private boolean pvcPressed     = false;
    private boolean endlessPressed = false;
    private boolean arcadePressed  = false;
    private boolean backPressed    = false;

    // Which tab is currently selected
    public enum Tab { PVP, PVC, ENDLESS, ARCADE }
    private Tab activeTab = Tab.PVP;

    private Vector3 touchPoint;

    // --- FONTS ---
    private BitmapFont font;
    private BitmapFont titleFont;

    // ------------------------------------------------------------------ //
    //  Layout constants
    // ------------------------------------------------------------------ //
    private static final float BTN_W       = 400.0F;
    private static final float BTN_H       = 150.0F;
    private static final float BTN_X       = 50.0F;
    private static final float BTN_SPACING = 150.0F;
    private static final float BTN_TOP_Y   = 830.0F;

    public RankingsScreen(Game game) {
        this.game       = game;
        this.batch      = new SpriteBatch();
        this.touchPoint = new Vector3();
        this.camera     = new OrthographicCamera();
        this.viewport   = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, this.camera);
        this.viewport.apply();
        this.camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0.0F);

        this.background = new Texture("backgrounds/rankings_background.jpg");

        this.pvpBtn            = new Texture("buttons/pvp_button.png");
        this.pvpBtnPressed     = new Texture("buttons/pvp_button_pressed.png");
        this.pvcBtn            = new Texture("buttons/pvc_button.png");
        this.pvcBtnPressed     = new Texture("buttons/pvc_button_pressed.png");
        this.endlessBtn        = new Texture("buttons/endless_button.png");
        this.endlessBtnPressed = new Texture("buttons/endless_button_pressed.png");
        this.arcadeBtn         = new Texture("buttons/arcade_button.png");
        this.arcadeBtnPressed  = new Texture("buttons/arcade_button_pressed.png");
        this.backBtn           = new Texture("buttons/back_button.png");
        this.backBtnPressed    = new Texture("buttons/back_button_pressed.png");

        pvpBounds     = new Rectangle(BTN_X, BTN_TOP_Y,                   BTN_W, BTN_H);
        pvcBounds     = new Rectangle(BTN_X, BTN_TOP_Y -     BTN_SPACING, BTN_W, BTN_H);
        arcadeBounds  = new Rectangle(BTN_X, BTN_TOP_Y - 2 * BTN_SPACING, BTN_W, BTN_H);
        endlessBounds = new Rectangle(BTN_X, BTN_TOP_Y - 3 * BTN_SPACING, BTN_W, BTN_H);
        backBounds    = new Rectangle(BTN_X, BTN_TOP_Y - 4 * BTN_SPACING, BTN_W, BTN_H);

        // Setup fonts
        font = new BitmapFont();
        font.getData().setScale(3.0f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4.0f);
    }

    // ------------------------------------------------------------------ //

    @Override
    public void show() {
    }

    // ------------------------------------------------------------------ //

    @Override
    public void render(float delta) {
        this.camera.update();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        this.touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        this.viewport.unproject(this.touchPoint);

        this.batch.setProjectionMatrix(this.camera.combined);
        this.batch.begin();

        // Draw background
        this.batch.draw(this.background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);

        // Draw tab buttons
        drawTabButton(pvpBtn,     pvpBtnPressed,     pvpBounds,     activeTab == Tab.PVP);
        drawTabButton(pvcBtn,     pvcBtnPressed,     pvcBounds,     activeTab == Tab.PVC);
        drawTabButton(arcadeBtn,  arcadeBtnPressed,  arcadeBounds,  activeTab == Tab.ARCADE);
        drawTabButton(endlessBtn, endlessBtnPressed, endlessBounds, activeTab == Tab.ENDLESS);
        drawTabButton(backBtn,    backBtnPressed,    backBounds,    false);

        // Draw title
        String tabTitle = "PVP RANKINGS";
        if (activeTab == Tab.PVC) tabTitle = "PVC RANKINGS";
        else if (activeTab == Tab.ENDLESS) tabTitle = "ENDLESS RANKINGS";
        else if (activeTab == Tab.ARCADE) tabTitle = "ARCADE RANKINGS";

        titleFont.setColor(Color.BLUE);
        GlyphLayout titleLayout = new GlyphLayout(titleFont, tabTitle);
        titleFont.draw(batch, tabTitle, 1170 - titleLayout.width / 2, 863);

        // Draw leaderboard scores (compact, no scrolling)
        drawLeaderboard(activeTab);

        this.batch.end();

        handleInput();
    }

    // ------------------------------------------------------------------ //

    private void drawLeaderboard(Tab tab) {
        String fileName = "pvp_scores.txt";
        if      (tab == Tab.PVC)     fileName = "pvc_scores.txt";
        else if (tab == Tab.ENDLESS) fileName = "endless_scores.txt";
        else if (tab == Tab.ARCADE)  fileName = "arcade_scores.txt";

        Leaderboard lbManager = new Leaderboard(fileName);
        ArrayList<PlayerScore> topScores = lbManager.getTopScores();

        boolean showTime   = (tab == Tab.ARCADE || tab == Tab.ENDLESS);
        boolean showStreak = (tab == Tab.ENDLESS);

        font.setColor(Color.WHITE);
        float yPos      = 800;
        float rowHeight = 50;

        if (topScores.isEmpty()) {
            font.draw(batch, "NO SCORES RECORDED YET! BE THE FIRST!", 700, yPos);
        } else {
            // Header row
            font.setColor(Color.YELLOW);
            font.draw(batch, "PLAYER", 700,  yPos);
            font.draw(batch, "SCORE",  1200, yPos);
            if (showStreak) font.draw(batch, "STREAK", 1400, yPos);
            if (showTime)   font.draw(batch, "TIME",   1600, yPos);
            yPos -= rowHeight;

            font.setColor(Color.WHITE);
            for (int i = 0; i < topScores.size() && yPos > 200; i++) {
                PlayerScore ps = topScores.get(i);

                font.draw(batch, (i + 1) + ". " + ps.getPlayer(), 700,  yPos);
                font.draw(batch, String.valueOf(ps.getScore()),    1200, yPos);

                if (showStreak) {
                    font.draw(batch, "x" + ps.getStreak(), 1400, yPos);
                }

                if (showTime) {
                    long   secs     = ps.getTimeSeconds();
                    String timeText = secs > 0
                            ? String.format("%d:%02d", secs / 60, secs % 60)
                            : "--:--";
                    font.draw(batch, timeText, 1600, yPos);
                }

                yPos -= rowHeight;
            }
        }
    }

    // ------------------------------------------------------------------ //

    private void drawTabButton(Texture normal, Texture pressed, Rectangle bounds, boolean isActive) {
        boolean touchingNow = Gdx.input.isTouched() && bounds.contains(this.touchPoint.x, this.touchPoint.y);
        boolean usePressed = isActive || touchingNow;
        this.batch.draw(usePressed ? pressed : normal, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    // ------------------------------------------------------------------ //

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            if      (pvpBounds    .contains(touchPoint.x, touchPoint.y)) {
                pvpPressed     = true;
                Main.clickSound.play();
            }
            else if (pvcBounds    .contains(touchPoint.x, touchPoint.y)) {
                pvcPressed     = true;
                Main.clickSound.play();
            }
            else if (arcadeBounds .contains(touchPoint.x, touchPoint.y)) {
                arcadePressed  = true;
                Main.clickSound.play();
            }
            else if (endlessBounds.contains(touchPoint.x, touchPoint.y)) {
                endlessPressed = true;
                Main.clickSound.play();
            }
            else if (backBounds   .contains(touchPoint.x, touchPoint.y)) {
                backPressed    = true;
                Main.clickSound.play();
            }
        }

        if (!Gdx.input.isTouched()) {
            if      (pvpPressed     && pvpBounds    .contains(touchPoint.x, touchPoint.y)) activeTab = Tab.PVP;
            else if (pvcPressed     && pvcBounds    .contains(touchPoint.x, touchPoint.y)) activeTab = Tab.PVC;
            else if (endlessPressed && endlessBounds.contains(touchPoint.x, touchPoint.y)) activeTab = Tab.ENDLESS;
            else if (arcadePressed  && arcadeBounds .contains(touchPoint.x, touchPoint.y)) activeTab = Tab.ARCADE;
            else if (backPressed    && backBounds   .contains(touchPoint.x, touchPoint.y)) {
                game.setScreen(new MainMenu(game));
            }

            pvpPressed = pvcPressed = endlessPressed = arcadePressed = backPressed = false;
        }
    }

    // ------------------------------------------------------------------ //

    public Tab getActiveTab() { return activeTab; }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        titleFont.dispose();
        background.dispose();
        pvpBtn.dispose();      pvpBtnPressed.dispose();
        pvcBtn.dispose();      pvcBtnPressed.dispose();
        arcadeBtn.dispose();   arcadeBtnPressed.dispose();
        endlessBtn.dispose();  endlessBtnPressed.dispose();
        backBtn.dispose();     backBtnPressed.dispose();
    }
}