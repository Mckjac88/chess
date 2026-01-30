package chess;

import java.util.Collection;
import java.util.HashSet;
import static chess.ChessPiece.PieceType.*;
import static chess.PieceMovesCalculator.Direction.*;

/**
 * A moves calculator for a single piece given the board and position
 */
public abstract class PieceMovesCalculator {
    protected final ChessBoard board;
    protected final ChessPosition position;
    protected final ChessPiece piece;

    /**
     * Final collection of moves to be returned
     */
    protected final Collection<ChessMove> validMoves = new HashSet<>();

    /**
     * Enum for the result of moving a piece one step
     * (CLEAR space, TAKE enemy piece, or BLOCKED by own team piece or out of bounds)
     */
    protected enum StepResult {CLEAR, TAKE, BLOCKED}

    /**
     * Array of PieceType (for easy iteration) of only possible promotion pieces
     */
    protected final ChessPiece.PieceType[] possiblePromotions = {QUEEN, ROOK, BISHOP, KNIGHT};

    /**
     * Enum for vectorizing movement directions
     * (this.row for row adjust, this.col for column adjust)
     */
    protected enum Direction {
        UP(0,-1), RIGHT(1,0), DOWN(0,1), LEFT(-1,0),
        UPLEFT(-1,-1), UPRIGHT(1,-1), DOWNLEFT(-1,1), DOWNRIGHT(1,1),
        UPLEFTLEFT(-2,-1), UPUPLEFT(-1,-2), UPUPRIGHT(1,-2), UPRIGHTRIGHT(2,-1),
        DOWNLEFTLEFT(-2,1), DOWNDOWNLEFT(-1,2), DOWNDOWNRIGHT(1,2), DOWNRIGHTRIGHT(2,1);

        public final int row;
        public final int col;

        Direction(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    /**
     * Collection of move directions to be assigned to each piece type
     */
    protected Direction[] moveDirections;

    /**
     * Whether the piece moves only one space by default
     * (KING and KNIGHT are true, pawn has different logic)
     */
    protected boolean oneStep = false;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
        this.piece = board.getPiece(position);
    }

    /**
     * Generates all valid moves for a piece and returns a collection
     *
     * @return A collection of valid moves
     */
    public Collection<ChessMove> moveAll() {
        for(Direction moveDirection : moveDirections){
            moveLine(moveDirection, oneStep);
        }
        return validMoves;
    }

    /**
     * Takes a direction and uses starting location to generate all valid moves in that direction
     *
     * @param moveDirection the direction the piece can move
     * @param oneStep whether it can only move once in that direction
     */
    protected void moveLine(Direction moveDirection, boolean oneStep) {
        ChessPosition target = new ChessPosition(position.getRow(), position.getColumn());
        boolean stopped = oneStep;

        do{
            target = new ChessPosition(target.getRow() + moveDirection.row, target.getColumn() + moveDirection.col);
            switch(moveStep(target)){
                case BLOCKED:
                    stopped = true;
                    break;
                case CLEAR:
                    addMove(position, target, false);
                    break;
                case TAKE:
                    addMove(position, target, false);
                    stopped = true;
                    break;
            }
        } while (!stopped);
    }

    /**
     * Given a single step in a direction, returns whether the move is valid and if a piece was taken
     * @param target the target location of the attempted move
     * @return the result of the attempted move
     */
    protected StepResult moveStep(ChessPosition target) {
        if(target.getRow() < 1 || target.getRow() > 8 || target.getColumn() < 1 || target.getColumn() > 8) {
            return StepResult.BLOCKED;
        }
        if(board.getPiece(target) == null) {
            return StepResult.CLEAR;
        }
        if(board.getPiece(target).getTeamColor() != piece.getTeamColor()) {
            return StepResult.TAKE;
        }
        return StepResult.BLOCKED;
    }

