package io.github.PASAN;

import io.github.PASAN.characters.*;
import io.github.PASAN.characters.Character;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;

public class BattleScreen implements Screen {
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;

    private String username;
    private Texture background;
    private Texture playerSprite;
    private Texture enemySprite;

    private Character player;
    private Character enemy;

    private Rectangle skill1Bounds, skill2Bounds, skill3Bounds;
    private Vector3 touch;

    private boolean isPlayerTurn = true;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    public BattleScreen(String username, String playerCharName, String enemyCharName) {
        this.username = username;

        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2.5f); // Make text readable

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();

        background = new Texture("backgrounds/battle_bg1.png");
        playerSprite = new Texture("characters/" + playerCharName.replace(" ", "") + ".png");
        enemySprite = new Texture("characters/" + enemyCharName.replace(" ", "") + ".png");

        // instantiate character
        player = createCharacterInstance(playerCharName);
        enemy = createCharacterInstance(enemyCharName);

        skill1Bounds = new Rectangle(500, 180, 900, 80);
        skill2Bounds = new Rectangle(500, 100, 900, 80);
        skill3Bounds = new Rectangle(500, 20, 900, 80);
    }

    private Character createCharacterInstance(String name) {
        switch (name) {
            case "Jollibee": return new Jollibee();
            case "Burger King": return new BurgerKing();
            case "Jack in the Box": return new JackInTheBox();
            case "Wendy": return new Wendys();
            case "McDonald": return new Mcdonalds();
            case "Little Caesar": return new LittleCaesars();
            case "Colonel Sanders": return new KFC();
            default: return new Jollibee();
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
        batch.draw(playerSprite, 200, 350, 300, 450);
        batch.draw(enemySprite, WORLD_WIDTH - 500, 350, 300, 450);

        batch.end();
        //hover effect
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(new Color(1, 1, 0, 0.3f));

        if (skill1Bounds.contains(touch.x, touch.y)) shape.rect(skill1Bounds.x, skill1Bounds.y, skill1Bounds.width, skill1Bounds.height);
        if (skill2Bounds.contains(touch.x, touch.y)) shape.rect(skill2Bounds.x, skill2Bounds.y, skill2Bounds.width, skill2Bounds.height);
        if (skill3Bounds.contains(touch.x, touch.y)) shape.rect(skill3Bounds.x, skill3Bounds.y, skill3Bounds.width, skill3Bounds.height);

        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        font.setColor(Color.BLACK);
        // Left Box Text
        font.draw(batch, "What will\n" + player.getName() + "\ndo?", 100, 220);
        // Middle Box Text
        font.draw(batch, "SKILL 1 - " + player.getSkills().get(0).getName(), skill1Bounds.x + 20, skill1Bounds.y + 50);
        font.draw(batch, "SKILL 2 - " + player.getSkills().get(1).getName(), skill2Bounds.x + 20, skill2Bounds.y + 50);
        font.draw(batch, "SKILL 3 - " + player.getSkills().get(2).getName(), skill3Bounds.x + 20, skill3Bounds.y + 50);

        // Right Box Text (Dynamic Stats)
        font.draw(batch, "HP   - " + player.getHealth() + "/" + player.getMaxHealth(), 1480, 200);
        font.draw(batch, "Mana - " + player.getCurrentMana() + "/" + player.getMaxMana(), 1480, 120);

        // Top Screen Stats
        font.draw(batch, player.getName() + " HP: " + player.getHealth(), 50, 1030);
        font.draw(batch, enemy.getName() + " HP: " + enemy.getHealth(), WORLD_WIDTH - 400, 1030);

        batch.end();
        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.justTouched() && isPlayerTurn) {

            boolean actionTaken = false;

            if (skill1Bounds.contains(touch.x, touch.y)) {
                System.out.println(player.getName() + " used Skill 1!");
                player.basicAttack(enemy);
                actionTaken = true;
            } else if (skill2Bounds.contains(touch.x, touch.y)) {
                System.out.println(player.getName() + " used Skill 2!");
                player.secondarySkill(enemy);
                actionTaken = true;
            } else if (skill3Bounds.contains(touch.x, touch.y)) {
                System.out.println(player.getName() + " used Skill 3!");
                player.ultimateSkill(enemy);
                actionTaken = true;
            }

            if (actionTaken) {
                isPlayerTurn = false;

                // enemy logic here
                System.out.println("Enemy HP is now: " + enemy.getHealth());
                isPlayerTurn = true;
            }
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
        background.dispose();
        playerSprite.dispose();
        enemySprite.dispose();
    }
}