package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame implements Cloneable {
    protected TeamColor currentTurn;
    protected boolean whiteKingMoved, whiteLeftRookMoved, whiteRightRookMoved,
        blackKingMoved, blackLeftRookMoved, blackRightRookMoved;
    protected ChessBoard gameBoard;

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK;

        public TeamColor opposite() {
            return this == WHITE ? BLACK : WHITE;
        }

    }

    public ChessGame() {
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Sets which team's turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece pieceToMove = gameBoard.getPiece(startPosition);
        if(pieceToMove == null) {return null;}
        TeamColor color = pieceToMove.getTeamColor();
        Collection<ChessMove> potentialMoves = pieceToMove.pieceMoves(gameBoard, startPosition);
        Collection<ChessMove> positionValidMoves = new HashSet<>();
        for (ChessMove move : potentialMoves) {
            ChessGame testGame = clone();
            testGame.gameBoard.addPiece(startPosition, null);
            testGame.gameBoard.addPiece(move.getEndPosition(), pieceToMove);
            if(!testGame.isInCheck(color)){
                positionValidMoves.add(move);
            }
        }
        return positionValidMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();

        if(validMoves(startPosition) != null && !validMoves(startPosition).contains(move)) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        ChessPiece startPiece = gameBoard.getPiece(startPosition);
        TeamColor color = startPiece.getTeamColor();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        ChessPiece endPiece;

        if(promotionPiece == null) {endPiece = startPiece;}
        else {endPiece = new ChessPiece(color, promotionPiece);}

        gameBoard.addPiece(startPosition, null);
        gameBoard.addPiece(move.getEndPosition(), endPiece);
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor enemyColor = teamColor.opposite();
        Collection<ChessPosition> enemyLocations = gameBoard.teamLocations(enemyColor);
        ChessPosition ownKingLocation = gameBoard.kingLocation(teamColor);

        for (ChessPosition enemyLocation : enemyLocations) {
            for (ChessMove testMove : gameBoard.getPiece(enemyLocation).pieceMoves(gameBoard, enemyLocation)) {
                if (testMove.getEndPosition().equals(ownKingLocation)) {return true;}
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoValidMove(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && hasNoValidMove(teamColor);
    }

    /**
     * Determines if the given team has no valid moves
     *
     * @param teamColor which team to check for valid moves
     * @return True if the specified team has no valid moves, otherwise false
     */
    public boolean hasNoValidMove(TeamColor teamColor) {
        Collection<ChessPosition> ownLocations = gameBoard.teamLocations(teamColor);
        for (ChessPosition ownLocation : ownLocations) {
            if (!validMoves(ownLocation).isEmpty()) {return false;}
        }
        return true;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return whiteKingMoved == chessGame.whiteKingMoved
                && whiteLeftRookMoved == chessGame.whiteLeftRookMoved
                && whiteRightRookMoved == chessGame.whiteRightRookMoved
                && blackKingMoved == chessGame.blackKingMoved
                && blackLeftRookMoved == chessGame.blackLeftRookMoved
                && blackRightRookMoved == chessGame.blackRightRookMoved
                && currentTurn == chessGame.currentTurn
                && Objects.equals(gameBoard, chessGame.gameBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                currentTurn, whiteKingMoved, whiteLeftRookMoved, whiteRightRookMoved,
                blackKingMoved, blackLeftRookMoved, blackRightRookMoved, gameBoard);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "currentTurn=" + currentTurn +
                ", whiteKingMoved=" + whiteKingMoved +
                ", whiteLeftRookMoved=" + whiteLeftRookMoved +
                ", whiteRightRookMoved=" + whiteRightRookMoved +
                ", blackKingMoved=" + blackKingMoved +
                ", blackLeftRookMoved=" + blackLeftRookMoved +
                ", blackRightRookMoved=" + blackRightRookMoved +
                ", gameBoard=" + gameBoard +
                '}';
    }

    @Override
    protected ChessGame clone() {
        try{
            ChessGame gameClone = (ChessGame) super.clone();
            gameClone.gameBoard = (ChessBoard) gameBoard.clone();
            return gameClone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This chess game isn't cloneable for some reason: " + e);
        }
    }
}

