package io.github.PASAN.modes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.leaderboard.CalculateScore;
import io.github.PASAN.leaderboard.Leaderboard;

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
            CalculateScore scoreCalculator = new CalculateScore();
            int finalScore = scoreCalculator.calculatePVCScore(true, player.getHealth(), enemyWins);

            Leaderboard manager = new Leaderboard("pvc_scores.txt");
            manager.addScore(username, finalScore);
            game.setScreen(new GameOverScreen(game, username, playerWon, true));
        } else {
            game.setScreen(new GameOverScreen(game, username, playerWon, true));
        }
        this.dispose();
    }
}