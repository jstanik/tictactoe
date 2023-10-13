package bakeit.club.tictactoe.game;

/**
 * A position on the board.
 *
 * @param row the zero based row index
 * @param column the zero based column index
 */
public record Position(int row, int column) {
  public Position {
    if (row < 0 || row > 2) {
      throw new IllegalArgumentException("Value 'row=" + row + "' is out of bounds <0, 2>");
    }

    if (column < 0 || column > 2) {
      throw new IllegalArgumentException("Value 'column=" + column + "' is out of bounds <0, 2>");
    }
  }

}
