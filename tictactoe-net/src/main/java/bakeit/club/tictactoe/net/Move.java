package bakeit.club.tictactoe.net;

import bakeit.club.tictactoe.game.Position;

/**
 * A command conveying the players move to the server.
 * @param position the position where the player wants to place a marker.
 * @param token the token received from the server when asked for a move.
 */
public record Move(
    Position position,
    long token
) implements Message {

}
