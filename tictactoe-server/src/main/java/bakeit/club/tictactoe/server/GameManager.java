package bakeit.club.tictactoe.server;

import bakeit.club.tictactoe.game.Game;
import java.io.IOException;
import java.util.Objects;

class GameManager implements Runnable {

  private final int gameId;
  private ServerPlayer player1;
  private ServerPlayer player2;

  public GameManager(int gameId) {
    this.gameId = gameId;
  }

  public void run() {
    Objects.requireNonNull(player1);
    Objects.requireNonNull(player2);
    try {
      player1.readName();
      log("Received player1 name: " + player1.getName());
      player2.readName();
      log("Received player2 name: " + player2.getName());

      Game game = new Game(player1, player2);
      game.play();

    } finally {
      try {
        player1.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }

      try {
        player2.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }

      log("Game ended.");

    }
  }

  public void setPlayer1(ServerPlayer player1) {
    this.player1 = player1;
  }

  public void setPlayer2(ServerPlayer player2) {
    this.player2 = player2;
  }

  private void log(String message) {
    System.out.println(Thread.currentThread() + ": Game[" + gameId + "] - " + message);
  }
}
