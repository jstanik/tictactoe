package bakeit.club.tictactoe.server;

import static bakeit.club.tictactoe.game.Marker.EMPTY;
import static bakeit.club.tictactoe.game.Marker.O;
import static bakeit.club.tictactoe.game.Marker.X;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.PlayerGameInfo;
import bakeit.club.tictactoe.game.PlayersResult;
import bakeit.club.tictactoe.game.Position;
import bakeit.club.tictactoe.net.GameEnded;
import bakeit.club.tictactoe.net.GameStarted;
import bakeit.club.tictactoe.net.InvalidMove;
import bakeit.club.tictactoe.net.JoinGame;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ServerPlayerTest {

  ServerSocket serverSocket;

  volatile RemotePlayerMock mock;

  ServerPlayer cut;

  @BeforeEach
  void beforeEach() throws Exception {
    serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();

    Thread thread = new Thread(() -> {
      Socket socket;
      try {
        socket = new Socket((String) null, port);
      } catch (IOException ex) {
        throw new UncheckedIOException(ex);
      }

      mock = new RemotePlayerMock(socket);
    });
    thread.start();
    cut = new ServerPlayer(serverSocket.accept());
    thread.join();

  }

  @AfterEach
  void afterEach() throws Exception {
    mock.verify();
    mock.close();
    serverSocket.close();
    cut.close();
  }

  @Test
  @Timeout(5)
  void playTheGame() {
    BoardState initialBoardState = new BoardState(EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
        EMPTY, EMPTY, EMPTY);

    mock.send(new JoinGame("John"));
    GameStarted gameStarted = new GameStarted(Marker.O, "Filip");
    mock.expect(gameStarted);
    mock.expect(new WaitOpponentsMove(initialBoardState));
    mock.react(m -> {
      MakeMove makeMove = (MakeMove) m;
      return new Move(new Position(1, 2), -1);
    });
    mock.expect(
        new InvalidMove(
            new Position(1, 2),
            O,
            "The returned token -1 is invalid."
        )
    );
    mock.react(m -> {
      MakeMove makeMove = (MakeMove) m;
      return new Move(new Position(0, 0), makeMove.token());
    });
    mock.expect(
        new InvalidMove(
            new Position(0, 0),
            O,
            "The cell was not free"
        )
    );
    mock.react(m -> {
      MakeMove makeMove = (MakeMove) m;
      return new Move(new Position(1, 2), makeMove.token());
    });
    BoardState finalBoardState = new BoardState(EMPTY, X, X, O, O, O, X, X, EMPTY);
    mock.expect(new MoveAccepted(new Position(1, 2), finalBoardState));
    mock.expect(new GameEnded(PlayersResult.VICTORY, finalBoardState));
    mock.start();

    cut.readName();
    cut.gameStarted(new PlayerGameInfo(gameStarted.assignedMarker(), "Filip"));
    cut.waitOpponentsMove(initialBoardState);

    BoardState boardState = BoardState.empty();

    Position position = cut.placeMarker(boardState);
    // invalid token reported automatically
    cut.placementRejected(position, "The cell was not free");
    position = cut.placeMarker(boardState);
    cut.placementAccepted(position, finalBoardState);

    assertEquals(1, position.row());
    assertEquals(2, position.column());

    cut.gameEnded(finalBoardState, PlayersResult.VICTORY);
  }

}

class RemotePlayerMock implements AutoCloseable {

  private List<Runnable> actions = new ArrayList<>();
  private final MessageWriter writer = new MessageWriter();
  private final MessageReader reader = new MessageReader();
  private final Socket socket;
  private final DataOutputStream output;
  private final DataInputStream input;
  private final CompletableFuture<Void> completed = new CompletableFuture<>();

  public RemotePlayerMock(Socket socket) {
    this.socket = socket;
    try {
      this.output = new DataOutputStream(socket.getOutputStream());
      this.input = new DataInputStream(socket.getInputStream());
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  void expect(Message message) {
    actions.add(new Receive(message, input, reader));
  }

  void send(Message message) {
    actions.add(new Send(message, output, writer));
  }

  void react(Function<Message, Message> reactor) {
    actions.add(new React(reactor, input, output, reader, writer));
  }

  @Override
  public void close() throws Exception {
    socket.close();
  }

  public void start() {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.execute(() -> {
      try {
        actions.forEach(action -> {
          action.run();
        });
        completed.complete(null);
      } catch (Throwable ex) {
        completed.completeExceptionally(ex);
      } finally {
        executor.shutdown();
      }
    });
  }

  public void verify() {
    try {
      completed.get(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}

record Send(Message message, DataOutputStream output, MessageWriter writer) implements Runnable {

  @Override
  public void run() {
    byte[] data = writer.write(message);
    try {
      output.writeInt(data.length);
      output.write(data);
      output.flush();
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}

record Receive(Message expectedMessage, DataInputStream input, MessageReader reader) implements
    Runnable {

  @Override
  public void run() {
    try {
      int frameSize = input.readInt();
      byte[] data = new byte[frameSize];
      input.readFully(data);
      Message actualMessage = reader.read(data);
      System.out.println(
          Thread.currentThread().getName() + ": Mock : Message received: " + actualMessage);
      assertEquals(expectedMessage, actualMessage);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}

record React(
    Function<Message, Message> reactor,
    DataInputStream input,
    DataOutputStream output,
    MessageReader reader,
    MessageWriter writer
) implements Runnable {

  @Override
  public void run() {
    try {
      int frameSize = input.readInt();
      byte[] data = new byte[frameSize];
      input.readFully(data);
      Message receivedMessage = reader.read(data);
      System.out.println(
          Thread.currentThread().getName() + ": Mock : Message received: " + receivedMessage);

      Message transformed = reactor.apply(receivedMessage);

      byte[] outputData = writer.write(transformed);
      output.writeInt(outputData.length);
      output.write(outputData);
      output.flush();

    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}