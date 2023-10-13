package bakeit.club.tictactoe.net;

import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.Position;

public record InvalidMove(
    Position position,
    Marker marker,
    String message
) implements Message {

}
