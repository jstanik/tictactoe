package bakeit.club.tictactoe.net;


import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Position;

public record MoveAccepted(
    Position position,
    BoardState boardState
) implements Message {

}
