package chess;

import java.util.Collection;
import java.util.HashSet;
import static chess.ChessPiece.PieceType.*;

/**
 * A Calculator to determine viable moves
 */
public abstract class PieceMovesCalculator {
    protected final ChessBoard board;
    protected final ChessPosition position;
    protected final ChessPiece piece;
    protected final Collection<ChessMove> validMoves = new HashSet<>();
    protected enum StepResult {CLEAR, TAKE, BLOCKED}
    protected final ChessPiece.PieceType[] possiblePromotions = {QUEEN, ROOK, BISHOP, KNIGHT};
    protected enum Direction {
        UP(0,-1), RIGHT(1,0), DOWN(0,1), LEFT(-1,0),
        UPLEFT(-1,-1), UPRIGHT(1,-1), DOWNLEFT(-1,1), DOWNRIGHT(1,1),
        UPLEFTLEFT(-2,-1), UPUPLEFT(-1,-2), UPUPRIGHT(1,-2), UPRIGHTRIGHT(2,-1),
        DOWNLEFTLEFT(-2,1), DOWNDOWNLEFT(-1,2), DOWNDOWNRIGHT(1,2), DOWNRIGHTRIGHT(2,1);

        public final int x;
        public final int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    protected Direction[] moveDirections;
    protected boolean oneStep = false;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position){
        this.board = board;
        this.position = position;
        this.piece = board.getPiece(position);
    }

    public Collection<ChessMove> moveAll() {
        for(Direction moveDirection : moveDirections){
            moveLine(moveDirection, oneStep);
        }
        return validMoves;
    }

    protected void moveLine(Direction moveDirection, boolean oneStep) {
        ChessPosition target = new ChessPosition(position.getRow(), position.getColumn());
        boolean stopped = oneStep;

        do{
            target = new ChessPosition(target.getRow() + moveDirection.x, target.getColumn() + moveDirection.y);
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

    protected void addMove(ChessPosition position, ChessPosition target, boolean promotion){
        if(promotion){
            for(ChessPiece.PieceType type : possiblePromotions){
                validMoves.add(new ChessMove(position, target, type));
            }
        } else {
            validMoves.add(new ChessMove(position, target, null));
        }
    }

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
                    Direction.UPLEFT, Direction.UP, Direction.UPRIGHT,
                            Direction.LEFT, Direction.RIGHT,
                    Direction.DOWNLEFT, Direction.DOWN, Direction.DOWNRIGHT
            };
        }

    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        public KingMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    Direction.UPLEFT, Direction.UP, Direction.UPRIGHT,
                    Direction.LEFT, Direction.RIGHT,
                    Direction.DOWNLEFT, Direction.DOWN, Direction.DOWNRIGHT
            };
            this.oneStep = true;
        }
    }

    private static class RookMovesCalculator extends PieceMovesCalculator {
        public RookMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    Direction.UP,
                    Direction.LEFT, Direction.RIGHT,
                    Direction.DOWN
            };
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {
        public BishopMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    Direction.UPLEFT, Direction.UPRIGHT,
                    Direction.DOWNLEFT, Direction.DOWNRIGHT
            };
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        public KnightMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.moveDirections = new Direction[] {
                    Direction.UPUPLEFT, Direction.UPUPRIGHT,
                    Direction.UPLEFTLEFT, Direction.UPRIGHTRIGHT,
                    Direction.DOWNLEFTLEFT, Direction.DOWNRIGHTRIGHT,
                    Direction.DOWNDOWNLEFT, Direction.DOWNDOWNRIGHT
            };
            this.oneStep = true;
        }
    }

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        private final int rowAdjust;
        private final int startRow;
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