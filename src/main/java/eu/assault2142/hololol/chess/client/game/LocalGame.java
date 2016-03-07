package eu.assault2142.hololol.chess.client.game;

import eu.assault2142.hololol.chess.client.game.ui.GameFrame;
import eu.assault2142.hololol.chess.client.translator.Translator;
import eu.assault2142.hololol.chess.game.Game;
import eu.assault2142.hololol.chess.game.Square;
import eu.assault2142.hololol.chess.game.chessmen.Bishop;
import eu.assault2142.hololol.chess.game.chessmen.CastlingMove;
import eu.assault2142.hololol.chess.game.chessmen.Chessman;
import eu.assault2142.hololol.chess.game.chessmen.King;
import eu.assault2142.hololol.chess.game.chessmen.Knight;
import eu.assault2142.hololol.chess.game.chessmen.Move;
import eu.assault2142.hololol.chess.game.chessmen.Pawn;
import eu.assault2142.hololol.chess.game.chessmen.Queen;
import eu.assault2142.hololol.chess.game.chessmen.Rook;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * A Game which is completely played locally
 *
 * @author hololol2
 */
public class LocalGame extends Game {

    private final GameFrame gameframe;
    private Chessman picked;
    private Square selected;

    /**
     * Start a new LocalGame
     */
    public LocalGame() {
        super(TYPE.LOCAL);
        gameframe = new GameFrame(this);
        new ClientMovementUpdater(this).start();
    }

    /**
     * Return the frame of the game
     *
     * @return the frame the game is played in
     */
    public GameFrame getGameFrame() {
        return gameframe;
    }

    @Override
    public ImageIcon getImage(Chessman.NAMES name, boolean black) {
        String color = "white";
        if (black) {
            color = "black";
        }
        return new ImageIcon(getClass().getResource(Settings.SETTINGS.chessmenFolder + "/" + name + "_" + color + ".gif"));
    }

    @Override
    public void updateMovements() {
        new ClientMovementUpdater(this).start();
    }

    @Override
    public void promotion(Pawn pawn) {
        Chessman man;
        String promotion = (String) JOptionPane.showInputDialog(gameframe, Translator.getBundle().getString("PROMOTION_HEAD"), Translator.getBundle().getString("PROMOTION_TEXT"), JOptionPane.QUESTION_MESSAGE, null, new String[]{Translator.getBundle().getString("CHESSMAN_QUEEN"), Translator.getBundle().getString("CHESSMAN_ROOK"), Translator.getBundle().getString("CHESSMAN_KNIGHT"), Translator.getBundle().getString("CHESSMAN_BISHOP")}, Translator.getBundle().getString("CHESSMAN_QUEEN"));
        switch (promotion) {
            //bei lokalem Spiel wird der Bauer direkt gesetzt,
            //bei Serverbasiertem senden der Daten an den Server
            case "ROOK":
                man = Rook.promotion(pawn, this);
                break;
            case "KNIGHT":
                man = Knight.promotion(pawn, this);
                break;
            case "BISHOP":
                man = Bishop.promotion(pawn, this);
                break;
            default:
                man = Queen.promotion(pawn, this);
                break;
        }
        getFiguren(pawn.isBlack())[pawn.getPositionInArray()] = man;
        getSquare(man.getX(), man.getY()).occupier = man;

        new ClientMovementUpdater(this).start();
    }

    @Override
    public void finishedCalcs() {

        gameframe.getGameBoard().movementsupdating = false;
    }

    @Override
    public void endGame() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clickAt(int feldx, int feldy) {
        selected = getSquare(feldx, feldy);
        doMoveIfPossible();
        showPossibleMoves();
    }

    /**
     * Assemble the CastlingMove which is currently selected
     *
     * @return the assembled CastlingMove
     */
    private CastlingMove getCastlingMove() {
        Rook t;
        int tx;
        int ty;
        if (picked.getX() < selected.getX()) {
            t = (Rook) getFiguren(picked.isBlack())[9];
            tx = 5;
        } else {
            t = (Rook) getFiguren(picked.isBlack())[8];
            tx = 3;
        }
        ty = picked.isBlack() ? 0 : 7;
        return new CastlingMove(selected.getX(), selected.getY(), t, tx, ty, (King) picked);
    }

    /**
     * Execute the selected Move or Capture if it is possible
     */
    private void doMoveIfPossible() {
        if (selected != null) {
            selected.highlight(Square.HIGHLIGHT.SELECTED);
            if (picked != null) {
                picked.doMove(selected.getX(), selected.getY());
                picked.doCapture(selected.getX(), selected.getY());

                if (picked.getClass() == King.class) {
                    ((King) picked).doCastling(getCastlingMove(), getGameSituation());
                }
            }
            picked = null;
        }
    }

    /**
     * Show all possible Moves for the currently selected chessman
     */
    private void showPossibleMoves() {
        if (selected.occupier != null) {
            picked = selected.occupier;
            Move[] bewegungen = getGameSituation().getAbstractChessmen(picked.isBlack())[picked.getPositionInArray()].getMoves();
            Move[] schläge = getGameSituation().getAbstractChessmen(picked.isBlack())[picked.getPositionInArray()].getCaptures();
            if (picked.getClass() == King.class) {
                CastlingMove[] rochaden = ((King) picked).computeCastlings(true, getGameSituation());
                Arrays.stream(rochaden).forEach((CastlingMove c) -> {
                    getSquare(c.getTargetX(), c.getTargetY()).highlight(Square.HIGHLIGHT.CASTLING);
                });

            }
            Arrays.stream(bewegungen).forEach((Move m) -> {
                getSquare(m.getTargetX(), m.getTargetY()).highlight(Square.HIGHLIGHT.MOVETARGET);
            });
            Arrays.stream(schläge).forEach((Move m) -> {
                getSquare(m.getTargetX(), m.getTargetY()).highlight(Square.HIGHLIGHT.CAPTURETARGET);
            });

        }
    }
}
