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
        WHITE_LEFT_CASTLE(true, 1, 1, 4),
        WHITE_RIGHT_CASTLE(true, 1,8, 6),
        BLACK_LEFT_CASTLE(true, 8, 1, 4),
        BLACK_RIGHT_CASTLE(true, 8, 8, 6),
        NOT_CASTLE(false, -1, -1, -1);

        public final boolean bool;
        public final ChessPosition rookLocation;
        public final ChessPosition rookTarget;

        CastleType(boolean bool, int startRow, int rookStartCol, int rookEndCol) {
            this.bool = bool;
            this.rookLocation = new ChessPosition(startRow, rookStartCol);
            this.rookTarget = new ChessPosition(startRow, rookEndCol);
        }
    }

    public ChessGame() {
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        currentTurn = TeamColor.WHITE;
        whiteLeftRookMoved = false;
        whiteKingMoved = false;
        whiteRightRookMoved = false;
        blackLeftRookMoved = false;
        blackKingMoved = false;
        blackRightRookMoved = false;
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
            CastleType castle = isCastle(move, color);
            if(castle.bool){
                if(isInCheck(color)) {continue;}
                if(castleHasMoved(castle)) {continue;}

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

        CastleType castle = isCastle(move, color);
        if(castle.bool) {
            gameBoard.addPiece(
                    castle.rookTarget,
                    gameBoard.getPiece(castle.rookLocation)
            );
            gameBoard.addPiece(castle.rookLocation, null);
        }

        updateCastleMove(move.getStartPosition());

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
    private CastleType isCastle(ChessMove move, TeamColor color) {
        if (gameBoard.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.KING) {
            int columnDif = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
            if (columnDif == 2) {
                if(color == TeamColor.WHITE) {return CastleType.WHITE_RIGHT_CASTLE;}
                if(color == TeamColor.BLACK) {return CastleType.BLACK_RIGHT_CASTLE;}
            } else if (columnDif == -2) {
                if(color == TeamColor.WHITE) {return CastleType.WHITE_LEFT_CASTLE;}
                if(color == TeamColor.BLACK) {return CastleType.BLACK_LEFT_CASTLE;}
            }
        }
        return CastleType.NOT_CASTLE;
    }

    private boolean castleHasMoved(CastleType castle) {
        return switch(castle) {
            case NOT_CASTLE -> false;
            case WHITE_LEFT_CASTLE -> whiteKingMoved || whiteLeftRookMoved;
            case WHITE_RIGHT_CASTLE -> whiteKingMoved || whiteRightRookMoved;
            case BLACK_LEFT_CASTLE -> blackKingMoved || blackLeftRookMoved;
            case BLACK_RIGHT_CASTLE -> blackKingMoved || blackRightRookMoved;
        };
    }

    private void updateCastleMove(ChessPosition piecePosition) {
        int row = piecePosition.getRow();
        int col = piecePosition.getColumn();

        whiteLeftRookMoved  |= (row == 1 && col == 1);
        whiteKingMoved      |= (row == 1 && col == 5);
        whiteRightRookMoved |= (row == 1 && col == 8);
        blackLeftRookMoved  |= (row == 8 && col == 1);
        blackKingMoved      |= (row == 8 && col == 5);
        blackRightRookMoved |= (row == 8 && col == 8);
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
                "}\n" + gameBoard;
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

