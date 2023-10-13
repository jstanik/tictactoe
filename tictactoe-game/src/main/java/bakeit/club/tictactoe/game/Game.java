package bakeit.club.tictactoe.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A tic-tac-toe game.
 */
public class Game {

  private static final List<Tripple> TRIPPLES = new ArrayList<>(8);

  static {
    for (int i = 0; i < 3; i++) {
      TRIPPLES.add(new Tripple(new Position(0, i), new Position(1, i), new Position(2, i)));
      TRIPPLES.add(new Tripple(new Position(i, 0), new Position(i, 1), new Position(i, 2)));
    }

    TRIPPLES.add(new Tripple(new Position(0, 0), new Position(1, 1), new Position(2, 2)));
    TRIPPLES.add(new Tripple(new Position(0, 2), new Position(1, 1), new Position(2, 0)));
  }

  private final Board board;
  private Player movingPlayer;
  private Player waitingPlayer;
  private BoardState currentState;
  private final Map<Player, Marker> markerAssignments = new HashMap<>();

  public Game(Player player1, Player player2) {
    this.movingPlayer = player1;
    this.waitingPlayer = player2;
    this.board = new Board();

    this.currentState = BoardState.of(board);
  }

  public void play() {
    markerAssignments.put(movingPlayer, Marker.X);
    markerAssignments.put(waitingPlayer, Marker.O);

    movingPlayer.gameStarted(new PlayerGameInfo(markerAssignments.get(movingPlayer), waitingPlayer.getName()));
    waitingPlayer.gameStarted(new PlayerGameInfo(markerAssignments.get(waitingPlayer), movingPlayer.getName()));

    while (!isGameEnded()) {
      makeRound();
    }

    PlayersResult movingPlayerResult = detectMovingPlayerResult();

    movingPlayer.gameEnded(currentState, movingPlayerResult);
    waitingPlayer.gameEnded(currentState, movingPlayerResult.invert());
  }

  private PlayersResult detectMovingPlayerResult() {
    Marker winnersMarker = findStrike()
        .map(strike -> currentState.getMarker(strike.p1))
        .orElse(Marker.EMPTY);

    if (winnersMarker == Marker.EMPTY) {
      return PlayersResult.DRAW;
    } else {
      return markerAssignments.get(movingPlayer) == winnersMarker
          ? PlayersResult.VICTORY
          : PlayersResult.DEFEAT;
    }
  }

  private void makeRound() {
    Marker marker = markerAssignments.get(movingPlayer);

    boolean markerPlaced = false;
    while (!markerPlaced) {
      waitingPlayer.waitOpponentsMove(currentState);
      Position position = movingPlayer.placeMarker(currentState);
      try {
        board.placeMarker(position, marker);
        currentState = BoardState.of(board);
        movingPlayer.placementAccepted(position, currentState);
        markerPlaced = true;
      } catch (InvalidMove invalidMove) {
        String errorMessage = "Invalid move detected for the player playing with '"
            + markerAssignments.get(movingPlayer)
            + "': "
            + invalidMove.getMessage();
        System.out.println(errorMessage);
        movingPlayer.placementRejected(position, errorMessage);
      }
    }

    Player playerMadeMove = movingPlayer;
    movingPlayer = waitingPlayer;
    waitingPlayer = playerMadeMove;
  }

  private boolean isGameEnded() {
    if (findStrike().isPresent()) {
      return true;
    }

    for (int i = 0; i < 9; i++) {
      if (currentState.getMarker(position(i)) == Marker.EMPTY) {
        return false;
      }
    }

    return true;
  }

  private Optional<Tripple> findStrike() {
    return TRIPPLES.stream().filter(t -> t.hasStrike(currentState)).findFirst();
  }

  private static Position position(int index) {
    return new Position(index / 3, index % 3);
  }

  private record Tripple(Position p1, Position p2, Position p3) {

    boolean hasStrike(BoardState state) {
      return state.getMarker(p1) != Marker.EMPTY
          && state.getMarker(p1) == state.getMarker(p2)
          && state.getMarker(p2) == state.getMarker(p3);
    }
  }
}
