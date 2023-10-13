package bakeit.club.tictactoe.game;

public class InvalidMove extends RuntimeException {

  public InvalidMove(String description) {
    super(description);
  }
}
