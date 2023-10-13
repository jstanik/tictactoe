package bakeit.club.tictactoe.net;

import bakeit.club.tictactoe.game.BoardState;

/**
 * A message asking a player to make a move.
 *
 * @param token      a token which entitles the player to make a move. The token should be used when
 *                   making the move.
 * @param boardState a current board state
 */
public record MakeMove(
    long token,
    BoardState boardState
) implements Message {

}
