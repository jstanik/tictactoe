package bakeit.club.tictactoe.net;

import bakeit.club.tictactoe.game.Marker;

/**
 * A message indicating a game has started
 *
 * @param assignedMarker the assigned marker to this player.
 * @param opponentName the name of the opponent player
 */
public record GameStarted(Marker assignedMarker, String opponentName) implements Message {

}
