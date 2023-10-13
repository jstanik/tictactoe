package bakeit.club.tictactoe.game;

import static bakeit.club.tictactoe.game.Marker.EMPTY;
import static java.util.Objects.requireNonNull;

/**
 * A tic-tac-toe board. It has three rows and three columns indexed from zero. The positions on the
 * board have the following coordinates:
 * <pre>
 *   [0, 0] [0, 1] [0, 2]
 *   [1, 0] [1, 1] [1, 2]
 *   [2, 0] [2, 1] [2, 2]
 * </pre>
 */
public class Board {

  private final Marker[] cells = {
      EMPTY, EMPTY, EMPTY,
      EMPTY, EMPTY, EMPTY,
      EMPTY, EMPTY, EMPTY
  };

  /**
   * Places a marker to the specified position on the board. Markers can be placed only into empty
   * cells.
   *
   * @param position  the position on the board
   * @param marker the marker to place
   * @throws InvalidMove If the move is invalid.
   */
  public void placeMarker(Position position, Marker marker) {
    int index = toIndex(position);

    requireNonNull(marker);

    if (marker == EMPTY) {
      throw new InvalidMove("Cannot empty a cell.");
    }

    if (cells[index] != EMPTY) {
      throw new InvalidMove(
          "Position [" + position.row() + ", " + position.column() + "] already contains a marker " + cells[index]);
    }

    cells[index] = marker;
  }

  /**
   * Gets a marker at a given position.
   *
   * @param position the position on the board.
   * @return the marker at the position {@code [row, column]}.
   */
  public Marker getMarker(Position position) {
    return cells[toIndex(position)];
  }

  private int toIndex(Position position) {
    return 3 * position.row() + position.column();
  }

}
