package bakeit.club.tictactoe.net;

import bakeit.club.tictactoe.game.BoardState;

public record WaitOpponentsMove(BoardState boardState) implements Message {

}
