package bakeit.club.tictactoe.client;

import bakeit.club.tictactoe.game.Player;
import bakeit.club.tictactoe.game.PlayerGameInfo;
import bakeit.club.tictactoe.game.Position;
import bakeit.club.tictactoe.net.GameEnded;
import bakeit.club.tictactoe.net.InvalidMove;
import bakeit.club.tictactoe.net.JoinGame;
import bakeit.club.tictactoe.net.MakeMove;
import bakeit.club.tictactoe.net.GameStarted;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class ClientPlayer implements AutoCloseable {

  private final Player delegate;
  private final MessageWriter messageWriter = new MessageWriter();
  private final MessageReader messageReader = new MessageReader();
  private final InetSocketAddress serverAddress;

  private final AtomicReference<Socket> socketReference = new AtomicReference<>();

  private DataOutputStream dataOutputStream;
  private DataInputStream dataInputStream;

  public ClientPlayer(
      Player delegate,
      InetSocketAddress serverAddress
  ) {
    this.delegate = delegate;
    this.serverAddress = serverAddress;

  }

  public void play() {
    connectToServer();

    System.out.println("Joining a game.");
    writeMessage(new JoinGame(delegate.getName()));
    System.out.println("Waiting for the game to start.");
    GameStarted gameStarted = readGameStarted();
    System.out.println(
        "Game started. You will play with the marker '" + gameStarted.assignedMarker() + "'.");
    System.out.println("Your opponent is: " + gameStarted.opponentName() + ".");
    delegate.gameStarted(
        new PlayerGameInfo(gameStarted.assignedMarker(), gameStarted.opponentName()));

    boolean isGameEnded = false;

    while (!isGameEnded) {
      Message message = readMessage();

      switch (message) {
        case MakeMove makeMove -> handleMakeMove(makeMove);
        case InvalidMove invalidMove -> handleInvalidMove(invalidMove);
        case MoveAccepted moveAccepted -> handleMoveAccepted(moveAccepted);
        case WaitOpponentsMove waitOpponentsMove -> handleWaitOpponentsMove(waitOpponentsMove);
        case GameEnded gameEnded -> {
          isGameEnded = true;
          handleGameEnded(gameEnded);
        }
        case null, default ->
            throw new IllegalStateException("Unexpected message read: " + message);
      }
    }
  }

  private void connectToServer() {

    System.out.println("Connecting to " + serverAddress);

    Socket socket = new Socket();
    try {
      socket.connect(serverAddress, 5000);
    } catch (SocketTimeoutException ex) {
      System.out.println("Failed to connect to the game server. Exiting the application.");
      System.exit(1);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }

    socketReference.set(socket);

    System.out.println("Connected!");

    try {
      this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
      this.dataInputStream = new DataInputStream(socket.getInputStream());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void handleMakeMove(MakeMove makeMove) {
    Position position = delegate.placeMarker(makeMove.boardState());
    writeMessage(new Move(position, makeMove.token()));
  }

  private void handleMoveAccepted(MoveAccepted moveAccepted) {
    delegate.placementAccepted(moveAccepted.position(), moveAccepted.boardState());
  }

  private void handleInvalidMove(InvalidMove invalidMove) {
    System.out.println("Server reports 'INVALID MOVE': " + invalidMove.message());
    delegate.placementRejected(invalidMove.position(), invalidMove.message());
  }

  private void handleGameEnded(GameEnded gameEnded) {
    delegate.gameEnded(gameEnded.boardState(), gameEnded.result());
  }

  private void handleWaitOpponentsMove(WaitOpponentsMove waitOpponentsMove) {
    delegate.waitOpponentsMove(waitOpponentsMove.boardState());
  }

  @Override
  public void close() throws IOException {
    Socket socket = socketReference.getAndSet(null);
    if (socket != null) {
      socket.close();
    }
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

  private Message readMessage() {
    try {
      int frameLength = dataInputStream.readInt();
      byte[] data = new byte[frameLength];
      dataInputStream.readFully(data);

      return messageReader.read(data);

    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  private GameStarted readGameStarted() {
    Message message = readMessage();

    if (message instanceof GameStarted gameStarted) {
      return gameStarted;
    } else {
      throw new IllegalStateException(
          "Expected '" + GameStarted.class + "' but '" + message.getClass()
              + "' was read from the socket.");
    }
  }

}
