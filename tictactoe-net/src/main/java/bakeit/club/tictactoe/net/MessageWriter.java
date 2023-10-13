package bakeit.club.tictactoe.net;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.PlayersResult;
import bakeit.club.tictactoe.game.Position;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageWriter {

  public byte[] write(Message message) {
    return switch (message) {
      case GameStarted gameStarted -> write(gameStarted);
      case GameEnded gameEnded -> write(gameEnded);
      case JoinGame joinGame -> write(joinGame);
      case MakeMove makeMove -> write(makeMove);
      case Move move -> write(move);
      case MoveAccepted moveAccepted -> write(moveAccepted);
      case InvalidMove invalidMove -> write(invalidMove);
      case WaitOpponentsMove waitOpponentsMove -> write(waitOpponentsMove);
    };
  }

  private byte[] write(GameStarted gameStarted) {
    BufferableString opponentsName = new BufferableString(gameStarted.opponentName());
    var buffer = ByteBuffer.allocate(
        1 + 1 + opponentsName.size()
    );
    buffer.put(gameStarted.getCode());
    buffer.put(toMarkerCode(gameStarted.assignedMarker()));
    opponentsName.putTo(buffer);

    return buffer.array();
  }

  private byte[] write(JoinGame joinGame) {
    BufferableString playerName = new BufferableString(joinGame.playerName());
    var buffer = ByteBuffer.allocate(
        1 + playerName.size()
    );
    buffer.put(joinGame.getCode());
    playerName.putTo(buffer);
    return buffer.array();
  }

  private byte[] write(MakeMove makeMove) {
    var buffer = ByteBuffer.allocate(1 + 8 + 9);
    buffer.put(makeMove.getCode());
    buffer.putLong(makeMove.token());
    writeBoard(buffer, makeMove.boardState());
    return buffer.array();
  }

  private byte[] write(Move move) {
    var buffer = ByteBuffer.allocate(1 + 4 + 4 + 8);
    buffer.put(move.getCode());
    buffer.putInt(move.position().row());
    buffer.putInt(move.position().column());
    buffer.putLong(move.token());
    return buffer.array();
  }

  private byte[] write(MoveAccepted moveAccepted) {
    var buffer = ByteBuffer.allocate(1 + 4 + 4 + 9);
    buffer.put(moveAccepted.getCode());
    buffer.putInt(moveAccepted.position().row());
    buffer.putInt(moveAccepted.position().column());
    writeBoard(buffer, moveAccepted.boardState());
    return buffer.array();
  }

  byte toResultCode(PlayersResult result) {
    return (byte) (byte) switch (result) {
      case VICTORY -> 0;
      case DEFEAT -> 1;
      case DRAW -> 2;
    };
  }

  private byte[] write(GameEnded gameEnded) {
    var buffer = ByteBuffer.allocate(1 + 1 + 9);
    buffer.put(gameEnded.getCode());
    buffer.put(toResultCode(gameEnded.result()));

    var boardState = gameEnded.boardState();
    writeBoard(buffer, boardState);
    return buffer.array();
  }

  private byte[] write(InvalidMove invalidMove) {
    var message = new BufferableString(invalidMove.message());
    var buffer = ByteBuffer.allocate(1 + 4 + 4 + 1 + message.size());

    buffer.put(invalidMove.getCode());
    writePosition(buffer, invalidMove.position());
    buffer.put(toMarkerCode(invalidMove.marker()));
    message.putTo(buffer);
    return buffer.array();
  }

  private byte[] write(WaitOpponentsMove move) {
    var buffer = ByteBuffer.allocate(1 + 9);
    buffer.put(move.getCode());
    writeBoard(buffer, move.boardState());
    return buffer.array();
  }

  byte toMarkerCode(Marker marker) {
    return (byte) switch (marker) {
      case X -> 0;
      case O -> 1;
      case EMPTY -> 2;
    };
  }

  private void writePosition(ByteBuffer buffer, Position position) {
    buffer.putInt(position.row());
    buffer.putInt(position.column());
  }

  private void writeBoard(ByteBuffer buffer, BoardState boardState) {
    for (int cell = 0; cell < 9; cell++) {
      buffer.put(
          toMarkerCode(
              boardState.getMarker(
                  new Position(
                      cell / 3,
                      cell % 3
                  )
              )
          )
      );
    }
  }

  private record BufferableString(byte[] data) {

    BufferableString(String value) {
      this(value.getBytes(StandardCharsets.UTF_8));
    }

    void putTo(ByteBuffer buffer) {
      buffer.putInt(data.length);
      buffer.put(data);
    }

    int size() {
      return 4 + data.length;
    }
  }

}
