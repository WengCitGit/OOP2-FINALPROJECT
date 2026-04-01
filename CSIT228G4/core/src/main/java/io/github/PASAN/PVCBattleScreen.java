package io.github.PASAN;

import com.badlogic.gdx.Game;
import io.github.PASAN.modes.PVCMode;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.screens.VictoryScreen;

/**
 * INHERITANCE  : Extends BaseBattleScreen — gets all rendering and turn logic for free.
 * POLYMORPHISM : executeEnemyTurn() overrides the base, calls PVCMode.chooseSkill()
 *                so the AI decision is always resolved the same way regardless of which
 *                enemy character is fighting.
 * ENCAPSULATION: This class only knows about match outcomes — score/screen logic
 *                stays here, AI logic stays in PVCMode.
 */
public class PVCBattleScreen extends BaseBattleScreen {

    public PVCBattleScreen(Game game, String username, String playerCharName, String enemyCharName) {
        super(game, username, playerCharName, enemyCharName);
    }

    @Override
    protected boolean isPVPMode() {
        return false;
    }

    @Override
    protected void executeEnemyTurn() {
        if (!enemy.isAlive()) return;
        // POLYMORPHISM: PVCMode.chooseSkill decides which skill index to use
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