package game;

/**
 * The marks which are used for playing Tic Tac Toe are {@link #X} and {@link #O}.
 * {@link #BLANK} is a free cell which isn't currently occupied by another mark.
 */
public enum Mark {

    X('X'),
    O('O'),
    BLANK(' ');

    private final char mark;

    Mark(char mark) {
        this.mark = mark;
    }

    public char getMark() {
        return this.mark;
    }

    @Override
    public String toString() {
        return String.valueOf(mark);
    }

}
