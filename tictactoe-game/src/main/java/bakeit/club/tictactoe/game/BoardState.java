package bakeit.club.tictactoe.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record BoardState(List<Marker> cells) {

  public static BoardState empty() {
    return new BoardState(List.of(
        Marker.EMPTY, Marker.EMPTY, Marker.EMPTY,
        Marker.EMPTY, Marker.EMPTY, Marker.EMPTY,
        Marker.EMPTY, Marker.EMPTY, Marker.EMPTY)
    );
  }

  public static BoardState of(Marker[] cells) {
    return new BoardState(Arrays.asList(cells));
  }

  public static BoardState of(Board board) {
    List<Marker> cells = new ArrayList<>(9);
    for (int i = 0; i < 9; i++) {
      cells.add(board.getMarker(new Position(i / 3, i % 3)));
    }

    return new BoardState(cells);
  }

  public BoardState {
    cells = List.copyOf(cells);
  }

  public BoardState(
      Marker m0, Marker m1, Marker m2,
      Marker m3, Marker m4, Marker m5,
      Marker m6, Marker m7, Marker m8) {
    this(List.of(
        m0, m1, m2, m3, m4, m5, m6, m7, m8
    ));
  }

  public Marker getMarker(Position position) {
    return cells.get(position.row() * 3 + position.column());
  }

}
