package io.github.PASAN;

import com.badlogic.gdx.Game;
import io.github.PASAN.modes.PVCMode;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.screens.VictoryScreen;

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
        if (!enemy.isAlive()) return;
        int skillIndex = PVCMode.chooseSkill(enemy, enemyCD);
        executeSkill(skillIndex);
    }

    @Override
    protected void onMatchOver(boolean playerWon) {
        if (playerWon) {
            game.setScreen(new VictoryScreen(game, username));
        } else {
            game.setScreen(new GameOverScreen(game, username));
        }
        dispose();
    }
}