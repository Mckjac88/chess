package chess;

import java.util.Collection;
import java.util.HashSet;

public abstract class PieceMovesCalculator {
    protected final ChessBoard board;
    protected final ChessPosition position;
    protected final ChessPiece piece;
    protected final ChessGame.TeamColor color;
    protected final Collection<ChessMove> pieceMoves = new HashSet<>();
    protected int[][] pieceDirections;
    protected boolean slide;
    protected enum StepResult {CLEAR, BLOCKED, TAKE}

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
        this.piece = board.getPiece(position);
        this.color = piece.getTeamColor();
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

    protected Collection<ChessMove> moveAll() {
        for (int[] direction : pieceDirections) {
            moveLine(direction);
        }
        return pieceMoves;
    }

    protected void moveLine(int[] direction) {
        ChessPosition target = new ChessPosition(position.getRow(), position.getColumn());
        boolean sliding = slide;
        do {
            target = new ChessPosition(target.getRow() + direction[0], target.getColumn() + direction[1]);
            var result = moveStep(target);
            if (result != StepResult.BLOCKED) addMove(target);
            if (result != StepResult.CLEAR) sliding = false;
        } while (sliding);
    }

    protected StepResult moveStep(ChessPosition target){
        if (target.getRow() < 1 || target.getRow() > 8 || target.getColumn() < 1 || target.getColumn() > 8) {
            return StepResult.BLOCKED;
        }
        ChessPiece targetPiece = board.getPiece(target);
        if (targetPiece == null) return StepResult.CLEAR;
        if (targetPiece.getTeamColor() != color) return StepResult.TAKE;
        return StepResult.BLOCKED;

    }

    protected void addMove(ChessPosition target){
        pieceMoves.add(new ChessMove(position, target, null));
    }

    private static class RookMovesCalculator extends PieceMovesCalculator {
        public RookMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {{0,-1},{1,0},{0,1},{-1,0}};
            this.slide = true;
        }
    }

    private static class KnightMovesCalculator extends PieceMovesCalculator {
        public KnightMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {
                    {1,-2},{2,-1},{2,1},{1,2},
                    {-1,2},{-2,1},{-2,-1},{-1,-2}
            };
            this.slide = false;
        }
    }

    private static class BishopMovesCalculator extends PieceMovesCalculator {
        public BishopMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {{1,-1},{1,1},{-1,1},{-1,-1}};
            this.slide = true;
        }
    }

    private static class QueenMovesCalculator extends PieceMovesCalculator {
        public QueenMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {
                    {0,-1},{1,0},{0,1},{-1,0},
                    {1,-1},{1,1},{-1,1},{-1,-1}
            };
            this.slide = true;
        }
    }

    private static class KingMovesCalculator extends PieceMovesCalculator {
        public KingMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {
                    {0,-1},{1,0},{0,1},{-1,0},
                    {1,-1},{1,1},{-1,1},{-1,-1}
            };
            this.slide = false;
        }
    }

    private static class PawnMovesCalculator extends PieceMovesCalculator {
        public PawnMovesCalculator(ChessBoard board, ChessPosition position) {
            super(board, position);
            this.pieceDirections = new int[][] {{1,0}};
        }

        @Override
        protected Collection<ChessMove> moveAll() {
            int step = color == ChessGame.TeamColor.WHITE ? 1 : -1;
            ChessPosition target = new ChessPosition(position.getRow() + step, position.getColumn());
            if (moveStep(target) == StepResult.CLEAR) {
                addMove(target);
                if (color == ChessGame.TeamColor.WHITE && position.getRow() == 2 ||
                        color == ChessGame.TeamColor.BLACK && position.getRow() == 7) {
                    ChessPosition doubleStep = new ChessPosition(target.getRow() + step, target.getColumn());
                    if (moveStep(doubleStep) == StepResult.CLEAR) addMove(doubleStep);
                }
            }
            ChessPosition takeRight = new ChessPosition(position.getRow() + step, position.getColumn() + 1);
            if (moveStep(takeRight) == StepResult.TAKE) addMove(takeRight);
            ChessPosition takeLeft = new ChessPosition(position.getRow() + step, position.getColumn() - 1);
            if (moveStep(takeLeft) == StepResult.TAKE) addMove(takeLeft);
            return pieceMoves;
        }
    }
}
