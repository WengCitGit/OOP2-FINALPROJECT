package io.github.PASAN;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;

public class UsernameScreen implements Screen
{

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;

    private String username = "";
    private String gameMode;

    private Rectangle enterBounds;
    private Rectangle cancelBounds;
    private Rectangle textFieldBounds;
    private Rectangle backBounds;

    private Vector3 touchPoint;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture backBtn, backBtnP;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    private float cursorTimer = 0;
    private boolean showCursor = true;
    private boolean backPressed = false;

    public UsernameScreen(String mode)
    {

        this.gameMode = mode;

        batch = new SpriteBatch();
        font = new BitmapFont();
        shape = new ShapeRenderer();

        backBtn = new Texture("buttons/back_button.png");
        backBtnP = new Texture("buttons/back_button_pressed.png");

        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

        float centerX = WORLD_WIDTH / 2f;

        textFieldBounds = new Rectangle(centerX - 300, 560, 600, 80);

        enterBounds = new Rectangle(centerX + 40, 350, 220, 100);
        cancelBounds = new Rectangle(centerX - 260, 350, 220, 100);

        backBounds = new Rectangle(centerX - 260, 350, 220, 100);
    }

    @Override
    public void show()
    {

        Gdx.input.setInputProcessor(new InputAdapter()
        {

            @Override
            public boolean keyTyped(char character)
            {

                if(character == '\b' && username.length() > 0) username = username.substring(0, username.length()-1);
                //else if(character == '\r') confirm();
                else if(Character.isLetterOrDigit(character) && username.length() < 12) username += character;

                return true;
            }

            @Override
            public boolean keyDown(int keycode)
            {

                if(keycode == Input.Keys.ESCAPE) cancel();
                if(keycode == Input.Keys.ENTER) confirm();
                return true;
            }
        });
    }

    @Override
    public void render(float delta)
    {
        camera.update();

        cursorTimer += delta;
        if(cursorTimer > 0.5f)
        {
            showCursor = !showCursor;
            cursorTimer = 0;
        }

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);
        boolean pressingBack = Gdx.input.isTouched() && backBounds.contains(touchPoint.x, touchPoint.y);
        if(Gdx.input.justTouched() && backBounds.contains(touchPoint.x, touchPoint.y)) backPressed = true;
        if(backPressed && !Gdx.input.isTouched())
        {
            if(backBounds.contains(touchPoint.x, touchPoint.y)) cancel();
            backPressed = false;
        }

        // ---------- DRAW SHAPES ----------
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);

        //Cancel PlaceHolder
        shape.rect(textFieldBounds.x, textFieldBounds.y, textFieldBounds.width, textFieldBounds.height);
        shape.rect(enterBounds.x, enterBounds.y, enterBounds.width, enterBounds.height);
        //shape.rect(cancelBounds.x, cancelBounds.y, cancelBounds.width, cancelBounds.height);

        shape.end();

        // ---------- DRAW UI ----------
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Back button
        if(pressingBack) batch.draw(backBtnP, backBounds.x, backBounds.y, backBounds.width, backBounds.height);
        else batch.draw(backBtn, backBounds.x, backBounds.y, backBounds.width, backBounds.height);

        font.draw(batch,"ENTER USERNAME",820,700);

        String displayName = username;

        if(showCursor) displayName += "|";

        font.draw(batch,displayName, textFieldBounds.x + 20, textFieldBounds.y + 50);

        font.draw(batch,"ENTER", enterBounds.x + 70, enterBounds.y + 60);
        //font.draw(batch,"CANCEL", cancelBounds.x + 60, cancelBounds.y + 60);

        batch.end();

        handleMouse();
    }

    private void handleMouse()
    {

        if(Gdx.input.justTouched()){
            if(enterBounds.contains(touchPoint.x, touchPoint.y)) confirm();
            //if(cancelBounds.contains(touchPoint.x, touchPoint.y)) cancel();
            //if(backBounds.contains(touchPoint.x, touchPoint.y)) cancel();
        }
    }

    private void confirm()
    {
        if(username.length() == 0) return;
        if(gameMode == null || gameMode.isEmpty()) gameMode = "DEFAULT";

        // Wrap in try-catch to prevent crashes
        try {
            CharacterSelector selector = new CharacterSelector(username, gameMode);
            ((Main) Gdx.app.getApplicationListener()).setScreen(selector);
        } catch(Exception e) {
            e.printStackTrace();
            // fallback or log error
        }
    }

    private void cancel()
    {
        ((Main) Gdx.app.getApplicationListener()).setScreen(new GameModeScreen());
    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width,height);
    }

    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}

    @Override
    public void dispose()
    {
        batch.dispose();
        font.dispose();
        shape.dispose();
        backBtn.dispose();
        backBtnP.dispose();
    }
}