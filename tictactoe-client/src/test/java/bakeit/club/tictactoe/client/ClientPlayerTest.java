package bakeit.club.tictactoe.client;

import static bakeit.club.tictactoe.game.Marker.EMPTY;
import static bakeit.club.tictactoe.game.Marker.O;
import static bakeit.club.tictactoe.game.Marker.X;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.Player;
import bakeit.club.tictactoe.game.PlayerGameInfo;
import bakeit.club.tictactoe.game.PlayersResult;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
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

class ClientPlayerTest {

  private static final SecureRandom RANDOM = new SecureRandom();

  ServerSocket serverSocket;

  volatile RemotePlayerMock mock;
  AssertionPlayer player = new AssertionPlayer("John");

  ClientPlayer cut;

  @BeforeEach
  void beforeEach() throws Exception {
    serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();
    mock = new RemotePlayerMock(serverSocket);
    cut = new ClientPlayer(player, new InetSocketAddress("localhost", port));

  }

  @AfterEach
  void afterEach() throws Exception {
    mock.verify();
    serverSocket.close();
    cut.close();
  }

  @Test
  void play() {
    long token1 = RANDOM.nextLong(0, Long.MAX_VALUE);
    long token2 = RANDOM.nextLong(0, Long.MAX_VALUE);
    BoardState initialBoardState = BoardState.empty();

    BoardState finalBoardState = new BoardState(
        EMPTY, X, X, O, O, O, X, X, EMPTY
    );

    mock.expect(new JoinGame("John"));
    mock.send(new GameStarted(Marker.O, "Filip"));
    mock.send(new WaitOpponentsMove(initialBoardState));
    mock.send(new MakeMove(token1, initialBoardState));
    mock.expect(new Move(new Position(1, 1), token1));
    mock.send(new InvalidMove(new Position(1, 1), Marker.O, "Invlid move!"));
    mock.send(new MakeMove(token2, initialBoardState));
    mock.expect(new Move(new Position(1, 1), token2));
    mock.send(new MoveAccepted(new Position(1, 1), finalBoardState));
    mock.send(new GameEnded(PlayersResult.VICTORY, finalBoardState));
    mock.start();

    player.positions = List.of(new Position(1, 1), new Position(1, 1));
    cut.play();

    assertEquals(initialBoardState, player.waitOpponentsMoveBoardState);
    assertEquals(new Position(1, 1), player.acceptedPosition);
    assertEquals(finalBoardState, player.acceptedPositionBoardState);
    assertEquals(new PlayerGameInfo(O, "Filip"), player.playerGameInfo);
    assertEquals(finalBoardState, player.boardState);
    assertEquals(PlayersResult.VICTORY, player.result);
  }

}

class AssertionPlayer implements Player {

  private final String name;

  PlayerGameInfo playerGameInfo;
  BoardState boardState;
  PlayersResult result;
  int position = 0;
  Position acceptedPosition;
  BoardState acceptedPositionBoardState;
  List<Position> positions;
  BoardState waitOpponentsMoveBoardState;

  public AssertionPlayer(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void gameStarted(PlayerGameInfo info) {
    this.playerGameInfo = info;
  }

  @Override
  public void waitOpponentsMove(BoardState boardState) {
    this.waitOpponentsMoveBoardState = boardState;
  }

  @Override
  public Position placeMarker(BoardState boardState) {
    return positions.get(position++);
  }

  @Override
  public void placementAccepted(Position position, BoardState boardState) {
    this.acceptedPosition = position;
    this.acceptedPositionBoardState = boardState;
  }

  @Override
  public void placementRejected(Position position, String reason) {

  }

  @Override
  public void gameEnded(BoardState boardState, PlayersResult result) {
    this.boardState = boardState;
    this.result = result;
  }
}

class RemotePlayerMock {

  private final List<Action> actions = new ArrayList<>();
  private final MessageWriter writer = new MessageWriter();
  private final MessageReader reader = new MessageReader();
  private final ServerSocket serverSocket;
  private final CompletableFuture<Void> completed = new CompletableFuture<>();

  public RemotePlayerMock(ServerSocket serverSocket) {
    this.serverSocket = serverSocket;
  }

  void expect(Message message) {
    actions.add(new Receive(message));
  }

  void send(Message message) {
    actions.add(new Send(message));
  }

  void react(Function<Message, Message> reactor) {
    actions.add(new React(reactor));
  }

  public void start() {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.execute(() -> {
      DataOutputStream output;
      DataInputStream input;
      try (Socket socket = serverSocket.accept()) {
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());

        try {
          actions.forEach(action -> {
            action.execute(input, reader, output, writer);
          });
          completed.complete(null);
        } catch (Throwable ex) {
          completed.completeExceptionally(ex);
        }
      } catch (IOException ex) {
        throw new UncheckedIOException(ex);
      } finally {
        executor.shutdown();
      }
    });
  }

  public void verify() {
    try {
      completed.get(10, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}

interface Action {

  void execute(DataInputStream input, MessageReader reader, DataOutputStream output,
      MessageWriter writer);
}

record Send(Message message) implements Action {

  @Override
  public void execute(
      DataInputStream input,
      MessageReader reader,
      DataOutputStream output,
      MessageWriter writer
  ) {
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

record Receive(Message expectedMessage) implements Action {

  @Override
  public void execute(
      DataInputStream input,
      MessageReader reader,
      DataOutputStream output,
      MessageWriter writer
  ) {
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

record React(Function<Message, Message> reactor) implements Action {

  @Override
  public void execute(DataInputStream input, MessageReader reader, DataOutputStream output,
      MessageWriter writer) {
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