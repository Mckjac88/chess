package chess;

import java.util.Collection;
import java.util.HashSet;
import static chess.ChessPiece.PieceType.*;


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
    protected final Collection<ChessMove> calculatorMoves = new HashSet<>();

    /**
     * Enum for the result of moving a piece one step
     * (CLEAR space, TAKE enemy piece, or BLOCKED by own team piece or out of bounds)
     */
    protected enum StepResult {CLEAR, TAKE, BLOCKED}

    /**
     * Array of PieceType (for easy iteration) of only possible promotion pieces
     */
    protected final ChessPiece.PieceType[] possiblePromotions = {QUEEN, ROOK, BISHOP, KNIGHT};

    protected int[] upVec = {1,0};
    protected int[] rightVec = {0,1};
    protected int[] downVec = {-1,0};
    protected int[] leftVec = {0,-1};
    protected int[] upLeftVec = {1,-1};
    protected int[] upRightVec = {1,1};
    protected int[] downLeftVec = {-1,-1};
    protected int[] downRightVec = {-1,1};
    protected int[] upLeftLeftVec = {1,-2};
    protected int[] upUpLeftVec = {2,-1};
    protected int[] upUpRightVec = {2,1};
    protected int[] upRightRightVec = {1,2};
    protected int[] downLeftLeftVec = {-1,-2};
    protected int[] downDownLeftVec = {-2,-1};
    protected int[] downDownRightVec = {-2,1};
    protected int[] downRightRightVec = {-1,2};

    /**
     * Collection of move directions to be assigned to each piece type
     */
    protected int[][] moveDirections;

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
        for(int[] moveDirection : moveDirections){
            moveLine(moveDirection, oneStep);
        }
        return calculatorMoves;
    }

    /**
     * Takes a direction and uses starting location to generate all valid moves in that direction
     *
     * @param moveDirection the direction the piece can move
     * @param oneStep whether it can only move once in that direction
     */
    protected void moveLine(int[] moveDirection, boolean oneStep) {
        ChessPosition target = new ChessPosition(position.getRow(), position.getColumn());
        boolean stopped = oneStep;

        do{
            target = new ChessPosition(target.getRow() + moveDirection[0], target.getColumn() + moveDirection[1]);
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
     * @param promotion whether promotion should be accounted for (note that promotion
     *                  is boolean unlike promotionPiece, which is usually null)
     */
    protected void addMove(ChessPosition position, ChessPosition target, boolean promotion){
        if(promotion){
            for(ChessPiece.PieceType type : possiblePromotions){
                calculatorMoves.add(new ChessMove(position, target, type));
            }
        } else {
            calculatorMoves.add(new ChessMove(position, target, null));
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
            this.moveDirections = new int[][] {
                    upLeftVec, upVec, upRightVec,
                    leftVec, rightVec,
                    downLeftVec, downVec, downRightVec
            };
        }

    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        public KingMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new int[][] {
                    upLeftVec, upVec, upRightVec,
                    leftVec, rightVec,
                    downLeftVec, downVec, downRightVec
            };
            this.oneStep = true;
        }

        /**
         * Short helper function to get the piece on your same row given the column
         *
         * @param col the column to check
         * @return the piece at the location (or null if empty)
         */
        private ChessPiece rowPiece(int col) {
            return board.getPiece(new ChessPosition(position.getRow(), col));
        }

        @Override
        public Collection<ChessMove> moveAll() {
            for(int[] moveDirection : moveDirections){
                moveLine(moveDirection, oneStep);
            }
            if (new ChessPosition(piece.getTeamColor().backRow,5).equals(position)) {
                if (rowPiece(6) == null && rowPiece(7) == null
                        && rowPiece(8) != null && rowPiece(8).getPieceType() == ROOK) {
                    addMove(position, new ChessPosition(position.getRow(), 7), false);
                }
                if (rowPiece(4) == null && rowPiece(3) == null && rowPiece(2) == null
                        && rowPiece(1) != null && rowPiece(1).getPieceType() == ROOK) {
                    addMove(position, new ChessPosition(position.getRow(), 3), false);
                }
            }
            return calculatorMoves;
        }
    }

    private static class RookMovesCalculator extends PieceMovesCalculator {
        public RookMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new int[][] {
                    upVec,
                    leftVec, rightVec,
                    downVec
            };
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {
        public BishopMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new int[][] {
                    upLeftVec, upRightVec,
                    downLeftVec, downRightVec
            };
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        public KnightMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new int[][] {
                    upUpLeftVec, upUpRightVec,
                    upLeftLeftVec, upRightRightVec,
                    downLeftLeftVec, downRightRightVec,
                    downDownLeftVec, downDownRightVec
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
            rowAdjust = color.pawnRowAdjust;
            startRow = color.pawnStartRow;
            endRow = color.pawnEndRow;
        }

        @Override
        public Collection<ChessMove> moveAll() {
            ChessPosition target = new ChessPosition(position.getRow() + rowAdjust, position.getColumn());
            ChessPosition takeLeft = new ChessPosition(target.getRow(), target.getColumn() - 1);
            ChessPosition takeRight = new ChessPosition(target.getRow(), target.getColumn() + 1);
            ChessPosition spaceLeft = new ChessPosition(position.getRow(), position.getColumn() - 1);
            ChessPosition spaceRight = new ChessPosition(position.getRow(), position.getColumn() + 1);
            boolean pawnLeft = false;
            boolean pawnRight = true;

            if(!(spaceLeft.getColumn() < 1 || spaceLeft.getColumn() > 8)) {
                ChessPiece pieceLeft = board.getPiece(spaceLeft);
                pawnLeft = (pieceLeft != null &&
                        pieceLeft.getPieceType() == PAWN &&
                        pieceLeft.getTeamColor() != piece.getTeamColor());
            }
            if(!(spaceRight.getColumn() < 1 || spaceRight.getColumn() > 8)) {
                ChessPiece pieceRight = board.getPiece(spaceRight);
                 pawnRight = (pieceRight != null &&
                        pieceRight.getPieceType() == PAWN &&
                        pieceRight.getTeamColor() != piece.getTeamColor());
            }
            boolean promotion = (target.getRow() == endRow);

            if (moveStep(target) == StepResult.CLEAR) {
                addMove(position, target, promotion);
                if (position.getRow() == startRow) {
                    target = new ChessPosition(target.getRow() + rowAdjust, target.getColumn());
                    if (moveStep(target) == StepResult.CLEAR) {
                        addMove(position, target, false);
                    }
                }
            }
            if (moveStep(takeLeft) == StepResult.TAKE || moveStep(takeLeft) == StepResult.CLEAR && pawnLeft) {
                addMove(position, takeLeft, promotion);
            }
            if (moveStep(takeRight) == StepResult.TAKE || moveStep(takeRight) == StepResult.CLEAR && pawnRight) {
                addMove(position, takeRight, promotion);
            }
            return calculatorMoves;
        }
    }
}