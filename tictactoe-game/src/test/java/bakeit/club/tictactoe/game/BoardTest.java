package bakeit.club.tictactoe.game;

import static bakeit.club.tictactoe.game.Marker.EMPTY;
import static bakeit.club.tictactoe.game.Marker.O;
import static bakeit.club.tictactoe.game.Marker.X;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class BoardTest {

  private List<Position> allPositions() {
    List<Position> positions = new ArrayList<>(9);

    for (int row = 0; row < 3; row++) {
      for (int column = 0; column < 3; column++) {
        positions.add(new Position(row, column));
      }
    }

    return positions;
  }

  @Test
  void newBoardIsEmpty() {
    Board cut = new Board();

    allPositions().forEach(p -> assertEquals(EMPTY, cut.getMarker(p)));
  }

  @Test
  void placeMarker_forbidAddingEmptyMarker() {
    Board cut = new Board();

    allPositions().forEach(p -> {
      InvalidMove actual = assertThrows(InvalidMove.class, () -> cut.placeMarker(p, EMPTY));
      assertEquals(actual.getMessage(), "Cannot empty a cell.");
    });
  }

  @Test
  void placeMarker_forbidPlacingMarkerOnNonEmptyPlace() {
    Board cut = new Board();
    Position position = new Position(0, 2);
    cut.placeMarker(position, X);
    InvalidMove actual = assertThrows(InvalidMove.class, () -> cut.placeMarker(position, O));
    assertEquals(actual.getMessage(), "Position [0, 2] already contains a marker X");
  }

  @ParameterizedTest
  @EnumSource(value = Marker.class, mode = Mode.EXCLUDE, names = "EMPTY")
  void getMarker(Marker marker) {
    Board cut = new Board();

    allPositions().forEach(position -> {
          cut.placeMarker(position, marker);
          assertEquals(marker, cut.getMarker(position));
        }
    );
  }

}