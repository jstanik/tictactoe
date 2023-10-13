package bakeit.club.tictactoe.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayersResultTest {


  @ParameterizedTest
  @CsvSource({
      "VICTORY,DEFEAT", "DEFEAT,VICTORY", "DRAW,DRAW"
  })
  void invert(PlayersResult source, PlayersResult expected) {
    PlayersResult actual = source.invert();

    assertEquals(expected, actual);
  }
}