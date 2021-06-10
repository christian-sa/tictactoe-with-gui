package game;

import java.util.Scanner;
import java.util.stream.IntStream;

import static game.Mark.*;

/**
 * Resembles an instance of the game.
 */
public class Field {

    private char[][] field = {{' ', ' ', ' '}, {' ', ' ', ' '}, {' ', ' ', ' '}};
    Scanner scan = new Scanner(System.in);

    /**
     * Prints the field in its current state.
     */
    public void print() {
        System.out.println("---------");
        for (char[] row : field) {
            StringBuilder rowBuilder = new StringBuilder("| "); // Outer border.
            for (char cell : row) {
                rowBuilder.append(cell).append(BLANK); // Append the cell + whitespace.
            }
            System.out.println(rowBuilder.append('|')); // Outer border.
        }
        System.out.println("---------");
    }

    /**
     * Used to process user input into int[] with the 2 required coordinates for further actions.
     * @return int[] with coordinates.
     */
    public int[] getCoordinates() {
        boolean gotInput = false;
        int row = 0, column = 0;
        while (!gotInput) {
            System.out.print("Enter the coordinates: ");
            String[] strCoords = scan.nextLine().trim().split(" ");
            try {
                row = Integer.parseInt(strCoords[0]);
                column = Integer.parseInt(strCoords[1]);
            } catch (NumberFormatException e) {
                System.out.println("You should enter numbers!");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Input should contain 2 numbers with a space in between!");
            }
            if (row > 3 || row < 1 || column > 3 || column < 1) {
                System.out.println("Coordinates should be from 1 to 3!");
                continue;
            }
            if (getMark(row - 1, column - 1) != BLANK) {
                System.out.println("This cell is occupied! Choose another one!");
                continue;
            }
            gotInput = true;
        }
        return new int[] {row, column};
    }

    /**
     * This method contains the logic to determine if the win condition is met by the given mark.
     *    2  3  5   Every cell on the game field has a prime number associated with it which is evaluated
     *   *7 11 13*  in the following multiplication if the mark at this cell corresponds with the mark
     *   17 19 23   given to the method otherwise the cell is valued as 1.
     * Using the concept of Prime Factorization, we can then later determine if the given mark won, because
     * all prime numbers in that row had to be multiplied to produce the 8 products obtainable by having a full row.
     * For example having a full horizontal row in the middle (highlighted by *) would produce 1001 by multiplying
     * 7 * 11 * 13.
     * All possible products that would lead to a win: 30, 1001, 7429, 238, 627, 1495, 506, 935.
     * @param c char of the mark that is checked.
     * @return Products of the prime numbers in the 8 possible win conditions.
     */
    private int[] getPrimeProducts(char c) {
        Mark m = c == 'X' ? X : O;
        int[] primeNums = {2, 3, 5, 7, 11, 13, 17, 19, 23};
        int[][] pf = { {1, 1, 1}, {1, 1, 1}, {1, 1, 1} };
        // Iterate over the 9 prime numbers
        for(int i = 0; i < 9; i++) {
            // Iterate over the 3 possible rows
            for (int j = 0; j < 3; j++) {
                // Iterate over the 3 possible columns
                for (int k = 0; k < 3; k++, i++) {
                    if (getMark(j, k) == m) {
                        pf[j][k] = primeNums[i];
                    }
                }
            }
        }
        return new int[] {pf[0][0] * pf[0][1] * pf[0][2], pf[1][0] * pf[1][1] * pf[1][2],
                pf[2][0] * pf[2][1] * pf[2][2], pf[0][0] * pf[1][0] * pf[2][0],
                pf[0][1] * pf[1][1] * pf[2][1], pf[0][2] * pf[1][2] * pf[2][2],
                pf[0][0] * pf[1][1] * pf[2][2], pf[0][2] * pf[1][1] * pf[2][0]};
    }

    /**
     * Checks if the given mark won by evaluating the prime products from {@link #getPrimeProducts(char)}.
     * @param m mark which is checked.
     * @return true if the given has won.
     */
    public boolean hasWon(Mark m) {
        // Stream for checking if the specified mark has met the win condition.
        // If the mark has a full row of any kind, one of the products of prime numbers
        // in a full row will match and the method returns true.
        char c = m == X ? 'X' : 'O';
        return IntStream.of(getPrimeProducts(c)).anyMatch
                (x -> x == 30 || x == 1001 || x == 7429 || x == 238 ||
                        x == 627 || x == 1495 || x == 935 || x == 506);
    }

    /**
     * Checks the state/outcome of the game.
     * @return outcome as String.
     */
    public String getGameState() {
        return hasWon(X) ? "X wins" :
                hasWon(O) ? "O wins" :
                        numOfMarks() != 9 ? "Game not finished" : "Draw";
    }

    /**
     * Calls getGameState method but interprets it as boolean for ease of use.
     * @return true if game is in a "Game Over" state.
     */
    public boolean isGameOver() {
        return !getGameState().equals("Game not finished");
    }

    public void setMark(int row, int column, Mark mark) {
        field[row][column] = mark.getMark();
    }

    public Mark getMark(int row, int column) {
        return field[row][column] == X.getMark() ? X :
                field[row][column] == O.getMark() ? O : BLANK;
    }

    /**
     * Determines how many Xs and Os are present on the game field.
     * @return number of Xs and Os.
     */
    public int numOfMarks() {
        int countX = 0, countO = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (getMark(i, j) == X) {
                    countX++;
                } else if (getMark(i, j) == O) {
                    countO++;
                }
            }
        }
        return countX + countO;
    }

    // Checks if the given cell is a valid move (must be blank).
    public boolean available(int i, int j) {
        return field[i][j] == BLANK.getMark();
    }

    /**
     * Check which mark makes current turn.
     * @return true if its Xs turn.
     */
    public boolean isXsTurn() {
        return numOfMarks() % 2 == 0;
    }
}

