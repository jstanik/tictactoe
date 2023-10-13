package bakeit.club.tictactoe.game;

/**
 * Basic information about the game for the player.
 *
 * @param assignedMarker the marker that has been assigned to the player
 * @param opponentsName the opponent's player name
 */
public record PlayerGameInfo(
    Marker assignedMarker,
    String opponentsName
) {

}
