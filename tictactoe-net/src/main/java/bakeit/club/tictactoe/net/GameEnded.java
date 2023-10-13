package bakeit.club.tictactoe.net;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.PlayersResult;

public record GameEnded(PlayersResult result, BoardState boardState) implements Message {

}
