package io.github.PASAN;

import io.github.PASAN.characters.*;
import io.github.PASAN.characters.Character;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.viewport.*;

public class BattleScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;

    private String username;
    private Texture background;
    private Texture playerSprite;
    private Texture enemySprite;

    // --- UI TEXTURES ---
    private Texture redUi;
    private Texture yellowUi;

    // --- SKILL BUTTON TEXTURES ---
    private Texture skill1Btn, skill1BtnP;
    private Texture skill2Btn, skill2BtnP;
    private Texture skill3Btn, skill3BtnP;

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
        font = new BitmapFont();
        font.getData().setScale(2.5f);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        touch = new Vector3();

        background = new Texture("backgrounds/battle_bg1.png");
        playerSprite = new Texture("characters/" + playerCharName.replace(" ", "") + ".png");
        enemySprite = new Texture("characters/" + enemyCharName.replace(" ", "") + ".png");

        // --- LOAD UI TEXTURES ---
        redUi = new Texture("backgrounds/red_background.png");
        yellowUi = new Texture("backgrounds/yellow_background.png");

        // --- LOAD SKILL BUTTON TEXTURES ---
        skill1Btn = new Texture("buttons/skill1_button.png");
        skill1BtnP = new Texture("buttons/skill1_button_pressed.png");

        skill2Btn = new Texture("buttons/skill2_button.png");
        skill2BtnP = new Texture("buttons/skill2_button_pressed.png");

        skill3Btn = new Texture("buttons/skill3_button.png");
        skill3BtnP = new Texture("buttons/skill3_button_pressed.png");

        // instantiate character
        player = createCharacterInstance(playerCharName);
        enemy = createCharacterInstance(enemyCharName);

        // Bounds for clicking the buttons
        skill1Bounds = new Rectangle(555, 230, 800, 70); // Top
        skill2Bounds = new Rectangle(555, 145, 800, 70); // Middle
        skill3Bounds = new Rectangle(555, 60, 800, 70);  // Bottom
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

        boolean isTouchingSkill1 = Gdx.input.isTouched() && skill1Bounds.contains(touch.x, touch.y);
        boolean isTouchingSkill2 = Gdx.input.isTouched() && skill2Bounds.contains(touch.x, touch.y);
        boolean isTouchingSkill3 = Gdx.input.isTouched() && skill3Bounds.contains(touch.x, touch.y);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.draw(playerSprite, 200, 350, 300, 450);
        batch.draw(enemySprite, WORLD_WIDTH - 500, 350, 300, 450);

        batch.draw(yellowUi, 40, 20, 1420, 320);
        batch.draw(redUi, 140, 60, 350, 250);
        batch.draw(redUi, 1480, 20, 400, 300);

        if (isTouchingSkill1) batch.draw(skill1BtnP, skill1Bounds.x, skill1Bounds.y, 250, 70);
        else batch.draw(skill1Btn, skill1Bounds.x, skill1Bounds.y, 250, 70);

        if (isTouchingSkill2) batch.draw(skill2BtnP, skill2Bounds.x, skill2Bounds.y, 250, 70);
        else batch.draw(skill2Btn, skill2Bounds.x, skill2Bounds.y, 250, 70);

        if (isTouchingSkill3) batch.draw(skill3BtnP, skill3Bounds.x, skill3Bounds.y, 250, 70);
        else batch.draw(skill3Btn, skill3Bounds.x, skill3Bounds.y, 250, 70);


        font.setColor(Color.WHITE);
        font.draw(batch, "What will\n" + player.getName() + "\ndo?", 200, 240);


        font.draw(batch, "HP   - " + player.getHealth() + "/" + player.getMaxHealth(), 1545, 220);
        font.draw(batch, "Mana - " + player.getCurrentMana() + "/" + player.getMaxMana(), 1545, 140);
        font.draw(batch, player.getName() + " HP: " + player.getHealth(), 50, 1030);
        font.draw(batch, enemy.getName() + " HP: " + enemy.getHealth(), WORLD_WIDTH - 450, 1030);

        font.setColor(Color.BLACK);
        font.draw(batch, "- " + player.getSkills().get(0).getName(), skill1Bounds.x + 280, skill1Bounds.y + 50);
        font.draw(batch, "- " + player.getSkills().get(1).getName(), skill2Bounds.x + 280, skill2Bounds.y + 50);
        font.draw(batch, "- " + player.getSkills().get(2).getName(), skill3Bounds.x + 280, skill3Bounds.y + 50);

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
        font.dispose();
        background.dispose();
        playerSprite.dispose();
        enemySprite.dispose();
        redUi.dispose();
        yellowUi.dispose();

        skill1Btn.dispose();
        skill1BtnP.dispose();
        skill2Btn.dispose();
        skill2BtnP.dispose();
        skill3Btn.dispose();
        skill3BtnP.dispose();
    }
}