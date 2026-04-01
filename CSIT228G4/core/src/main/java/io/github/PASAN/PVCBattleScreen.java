package io.github.PASAN;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.PASAN.modes.PVCMode;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.screens.VictoryScreen;

//1 Thread at executeEnemyTurn()
public class PVCBattleScreen extends BaseBattleScreen {

    /**
     * Constructor now accepts 4 parameters.
     * The 'enemyCharName' is provided by the VSScreen.
     */
    public PVCBattleScreen(Game game, String username, String playerCharName, String enemyCharName) {
        super(game, username, playerCharName, enemyCharName);
        System.out.println("[PVC] Battle Loaded: " + playerCharName + " vs " + enemyCharName);
    }

    @Override
    protected boolean isPVPMode() {
        return false;
    }

    @Override
    protected void executeEnemyTurn() {
//        if (!enemy.isAlive()) return;
//        int skillIndex = PVCMode.chooseSkill(enemy, enemyCD);
//        executeSkill(skillIndex);

        if(!enemy.isAlive()) return;

        new Thread(new Runnable(){
            @Override
            public void run(){
                int skillIndex = PVCMode.chooseSkill(enemy, enemyCD);

                Gdx.app.postRunnable(new Runnable(){
                    @Override
                    public void run(){
                        executeSkill(skillIndex);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onMatchOver(boolean playerWon) {
        if (playerWon) {
            game.setScreen(new GameOverScreen(game, username));
        } else {
//            game.setScreen(new VictoryScreen(game, username, 0, "You defeated the Computer. Good for you!"));
            game.setScreen(new GameOverScreen(game, username));
        }
        dispose();
    }
}