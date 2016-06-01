package eu.assault2142.hololol.chess.client.game;

import eu.assault2142.hololol.chess.game.GameState;
import eu.assault2142.hololol.chess.game.MovementUpdater;

/**
 * Updates the possible moves and captures after each turn. Also checks for
 * check/checkmate/stalemate-situations. Used in games with a UI
 *
 * @author hololol2
 */
public class ClientMovementUpdater extends MovementUpdater {

    /**
     * Create a new Instance
     *
     * @param state the game-state to update
     */
    public ClientMovementUpdater(GameState state) {
        super(state);
    }

    @Override
    public void run() {

        Runnable wait = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Main.MENU.showErrorMessage("Unexpected Critical Error!", true);
            }
        };
        Thread waitthread = new Thread(wait);
        waitthread.start();
        updateMovements();
        try {
            waitthread.join();
        } catch (InterruptedException ex) {
            Main.MENU.showErrorMessage("Unexpected Critical Error!", true);
        }
        testCheck();
    }

}
