package game;

import static game.Mark.*;

import java.util.Random;

/**
 * AI for Tic Tac Toe.
 * Multiple difficulties:
 * {@link #easyMove(Field, Mark)}
 * {@link #mediumMove(Field, Mark)}
 * {@link #hardMove(Field, Mark)}
 */
public abstract class AI {

    static Random random = new Random();

    /**
     * Compute difficulty level "Easy" (random) move and print updated field.
     * @param field current field.
     * @param markAI the AIs mark.
     */
    private static int[] easyMove(Field field, Mark markAI) {
        while (true) {
            int row = random.nextInt(3);
            int column = random.nextInt(3);
            if (field.available(row, column)) {
                return new int[] {row, column};
            }
        }
    }

    /**
     * Compute difficulty level "Medium" move and print updated field.
     * This method executes either an Easy or Hard move depending on the state of the game.
     * Hard if 1 move away from losing or winning, else Easy.
     * @param field current instance of the game.
     * @param markAI the AIs mark.
     * @return Int[] containing move.
     */
    private static int[] mediumMove(Field field, Mark markAI) {
        int bestScore = Integer.MIN_VALUE;
        int worstScore = Integer.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field.available(i, j)) {
                    field.setMark(i, j, markAI);
                    int score = miniMax(field, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false, markAI);
                    field.setMark(i, j, BLANK);
                    bestScore = Math.max(bestScore, score);
                    worstScore = Math.min(worstScore, score);
                    if (bestScore == 10 || worstScore == -20) {
                        return hardMove(field, markAI);
                    }
                }
            }
        }
        return easyMove(field, markAI);
    }

    /**
     * Compute difficulty level "Hard" (optimal) move.
     * @param field current instance of the game.
     * @param markAI AIs mark.
     * @return Int[] containing move.
     */
    private static int[] hardMove(Field field, Mark markAI) {
        // If AIs mark is X and its the first turn..
        if(markAI == X && field.numOfMarks() == 0) {
            return getFirstHardMove();
        }

        int bestScore = Integer.MIN_VALUE;
        int[] move = {-1, -1};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field.available(i, j)) {
                    field.setMark(i, j, markAI);
                    int score = miniMax(field, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false, markAI);
                    field.setMark(i, j, BLANK);
                    if (score > bestScore) {
                        bestScore = score;
                        move[0] = i;
                        move[1] = j;
                    }
                }
            }
        }
        return move;
    }

    /**
     * The Minimax algorithm. Used to assigns a score for each valid move possible (using recursion).
     * @param field current instance of the game.
     * @param depth counts moves required to achieve a certain outcome.
     *              This is then used for a more logical evaluation of the score
     *              (e.g. quicker win = higher score).
     * @param alpha value for alpha-beta pruning.
     * @param beta value for alpha-beta pruning.
     * @param isMax true if maximizing score, else minimizing.
     * @param markAI AIs mark.
     * @return score.
     */
    private static int miniMax(Field field, int depth, int alpha, int beta, boolean isMax, Mark markAI) {
        // Terminal condition: Ends method execution if end-of-game state is reached in during recursion.
        // Logic: If the AI marks miniMax move (achieved by the recursion later) ultimately results in a win,
        // the moves score is valued as 10. If its a loss its -10 and the score is 0 in case of a draw
        // (-20 if lose next turn for clearer distinction from regular lose, used for medium difficulty).
        // We then also subtract the depth (if win) or subtract FROM the depth (if loose).

        // This will achieve two things:
        // It will have the effect that a faster win has a higher score and a slower (delayed) lose will have a higher score then losing fast.
        // For example if a lose can be delayed for 2 turns by blocking a row (considering opponent plays perfect and AI knows
        // loose is guaranteed because of that anyway), this score will be higher than the score for not blocking and
        // letting the opponent win faster.
        if (!field.getGameState().equals("Game not finished")) {
            if (markAI == X) {
                return field.getGameState().equals("X wins") ? 10 - depth :
                        field.getGameState().equals("O wins") && depth == 1 ? -20 :
                          field.getGameState().equals("O wins") ? depth - 10 : 0;

            } else {
                return field.getGameState().equals("O wins") ? 10 - depth :
                        field.getGameState().equals("X wins") && depth == 1 ? -20 :
                          field.getGameState().equals("X wins") ? depth - 10 : 0;

            }
        }

        Mark mark;
        depth += 1;

        // If maximizing..
        if (isMax) {
            // Set a "worst case" score (unattainably LOW).
            int maxScore = Integer.MIN_VALUE;
            // Switch marks depending on the AIs mark.
            // We have to use the AIs mark here.
            mark = markAI == X ? X : O;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field.available(i, j)) {
                        field.setMark(i, j, mark);
                        // Set new maxScore if miniMax results in a higher score
                        // We want to get the highest possible score -> maximizing.
                        int score = miniMax(field, depth, alpha, beta, false, markAI);
                        maxScore = Math.max(score, maxScore);
                        alpha = Math.max(alpha, score);
                        field.setMark(i, j, BLANK);
                        if (alpha >= beta) {
                            return maxScore;
                        }
                    }
                }
            }
            return maxScore;
        // If minimizing..
        } else {
            // Set a "worst case" score (unattainably HIGH).
            int minScore = Integer.MAX_VALUE;
            // Switch marks depending the AIs mark.
            // In this case we need to use the opponents mark.
            mark = markAI == X ? O : X;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field.available(i, j)) {
                        field.setMark(i, j, mark);
                        // Set new minScore if miniMax results in a lower score.
                        // We want to get the lowest possible score -> minimizing.
                        int score = miniMax(field, depth, alpha, beta, true, markAI);
                        minScore = Math.min(score, minScore);
                        beta = Math.min(beta, score);
                        field.setMark(i, j, BLANK);
                        if (beta <= alpha) {
                            return minScore;
                        }
                    }
                }
            }
            return minScore;
        }
    }

    /**
     * Handles executing the correct method matching the provided difficulty.
     * @param field current instance of the game.
     * @param markAI AIs mark.
     * @param difficulty difficulty level of the move to be computed.
     * @return Int[] containing move.
     */
     public static int[] getMoveByDifficulty(Field field, Mark markAI, int difficulty) {
        switch (difficulty) {
            case 1:
                return easyMove(field, markAI);
            case 2:
                return mediumMove(field, markAI);
            case 3:
                return hardMove(field, markAI);
            default:
                System.out.println("Unsupported AI difficulty.");
                return new int[] {-1, -1};
        }
    }

    /**
     * At the start of the game, there are multiple valid moves which are equally optimal.
     * These are one the 4 corners present on the board. To add some variability, this
     * method picks one of these moves randomly. Otherwise the Minimax algorithm will always pick
     * the first corner which may get a bit boring. This behaviour is explained by the 4 corners having
     * an equally high score, so the initially set highest score of the first corner never gets overwritten
     * (The Minimax algorithm goes cell by cell, starting at the top-left cell).
     * @return
     */
    public static int[] getFirstHardMove() {
        int[][] validFirstMoves = { {0,0}, {0,2}, {2,0}, {2,2} };
        int pickMove = random.nextInt(4);
        return new int[] {validFirstMoves[pickMove][0], validFirstMoves[pickMove][1]};
    }
}
