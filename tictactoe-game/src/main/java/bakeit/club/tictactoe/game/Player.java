package bakeit.club.tictactoe.game;

/**
 * A tic-tac-toe player that can play the game.
 */
public interface Player {

  String getName();

  /**
   * Notifies this player that the game has started.
   *
   * @param gameInfo the basic information about the game
   */
  void gameStarted(PlayerGameInfo gameInfo);

  /**
   * Indicates this player that it's now the opponents turn to make a move.
   *
   * @param boardState the current board state
   */
  void waitOpponentsMove(BoardState boardState);

  /**
   * Calls the player to place the marker to a next position.
   *
   * @param boardState the current state of the board
   * @return the position where the player wants to place his marker
   */
  Position placeMarker(BoardState boardState);

  /**
   * Notifies this player that his last placement of the marker has been accepted by the game.
   *
   * @param position   the position of the marker
   * @param boardState the board state after the placement of the marker was accepted
   * @see #placeMarker(BoardState)
   */
  void placementAccepted(Position position, BoardState boardState);

  /**
   * Notifies this player that his last placement of the marker has been rejected by the game.
   *
   * @param position the position where the marker should have been placed
   * @param reason   the reason of the placement rejection
   */
  void placementRejected(Position position, String reason);

  /**
   * Signals the player the game ended.
   *
   * @param boardState the end-game board state
   * @param result     the result of the game from the player's perspective
   */
  void gameEnded(BoardState boardState, PlayersResult result);

}
