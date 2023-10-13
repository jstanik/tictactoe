package bakeit.club.tictactoe.net;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.PlayersResult;
import bakeit.club.tictactoe.game.Position;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageReader {

  public Message read(byte[] data) {
    ByteBuffer buffer = ByteBuffer.wrap(data);

    return switch (buffer.get()) {
      case 0 -> readGameStarted(buffer);
      case 1 -> readJoinGame(buffer);
      case 2 -> readMakeMove(buffer);
      case 3 -> readMove(buffer);
      case 4 -> readMoveAccepted(buffer);
      case 5 -> readGameEnded(buffer);
      case 6 -> readInvalidMove(buffer);
      case 7 -> readWaitOpponentsMove(buffer);
      default -> throw new IllegalStateException("Unexpected message code: " + buffer.get());
    };
  }

  private GameStarted readGameStarted(ByteBuffer buffer) {
    Marker marker = readMarker(buffer);
    String opponentName = readString(buffer);
    return new GameStarted(marker, opponentName);
  }

  private JoinGame readJoinGame(ByteBuffer buffer) {
    String playerName = readString(buffer);
    return new JoinGame(playerName);
  }

  private MakeMove readMakeMove(ByteBuffer buffer) {
    long token = buffer.getLong();
    BoardState boardState = readBoardState(buffer);
    return new MakeMove(token, boardState);
  }

  private Move readMove(ByteBuffer buffer) {
    Position position = readPosition(buffer);
    long token = buffer.getLong();
    return new Move(position, token);
  }

  private MoveAccepted readMoveAccepted(ByteBuffer buffer) {
    Position position = readPosition(buffer);
    BoardState boardState = readBoardState(buffer);
    return new MoveAccepted(position, boardState);
  }

  private GameEnded readGameEnded(ByteBuffer buffer) {
    PlayersResult result = readResult(buffer);
    BoardState boardState = readBoardState(buffer);

    return new GameEnded(result, boardState);
  }

  private InvalidMove readInvalidMove(ByteBuffer buffer) {
    Position position = readPosition(buffer);
    Marker marker = readMarker(buffer);
    String errorMessage = readString(buffer);

    return new InvalidMove(
        position,
        marker,
        errorMessage
    );
  }

  private WaitOpponentsMove readWaitOpponentsMove(ByteBuffer buffer) {
    return new WaitOpponentsMove(readBoardState(buffer));
  }

  private String readString(ByteBuffer buffer) {
    int length = buffer.getInt();
    byte[] data = new byte[length];
    buffer.get(data);
    return new String(data, StandardCharsets.UTF_8);
  }

  Marker readMarker(ByteBuffer buffer) {
    byte markerCode = buffer.get();
    return switch (markerCode) {
      case 0 -> Marker.X;
      case 1 -> Marker.O;
      case 2 -> Marker.EMPTY;
      default -> throw new IllegalArgumentException("Unsupported marker code: " + markerCode);
    };
  }

  PlayersResult readResult(ByteBuffer buffer) {
    byte resultCode = buffer.get();
    return switch (resultCode) {
      case 0 -> PlayersResult.VICTORY;
      case 1 -> PlayersResult.DEFEAT;
      case 2 -> PlayersResult.DRAW;
      default -> throw new IllegalArgumentException("Unsupported game result code: " + resultCode);
    };
  }

  private BoardState readBoardState(ByteBuffer buffer) {
    Marker[] cells = new Marker[9];

    for (int i = 0; i < cells.length; i++) {
      cells[i] = readMarker(buffer);
    }

    return BoardState.of(cells);
  }

  private Position readPosition(ByteBuffer buffer) {
    int row = buffer.getInt();
    int column = buffer.getInt();

    return new Position(row, column);
  }
}
