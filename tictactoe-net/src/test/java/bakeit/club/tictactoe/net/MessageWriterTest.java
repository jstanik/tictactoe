package bakeit.club.tictactoe.net;

import static bakeit.club.tictactoe.game.Marker.EMPTY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.PlayersResult;
import bakeit.club.tictactoe.game.Position;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MessageWriterTest {

  SecureRandom random = new SecureRandom();
  MessageWriter cut = new MessageWriter();

  @Test
  void writeAssignedMarker() {
    String opponentName = "Alex";
    byte[] opponentNameData = opponentName.getBytes(StandardCharsets.UTF_8);
    GameStarted message = new GameStarted(Marker.X, opponentName);

    byte[] actual = cut.write(message);

    assertArrayEquals(actual, new byte[]{
        0, // GameStarted
        0, // X marker
        0, 0, 0, 4,
        opponentNameData[0], opponentNameData[1], opponentNameData[2], opponentNameData[3]
    });
  }

  @Test
  void writeGameEnded() {
    Marker[] board = {
        Marker.O, Marker.O, Marker.O,
        Marker.X, Marker.X, EMPTY,
        Marker.X, EMPTY, EMPTY
    };
    GameEnded message = new GameEnded(
        PlayersResult.VICTORY,
        BoardState.of(board)
    );

    byte[] actual = cut.write(message);

    assertArrayEquals(actual, new byte[]{
        5, // game ended
        0, // victory
        1, 1, 1, 0, 0, 2, 0, 2, 2 // the board state
    });
  }

  @Test
  void writeWaitOpponentsMove() {
    byte[] actual = cut.write(new WaitOpponentsMove(
        new BoardState(EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY)
    ));
    assertArrayEquals(new byte[]{7, 2, 2, 2, 2, 2, 2, 2, 2, 2}, actual);
  }

  @ParameterizedTest
  @CsvSource({
      "X,0", "O,1", "EMPTY,2"
  })
  void toMarkerCode(Marker source, byte expected) {
    byte actual = cut.toMarkerCode(source);

    assertEquals(actual, expected);
  }

  @Test
  void writeJoinGame() {
    JoinGame message = new JoinGame("Bob");
    byte[] nameData = message.playerName().getBytes(StandardCharsets.UTF_8);

    byte[] actual = cut.write(message);

    assertArrayEquals(actual, new byte[]{
        1, // JoinGame
        0, 0, 0, (byte) nameData.length,
        nameData[0], nameData[1], nameData[2]
    });
  }

  @Test
  void writeMakeMove() {
    long token = random.nextLong();
    MakeMove message = new MakeMove(token,
        new BoardState(
            EMPTY, EMPTY, EMPTY,
            EMPTY, Marker.X, EMPTY,
            Marker.O, EMPTY, EMPTY
        ));

    byte[] actual = cut.write(message);
    assertArrayEquals(actual, new byte[]{
        2,
        (byte) (token >> 7 * 8),
        (byte) (token >> 6 * 8),
        (byte) (token >> 5 * 8),
        (byte) (token >> 4 * 8),
        (byte) (token >> 3 * 8),
        (byte) (token >> 2 * 8),
        (byte) (token >> 1 * 8),
        (byte) token,
        2, 2, 2, 2, 0, 2, 1, 2, 2
    });
  }

  @Test
  void writeMove() {
    long token = random.nextLong();
    Move message = new Move(new Position(2, 1), token);

    byte[] actual = cut.write(message);

    assertArrayEquals(actual, new byte[]{
        3,
        0, 0, 0, 2,
        0, 0, 0, 1,
        (byte) (token >> 7 * 8),
        (byte) (token >> 6 * 8),
        (byte) (token >> 5 * 8),
        (byte) (token >> 4 * 8),
        (byte) (token >> 3 * 8),
        (byte) (token >> 2 * 8),
        (byte) (token >> 1 * 8),
        (byte) token,
    });
  }

  @Test
  void writeMoveAccepted() {
    MoveAccepted message = new MoveAccepted(new Position(1, 1),
        new BoardState(
            EMPTY, EMPTY, EMPTY,
            EMPTY, Marker.X, EMPTY,
            EMPTY, EMPTY, EMPTY)
    );

    byte[] actual = cut.write(message);

    assertArrayEquals(actual, new byte[]{
        4, // MoveAccepted
        0, 0, 0, 1,
        0, 0, 0, 1,
        2, 2, 2, 2, 0, 2, 2, 2, 2
    });
  }

  @ParameterizedTest
  @CsvSource({
      "VICTORY,0", "DEFEAT,1", "DRAW,2"
  })
  void toTestResultCode(PlayersResult result, byte expected) {
    byte actual = cut.toResultCode(result);
    assertEquals(actual, expected);
  }

  @Test
  void writeInvalidMove() {
    InvalidMove message = new InvalidMove(new Position(0, 2), Marker.X, "msg");
    byte[] data = message.message().getBytes(StandardCharsets.UTF_8);

    byte[] actual = cut.write(message);

    assertArrayEquals(actual, new byte[]{
        6, // InvalidMove
        0, 0, 0, 0, // row
        0, 0, 0, 2, // column
        0, // marker
        0, 0, 0, (byte) data.length,
        data[0], data[1], data[2]
    });
  }
}