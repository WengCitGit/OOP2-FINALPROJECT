package io.github.PASAN;

import com.badlogic.gdx.Game;
import io.github.PASAN.screens.VictoryScreen;

public class PVPBattleScreen extends BaseBattleScreen {

    public PVPBattleScreen(Game game, String player1Name, String player2Name, String player1CharName, String player2CharName) {
        super(game, player1Name, player1CharName, player2CharName);
        this.player2Name = player2Name;
    }

    @Override
    protected boolean isPVPMode() {
        return true; // Tells the Base screen to let Player 2 use the mouse!
    }

    @Override
    protected void executeEnemyTurn() {
        // We leave this completely empty!
        // Because it's PVP, the CPU doesn't take turns. Humans do.
    }

    @Override
    protected void onMatchOver(boolean playerWon) {
        // You can customize this later to show "Player 1 Wins!" or "Player 2 Wins!"
        System.out.println("PVP Match Over!");
        game.setScreen(new VictoryScreen(game, username));
        this.dispose();
    }
}