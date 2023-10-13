package bakeit.club.tictactoe.game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameTest {

  @Test
  void playWithWinner() {
    TestPlayer player1 = new TestPlayer(
        "player-1",
        List.of(new Position(1, 1),
            new Position(0, 0),
            new Position(2, 2))
    );
    TestPlayer player2 = new TestPlayer(
        "player-2",
        List.of(new Position(0, 1),
            new Position(0, 2))
    );

    Game cut = new Game(player1, player2);
    cut.play();

    assertEquals(player1.playerGameInfo, new PlayerGameInfo(Marker.X, player2.getName()));
    assertEquals(player1.result, PlayersResult.VICTORY);
    assertTrue(player1.moves.isEmpty());
    assertEquals(2, player1.waitOpponentsMove);

    assertEquals(player2.playerGameInfo, new PlayerGameInfo(Marker.O, player1.getName()));
    assertEquals(player2.result, PlayersResult.DEFEAT);
    assertTrue(player2.moves.isEmpty());
    assertEquals(3, player2.waitOpponentsMove);

  }

  @Test
  void playWithInvalidMoveRepeatsMoveRequest() {
    TestPlayer player1 = new TestPlayer(
        "player-1",
        List.of(new Position(1, 1),
            new Position(0, 0),
            new Position(2, 2))
    );
    TestPlayer player2 = new TestPlayer(
        "player-2",
        List.of(
            new Position(0, 1),
            new Position(1, 1), // invalid move that will have to be repeated
            new Position(0, 2))
    );

    Game cut = new Game(player1, player2);
    cut.play();

    assertEquals(player1.playerGameInfo, new PlayerGameInfo(Marker.X, player2.getName()));
    assertEquals(player1.result, PlayersResult.VICTORY);
    assertTrue(player1.moves.isEmpty());

    assertEquals(player2.playerGameInfo, new PlayerGameInfo(Marker.O, player1.getName()));
    assertEquals(player2.result, PlayersResult.DEFEAT);
    assertTrue(player2.moves.isEmpty());

  }

  @Test
  void playWithDraw() {
    TestPlayer player1 = new TestPlayer(
        "player-1",
        List.of(new Position(1, 1),
            new Position(0, 0),
            new Position(1, 0),
            new Position(2, 1),
            new Position(0, 2))
    );
    TestPlayer player2 = new TestPlayer(
        "player-2",
        List.of(new Position(0, 1),
            new Position(1, 2),
            new Position(2, 0),
            new Position(2, 2)
        )
    );

    Game cut = new Game(player1, player2);
    cut.play();

    assertEquals(player1.playerGameInfo, new PlayerGameInfo( Marker.X, player2.getName()));
    assertEquals(player1.result, PlayersResult.DRAW);
    assertTrue(player1.moves.isEmpty());

    assertEquals(player2.playerGameInfo, new PlayerGameInfo(Marker.O, player1.getName()));
    assertEquals(player2.result, PlayersResult.DRAW);
    assertTrue(player2.moves.isEmpty());

  }
}

class TestPlayer implements Player {

  private final String name;
  PlayerGameInfo playerGameInfo;
  List<Position> moves;
  int waitOpponentsMove = 0;

  TestPlayer(String name, List<Position> moves) {
    this.name = name;
    this.moves = new LinkedList<>(moves);
  }

  PlayersResult result;
  BoardState boardState;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void gameStarted(PlayerGameInfo playerGameInfo) {
    this.playerGameInfo = playerGameInfo;
  }

  @Override
  public void waitOpponentsMove(BoardState boardState) {
    waitOpponentsMove++;
  }

  @Override
  public Position placeMarker(BoardState boardState) {
    return moves.remove(0);
  }

  @Override
  public void placementAccepted(Position position, BoardState boardState) {

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