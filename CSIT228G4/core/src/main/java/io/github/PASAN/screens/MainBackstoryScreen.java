package io.github.PASAN.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;
import io.github.PASAN.Main;

public class MainBackstoryScreen implements Screen {

    private Game game;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private static final String[] PAGE_FILES = {
            "backstory/page_1.jpg",
            "backstory/page_2.jpg"
    };

    private Texture[]  pages;         // loaded lazily, null until first needed
    private boolean[]  pageLoaded;    // tracks which pages have been loaded
    private Texture    placeholderTex;

    private int currentPage = 0; // 0-based index

    // ── Button textures ───────────────────────────────────────────────────────
    private Texture nextBtn,  nextBtnP;
    private Texture backBtn,  backBtnP;
    private Texture closeBtn, closeBtnP;

    // ── Hit-boxes ─────────────────────────────────────────────────────────────
    // Measured from the 1280×720 screenshot, scaled ×1.5 for 1920×1080.
    // LibGDX Y is bottom-up: world_y = WORLD_HEIGHT - (screenshot_y_top * 1.5)
    //
    //  X button  : screenshot 1192–1255, y 25–88   → world x=1788 w=95  y=1017-95=922...
    //              simpler: top-right corner, fixed offset from screen edge
    //  NEXT btn  : screenshot  980–1200, y 618–668  → world x=1470 w=330 y=63  h=75
    //  BACK btn  : screenshot   80– 300, y 618–668  → world x=120  w=330 y=63  h=75

    private static final float CLOSE_X = 1700f;
    private static final float CLOSE_Y =  940f;
    private static final float CLOSE_W =  90f;
    private static final float CLOSE_H =  85f;

    private static final float NEXT_X  = 1420f;
    private static final float NEXT_Y  =  125f;
    private static final float NEXT_W  =  290f;
    private static final float NEXT_H  =   90f;

    private static final float BACK_X  =  220f;
    private static final float BACK_Y  =  125f;
    private static final float BACK_W  =  290f;
    private static final float BACK_H  =   90f;

    private Rectangle closeBounds;
    private Rectangle nextBounds;
    private Rectangle backBounds;

    // ── Input state ───────────────────────────────────────────────────────────
    private Vector3 touch = new Vector3();
    private boolean closeHeld = false;
    private boolean nextHeld  = false;
    private boolean backHeld  = false;

    // ─────────────────────────────────────────────────────────────────────────
    public MainBackstoryScreen(Game game) {
        this.game = game;

        batch    = new SpriteBatch();
        camera   = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        placeholderTex = createPlaceholder(Color.MAGENTA);

        // Lazy page array — pages are loaded on demand
        pages      = new Texture[PAGE_FILES.length];
        pageLoaded = new boolean[PAGE_FILES.length];

        // Load the first page immediately so there's no blank flash on open
        loadPage(0);

        // Button textures
        try { nextBtn   = new Texture("buttons/next_button.png");          } catch (Exception e) { nextBtn   = createPlaceholder(Color.GREEN);  }
        try { nextBtnP  = new Texture("buttons/next_button_pressed.png");   } catch (Exception e) { nextBtnP  = nextBtn; }
        try { backBtn   = new Texture("buttons/prev_button.png");           } catch (Exception e) { backBtn   = createPlaceholder(Color.YELLOW); }
        try { backBtnP  = new Texture("buttons/prev_button_pressed.png");   } catch (Exception e) { backBtnP  = backBtn; }
        try { closeBtn  = new Texture("buttons/close_button.png");          } catch (Exception e) { closeBtn  = createPlaceholder(Color.RED);    }
        try { closeBtnP = new Texture("buttons/close_button_pressed.png");  } catch (Exception e) { closeBtnP = closeBtn; }

        // Hit-boxes
        closeBounds = new Rectangle(CLOSE_X, CLOSE_Y, CLOSE_W, CLOSE_H);
        nextBounds  = new Rectangle(NEXT_X,  NEXT_Y,  NEXT_W,  NEXT_H);
        backBounds  = new Rectangle(BACK_X,  BACK_Y,  BACK_W,  BACK_H);
    }

    // ── Lazy page loader ──────────────────────────────────────────────────────
    private void loadPage(int index) {
        if (index < 0 || index >= pages.length) return;
        if (pageLoaded[index]) return;
        try {
            pages[index] = new Texture(PAGE_FILES[index]);
        } catch (Exception e) {
            Gdx.app.error("MainBackstoryScreen", "Missing page: " + PAGE_FILES[index], e);
            pages[index] = null;
        }
        pageLoaded[index] = true;
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touch);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Current page PNG (fullscreen)
        Texture page = pages[currentPage];
        batch.draw(page != null ? page : placeholderTex, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        // "Image Missing" label
        if (page == null) {
            // nothing extra needed — magenta placeholder is obvious enough
        }

        // Close button (always visible)
        boolean closeDown = Gdx.input.isTouched() && closeBounds.contains(touch.x, touch.y);
        batch.draw(closeDown ? closeBtnP : closeBtn,
                CLOSE_X, CLOSE_Y, CLOSE_W, CLOSE_H);

        // NEXT button — only if not on last page
        if (currentPage < pages.length - 1) {
            boolean nextDown = Gdx.input.isTouched() && nextBounds.contains(touch.x, touch.y);
            batch.draw(nextDown ? nextBtnP : nextBtn,
                    NEXT_X, NEXT_Y, NEXT_W, NEXT_H);
        }

        // BACK button — only if not on first page
        if (currentPage > 0) {
            boolean backDown = Gdx.input.isTouched() && backBounds.contains(touch.x, touch.y);
            batch.draw(backDown ? backBtnP : backBtn,
                    BACK_X, BACK_Y, BACK_W, BACK_H);
        }

        batch.end();

        handleInput();
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    private void handleInput() {

        if (Gdx.input.justTouched()) {
            if (closeBounds.contains(touch.x, touch.y)){
                closeHeld = true;
                Main.clickSound.play();
            }
            else if (nextBounds.contains(touch.x, touch.y) && currentPage < pages.length - 1){
                nextHeld = true;
                Main.clickSound.play();
            }
            else if (backBounds.contains(touch.x, touch.y) && currentPage > 0){
                backHeld  = true;
                Main.clickSound.play();
            }
        }

        if (!Gdx.input.isTouched()) {

            if (closeHeld && closeBounds.contains(touch.x, touch.y)) {
                game.setScreen(new GameLoreScreen(game));
            }

            if (nextHeld && nextBounds.contains(touch.x, touch.y)) {
                currentPage++;
                loadPage(currentPage);           // lazy-load the next page
                loadPage(currentPage + 1);       // pre-load the one after too
            }

            if (backHeld && backBounds.contains(touch.x, touch.y)) {
                currentPage--;
            }

            closeHeld = false;
            nextHeld  = false;
            backHeld  = false;
        }
    }

    /** 1×1 solid-colour fallback texture. */
    private Texture createPlaceholder(Color color) {
        com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void show()   {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        placeholderTex.dispose();
        for (Texture t : pages)      if (t != null) t.dispose();
        if (nextBtn  != null) nextBtn.dispose();
        if (nextBtnP != null && nextBtnP != nextBtn)   nextBtnP.dispose();
        if (backBtn  != null) backBtn.dispose();
        if (backBtnP != null && backBtnP != backBtn)   backBtnP.dispose();
        if (closeBtn  != null) closeBtn.dispose();
        if (closeBtnP != null && closeBtnP != closeBtn) closeBtnP.dispose();
    }
}