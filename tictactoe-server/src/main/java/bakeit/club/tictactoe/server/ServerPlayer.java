package bakeit.club.tictactoe.server;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.Player;
import bakeit.club.tictactoe.game.PlayerGameInfo;
import bakeit.club.tictactoe.game.PlayersResult;
import bakeit.club.tictactoe.game.Position;
import bakeit.club.tictactoe.net.JoinGame;
import bakeit.club.tictactoe.net.GameStarted;
import bakeit.club.tictactoe.net.GameEnded;
import bakeit.club.tictactoe.net.InvalidMove;
import bakeit.club.tictactoe.net.MakeMove;
import bakeit.club.tictactoe.net.Message;
import bakeit.club.tictactoe.net.MessageReader;
import bakeit.club.tictactoe.net.MessageWriter;
import bakeit.club.tictactoe.net.Move;
import bakeit.club.tictactoe.net.MoveAccepted;
import bakeit.club.tictactoe.net.WaitOpponentsMove;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.security.SecureRandom;

class ServerPlayer implements Player, AutoCloseable {

  private static final SecureRandom RANDOM = new SecureRandom();

  private String name;
  private final Socket socket;
  private final MessageWriter messageWriter = new MessageWriter();
  private final MessageReader messageReader = new MessageReader();
  private final DataOutputStream dataOutputStream;
  private final DataInputStream dataInputStream;
  private Marker marker;

  ServerPlayer(
      Socket socket
  ) {
    this.socket = socket;
    try {
      this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
      this.dataInputStream = new DataInputStream(socket.getInputStream());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void readName() {
    JoinGame joinGame = readMessage(JoinGame.class);
    name = joinGame.playerName();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void gameStarted(PlayerGameInfo info) {
    this.marker = info.assignedMarker();
    writeMessage(new GameStarted(marker, info.opponentsName()));
  }

  @Override
  public void waitOpponentsMove(BoardState boardState) {
    writeMessage(new WaitOpponentsMove(boardState));
  }

  @Override
  public Position placeMarker(BoardState boardState) {

    for (int attempt = 1; attempt < 3; attempt++) {
      final long token = RANDOM.nextLong(1, Long.MAX_VALUE);
      MakeMove message = new MakeMove(token, boardState);
      writeMessage(message);
      Move move = readMessage(Move.class);

      if (move.token() == token) {
        return move.position();
      }

      writeMessage(new InvalidMove(move.position(), marker,
          "The returned token " + move.token() + " is invalid."));
    }

    throw new IllegalStateException("No valid move received after 3 attempts");
  }

  @Override
  public void placementAccepted(Position position, BoardState boardState) {
    writeMessage(new MoveAccepted(position, boardState));
  }

  @Override
  public void placementRejected(Position position, String reason) {
    writeMessage(new InvalidMove(position, marker, reason));
  }

  @Override
  public void gameEnded(BoardState boardState, PlayersResult result) {
    writeMessage(
        new GameEnded(
            result,
            boardState
        )
    );
  }

  @Override
  public void close() throws IOException {
    socket.close();
  }

  private void writeMessage(Message message) {
    try {
      byte[] data = messageWriter.write(message);
      dataOutputStream.writeInt(data.length);
      dataOutputStream.write(data);
      dataOutputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private <T extends Message> T readMessage(Class<T> type) {
    try {
      int frameLength = dataInputStream.readInt();
      byte[] data = new byte[frameLength];
      dataInputStream.readFully(data);

      Message message = messageReader.read(data);

      if (type.isInstance(message)) {
        return type.cast(message);
      } else {
        throw new IllegalStateException(
            "Expected '" + type + "' but '" + message.getClass() + "' was read from the socket.");
      }
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
