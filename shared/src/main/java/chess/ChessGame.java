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
    protected ChessMove lastMove;
    protected ChessBoard gameBoard;

    /**
     * Enum identifying the 2 possible teams in a chess game, as well as holding useful information
     * for rows associated with each team
     */
    public enum TeamColor {
        WHITE(1, 2, 8, 4, 1),
        BLACK(-1, 7, 1, 5, 8);

        public final int pawnRowAdjust;
        public final int pawnStartRow;
        public final int pawnEndRow;
        public final int pawnDoubleRow;
        public final int backRow;

        TeamColor(int pawnRowAdjust, int pawnStartRow, int pawnEndRow, int pawnDoubleRow, int backRow){
            this.pawnRowAdjust = pawnRowAdjust;
            this.pawnStartRow = pawnStartRow;
            this.pawnEndRow = pawnEndRow;
            this.pawnDoubleRow = pawnDoubleRow;
            this.backRow = backRow;
        }

        public TeamColor opposite() {
            return this == WHITE ? BLACK : WHITE;
        }

    }

    /**
     * Enum identifying if a move is a castle (with stored boolean value) and which direction it moves
     */
    public enum CastleType {
        LEFT_CASTLE(true, 1, 4),
        RIGHT_CASTLE(true, 8, 6),
        NOT_CASTLE(false, -1, -1);

        public final boolean bool;
        public final int rookStartRow;
        public final int rookEndRow;

        CastleType(boolean bool, int rookStartRow, int rookEndRow) {
            this.bool = bool;
            this.rookStartRow = rookStartRow;
            this.rookEndRow = rookEndRow;
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
            if(isEnPassant(move)){
                ChessPosition otherPawn = new ChessPosition(
                        move.getEndPosition().getRow() - color.pawnRowAdjust,
                        move.getEndPosition().getColumn()
                );
                if(!lastMove.getEndPosition().equals(otherPawn)) {continue;}
            }

            if(isCastle(move).bool){
                if(isInCheck(color)) {continue;}

                ChessPosition halfway = new ChessPosition(
                        move.getStartPosition().getRow(),
                        5 + (move.getEndPosition().getColumn() - 5)/2
                );

                ChessGame testGame = clone();
                testGame.gameBoard.addPiece(startPosition, null);
                testGame.gameBoard.addPiece(halfway, pieceToMove);
                if(testGame.isInCheck(color)) {continue;}
            }

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

        ChessPiece startPiece = gameBoard.getPiece(startPosition);
        if (startPiece == null) {throw new InvalidMoveException("No piece at that location");}

        if(validMoves(startPosition) != null && !validMoves(startPosition).contains(move)) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        TeamColor color = startPiece.getTeamColor();
        if (color != currentTurn) {throw new InvalidMoveException("It's not your turn");}

        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        ChessPiece endPiece;
        if(promotionPiece == null) {endPiece = startPiece;}
        else {endPiece = new ChessPiece(color, promotionPiece);}

        if(isEnPassant(move)) {
            ChessPosition otherPawn = new ChessPosition(
                    move.getEndPosition().getRow() - color.pawnRowAdjust,
                    move.getEndPosition().getColumn()
            );
            gameBoard.addPiece(otherPawn, null);
        }

        CastleType isCastleMove = isCastle(move);
        if(isCastleMove.bool) {
            ChessPosition rookPosition = new ChessPosition(color.backRow, isCastleMove.rookStartRow);
            gameBoard.addPiece(
                    new ChessPosition(color.backRow, isCastleMove.rookEndRow),
                    gameBoard.getPiece(rookPosition)
            );
            gameBoard.addPiece(rookPosition, null);
        }

        gameBoard.addPiece(startPosition, null);
        gameBoard.addPiece(move.getEndPosition(), endPiece);
        currentTurn = getTeamTurn().opposite();
        lastMove = move;
    }

    /**
     * Determines if a move is en passant
     *
     * @param move the move to test
     * @return true if the move is en passant, false otherwise
     */
    private boolean isEnPassant(ChessMove move) {
        return (gameBoard.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.PAWN &&
                gameBoard.getPiece(move.getEndPosition()) == null &&
                move.getStartPosition().getColumn() != move.getEndPosition().getColumn()
            );
    }

    /**
     * Determines if a move is castling
     *
     * @param move the move to test
     * @return LEFT_CASTLE or RIGHT_CASTLE if the move is castling, NOT_CASTLE otherwise
     */
    private CastleType isCastle(ChessMove move) {
        if (gameBoard.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.KING) {
            int columnDif = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
            if (columnDif == 2) {
                return CastleType.RIGHT_CASTLE;
            } else if (columnDif == -2) {
                return CastleType.LEFT_CASTLE;
            }
        }
        return CastleType.NOT_CASTLE;
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

