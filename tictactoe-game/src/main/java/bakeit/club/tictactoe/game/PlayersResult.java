package bakeit.club.tictactoe.game;

/**
 * A result of a game from a player's perspective.
 */
public enum PlayersResult {
  VICTORY,
  DEFEAT,
  DRAW;

  /**
   * Inverts this result to the other player's perspective result.
   */
  public PlayersResult invert() {
    return switch (this) {
      case VICTORY -> DEFEAT;
      case DEFEAT -> VICTORY;
      case DRAW -> DRAW;
    };
  }
}
