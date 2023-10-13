package bakeit.club.tictactoe.net;

public sealed interface Message permits
    GameEnded,
    GameStarted,
    JoinGame,
    MakeMove,
    Move,
    MoveAccepted,
    InvalidMove,
    WaitOpponentsMove {



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
