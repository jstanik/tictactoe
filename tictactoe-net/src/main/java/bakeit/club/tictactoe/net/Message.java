package bakeit.club.tictactoe.net;

/**
 * This interface represents a message that can be exchanged between a game server and the player's
 * client application.
 */
public sealed interface Message permits
    GameEnded,
    GameStarted,
    JoinGame,
    MakeMove,
    Move,
    MoveAccepted,
    InvalidMove,
    WaitOpponentsMove {

  /**
   * Gets the code of this message. The code identifies the message type.
   *
   * @return the message type's assigned code
   */
  default byte getCode() {
    return switch (this) {
      case GameStarted gameStarted -> 0;
      case JoinGame joinGame -> 1;
      case MakeMove makeMove -> 2;
      case Move move -> 3;
      case MoveAccepted moveAccepted -> 4;
      case GameEnded gameEnded -> 5;
      case InvalidMove invalidMove -> 6;
      case WaitOpponentsMove waitOpponentsMove -> 7;
    };
  }
}
