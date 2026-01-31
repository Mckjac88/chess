package chess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {
    protected ChessPiece[][] board;
    private final ChessPiece.PieceType[] rowOrder = {
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.KING,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.ROOK
    };

    public ChessBoard() {
         board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[8][8];
        for(int i = 1; i <= 8; i++){
            addPiece(new ChessPosition(1, i), new ChessPiece(ChessGame.TeamColor.WHITE, rowOrder[i-1]));
            addPiece(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(8, i), new ChessPiece(ChessGame.TeamColor.BLACK, rowOrder[i-1]));
        }
    }

    /**
     * Gets positions for pieces of a team color
     *
     * @param color the color to search
     * @return a collection of ChessPositions with team pieces
     */
    public Collection<ChessPosition> teamLocations(ChessGame.TeamColor color){
        Collection<ChessPosition> locations = new HashSet<>();
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++) {
                ChessPosition scanLocation = new ChessPosition(i, j);
                ChessPiece scanPiece = getPiece(scanLocation);
                if(scanPiece != null && scanPiece.getTeamColor() == color) {
                    locations.add(scanLocation);
                }
            }
        }
        return locations;
    }

    /**
     * Gets the position of the king given a color
     *
     * @param color the color to search
     * @return a ChessPosition with the king
     */
    public ChessPosition kingLocation(ChessGame.TeamColor color){
        ChessPosition location = null;
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++) {
                if (location == null) {
                    ChessPosition scanLocation = new ChessPosition(i, j);
                    ChessPiece scanPiece = getPiece(scanLocation);
                    if (scanPiece != null &&
                                    scanPiece.getTeamColor() == color &&
                                    scanPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        location = scanLocation;
                    }
                }
            }
        }
        if (location == null) {throw new RuntimeException("King not found");}
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder boardBuilder = new StringBuilder();
        for(ChessPiece[] row : board){
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append("|");
            for(ChessPiece piece : row){
                if (piece == null) {rowBuilder.append(" ");}
                else {
                    if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        rowBuilder.append(piece.getPieceType().typeString);
                    } else {
                        rowBuilder.append(piece.getPieceType().typeString.toLowerCase());
                    }
                }
                rowBuilder.append("|");
            }
            rowBuilder.append("\n");
            boardBuilder.insert(0, rowBuilder);
        }
        return boardBuilder.toString();
    }

    @Override
    public Object clone() {
        try{
            ChessBoard cloneBoard = (ChessBoard) super.clone();
            cloneBoard.board = new ChessPiece[8][8];
            for (int i = 0; i < 8; i++) {
                cloneBoard.board[i] = board[i].clone();
            }
            return cloneBoard;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This chessboard isn't cloneable for some reason" + e);
        }
    }
}

