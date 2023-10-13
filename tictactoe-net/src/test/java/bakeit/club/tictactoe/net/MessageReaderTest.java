package bakeit.club.tictactoe.net;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.PlayersResult;
import bakeit.club.tictactoe.game.Position;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class MessageReaderTest {

  private static final SecureRandom RANDOM = new SecureRandom();

  MessageWriter writer = new MessageWriter();
  MessageReader cut = new MessageReader();

  static Stream<Message> read() {
    return Stream.of(
        new GameStarted(Marker.X, "Max"),
        new JoinGame("Filip"),
        new MakeMove(RANDOM.nextLong(), new BoardState(
            Marker.O, Marker.X, Marker.EMPTY,
            Marker.X, Marker.O, Marker.EMPTY,
            Marker.X, Marker.X, Marker.EMPTY
        )),
        new Move(
            new Position(
                RANDOM.nextInt(0, 3),
                RANDOM.nextInt(0, 3)
            ),
            RANDOM.nextLong()
        ),
        new MoveAccepted(new Position(2, 1), new BoardState(
            Marker.O, Marker.X, Marker.EMPTY,
            Marker.X, Marker.O, Marker.EMPTY,
            Marker.X, Marker.X, Marker.O
        )),
        new GameEnded(PlayersResult.DEFEAT, new BoardState(
            Marker.O, Marker.X, Marker.EMPTY,
            Marker.X, Marker.O, Marker.EMPTY,
            Marker.X, Marker.X, Marker.O
        )),
        new InvalidMove(
            new Position(
                RANDOM.nextInt(0, 3),
                RANDOM.nextInt(0, 3)
            ),
            Marker.O,
            "Invalid move"
        )
    );
  }

  @ParameterizedTest
  @MethodSource
  void read(Message message) {
    byte[] data = writer.write(message);

    Message actual = cut.read(data);

    assertEquals(message, actual);
  }

  @ParameterizedTest
  @EnumSource(Marker.class)
  void readMarker(Marker marker) {
    byte markerCode = writer.toMarkerCode(marker);
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(markerCode);
    buffer.flip();

    Marker actual = cut.readMarker(buffer);

    assertEquals(marker, actual);

  }

  @ParameterizedTest
  @EnumSource(PlayersResult.class)
  void readResult(PlayersResult result) {
    byte markerCode = writer.toResultCode(result);
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(markerCode);
    buffer.flip();

    PlayersResult actual = cut.readResult(buffer);

    assertEquals(result, actual);

  }

}