package io.github.PASAN;

import com.badlogic.gdx.Game;
import io.github.PASAN.screens.VictoryScreen;

/**
 * INHERITANCE  : Extends BaseBattleScreen — gets all rendering and turn logic for free.
 * POLYMORPHISM : isPVPMode() returns true — BaseBattleScreen uses this to let
 *                Player 2 control the enemy character instead of the CPU.
 *                executeEnemyTurn() is empty because humans take turns, not the CPU.
 * ENCAPSULATION: Player 2's name is set through the constructor only.
 */
public class PVPBattleScreen extends BaseBattleScreen {

    public PVPBattleScreen(Game game, String player1Name, String player2Name,
                           String player1CharName, String player2CharName) {
        super(game, player1Name, player1CharName, player2CharName);
        this.player2Name = player2Name;
    }

    @Override
    protected boolean isPVPMode() {
        return true;
    }

    @Override
    protected void executeEnemyTurn() {
        // Intentionally empty — Player 2 is human, no CPU turn needed
    }

    @Override
    protected void onMatchOver(boolean playerWon) {
        String winner = playerWon ? username : player2Name;
        System.out.println("PVP Match Over! Winner: " + winner);
        game.setScreen(new VictoryScreen(game, winner));
        dispose();
    }
}