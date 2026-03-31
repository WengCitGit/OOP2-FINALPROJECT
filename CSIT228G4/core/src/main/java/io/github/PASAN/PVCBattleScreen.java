package io.github.PASAN;

import com.badlogic.gdx.Game;
import io.github.PASAN.screens.GameOverScreen;
import io.github.PASAN.screens.VictoryScreen;

public class PVCBattleScreen extends BaseBattleScreen {

    public PVCBattleScreen(Game game, String username, String playerCharName, String enemyCharName) {
        super(game, username, playerCharName, enemyCharName);
    }

    @Override
    protected boolean isPVPMode() {
        return false; // This is a CPU battle, not PVP
    }

    @Override
    protected void executeEnemyTurn() {
        if (!enemy.isAlive()) return;

        // Smart CPU AI: Prioritize Ultimate, then Secondary, then Basic Attack
        if (enemyCD[2] == 0 && enemy.getCurrentMana() >= enemy.getSkills().get(2).getManaCost()) {
            executeSkill(2); // Ultimate
        }
        else if (enemyCD[1] == 0 && enemy.getCurrentMana() >= enemy.getSkills().get(1).getManaCost()) {
            executeSkill(1); // Secondary
        }
        else {
            executeSkill(0); // Basic Attack
        }
    }

    @Override
    protected void onMatchOver(boolean playerWon) {
        if (playerWon) {
            System.out.println("Victory!");
            game.setScreen(new VictoryScreen(game, username));
        } else {
            System.out.println("Defeat!");
            game.setScreen(new GameOverScreen(game, username)); // or pass game depending on your constructor
        }
        this.dispose(); // Free up memory
    }
}