    /**
     * Adds a move to the calculator's collection (assumed to be valid).
     * In the case of promotion, adds one move for each possible promotion piece
     *
     * @param position starting position of the move
     * @param target ending position of the move
     * @param promotion whether promotion should be accounted for
     */
    protected void addMove(ChessPosition position, ChessPosition target, boolean promotion){
        if(promotion){
            for(ChessPiece.PieceType type : possiblePromotions){
                validMoves.add(new ChessMove(position, target, type));
            }
        } else {
            validMoves.add(new ChessMove(position, target, null));
        }
    }

    /**
     * A simple factory
     *
     * @param board the chessboard
     * @param position the position of the piece
     * @return a PieceMovesCalculator subclass specific to the piece type
     */
    public static PieceMovesCalculator create(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        return switch(piece.getPieceType()) {
            case KING -> new KingMovesCalculator(board, position);
            case QUEEN -> new QueenMovesCalculator(board, position);
            case BISHOP -> new BishopMovesCalculator(board, position);
            case KNIGHT -> new KnightMovesCalculator(board, position);
            case ROOK -> new RookMovesCalculator(board, position);
            case PAWN -> new PawnMovesCalculator(board, position);
        };
    }

    private static class QueenMovesCalculator extends PieceMovesCalculator {
        public QueenMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    UPLEFT, UP, UPRIGHT,
                    LEFT, RIGHT,
                    DOWNLEFT, DOWN, DOWNRIGHT
            };
        }

    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        public KingMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    UPLEFT, UP, UPRIGHT,
                    LEFT, RIGHT,
                    DOWNLEFT, DOWN, DOWNRIGHT
            };
            this.oneStep = true;
        }
    }

    private static class RookMovesCalculator extends PieceMovesCalculator {
        public RookMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    UP,
                    LEFT, RIGHT,
                    DOWN
            };
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {
        public BishopMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    UPLEFT, UPRIGHT,
                    DOWNLEFT, DOWNRIGHT
            };
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        public KnightMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    UPUPLEFT, UPUPRIGHT,
                    UPLEFTLEFT, UPRIGHTRIGHT,
                    DOWNLEFTLEFT, DOWNRIGHTRIGHT,
                    DOWNDOWNLEFT, DOWNDOWNRIGHT
            };
            this.oneStep = true;
        }
    }

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        /**
         * The adjustment in row associated with the team's direction
         */
        private final int rowAdjust;

        /**
         * The row the team's pawns start on for double step logic
         */
        private final int startRow;

        /**
         * The row in which pawns promote
         */
        private final int endRow;

        public PawnMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            ChessGame.TeamColor color = piece.getTeamColor();
            if(color == ChessGame.TeamColor.WHITE) {
                rowAdjust = 1;
                startRow = 2;
                endRow = 8;
            }
            else {
                rowAdjust = -1;
                startRow = 7;
                endRow = 1;
            }
        }

        @Override
        public Collection<ChessMove> moveAll() {
            ChessPosition target = new ChessPosition(position.getRow() + rowAdjust, position.getColumn());
            ChessPosition takeLeft = new ChessPosition(target.getRow(), target.getColumn() - 1);
            ChessPosition takeRight = new ChessPosition(target.getRow(), target.getColumn() + 1);
            boolean promotion = (target.getRow() == endRow);

            if (moveStep(target) == StepResult.CLEAR) {
                addMove(position, target, promotion);
                if (position.getRow() == startRow) {
                    target = new ChessPosition(target.getRow() + rowAdjust, target.getColumn());
                    if (moveStep(target) == StepResult.CLEAR) {
                        addMove(position, target, promotion);
                    }
                }
            }
            if (moveStep(takeLeft) == StepResult.TAKE) {
                addMove(position, takeLeft, promotion);
            }
            if (moveStep(takeRight) == StepResult.TAKE) {
                addMove(position, takeRight, promotion);
            }
            return validMoves;
        }
    }
}