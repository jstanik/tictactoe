package bakeit.club.tictactoe.console;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.Player;
import bakeit.club.tictactoe.game.PlayerGameInfo;
import bakeit.club.tictactoe.game.PlayersResult;
import bakeit.club.tictactoe.game.Position;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsolePlayer implements Player {

  private final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
  private final String name;
  private PlayerGameInfo playerGameInfo;
  private boolean waitingForOpponent;

  public ConsolePlayer(String name) {
    this.name = name;
  }

  @Override
  public void gameStarted(PlayerGameInfo info) {
    this.playerGameInfo = info;
  }

  @Override
  public void waitOpponentsMove(BoardState boardState) {
    waitingForOpponent = true;
    System.out.println("Waiting for opponents move.");
    print(boardState);
  }

  @Override
  public Position placeMarker(BoardState boardState) {
    if (waitingForOpponent) {
      System.out.println("The opponent '" + playerGameInfo.opponentsName() + "' has made a move.");
    }
    waitingForOpponent = false;

    print(boardState);
    System.out.println(name + ", it's your turn.");
    System.out.println();

    return readPosition();
  }

  @Override
  public void placementAccepted(Position position, BoardState boardState) {
    System.out.println("Marker was accepted at the position '" + toCoordinates(position) + "'");
    System.out.println();
    print(boardState);
    System.out.println();
  }

  @Override
  public void placementRejected(Position position, String reason) {
    System.out.println("Error: placement of the marker to position " + toCoordinates(position) + "'"
        + " was rejected.");
    System.out.println("Reason: " + reason);
  }

  @Override
  public void gameEnded(BoardState boardState, PlayersResult result) {
    switch (result) {
      case VICTORY ->
          System.out.println("Congratulations " + getName() + ". You won the game!");
      case DEFEAT -> System.out.println("Game Over, " + getName() + ". You lost the game!");
      case DRAW -> System.out.println("Game Over, " + getName() + " - Draw!");
    }

    print(boardState);
  }

  public String getName() {
    return name;
  }

  private void print(BoardState boardState) {
    System.out.println("     a   b   c");
    System.out.println("   -------------");

    for (int row = 0; row < 3; row++) {
      System.out.print(" " + (row + 1) + " |");
      for (int column = 0; column < 3; column++) {
        Marker marker = boardState.getMarker(new Position(row, column));
        String symbol;
        if (marker == Marker.EMPTY) {
          symbol = " ";
        } else {
          symbol = marker.toString();
        }
        System.out.print(" " + symbol + " |");
      }
      System.out.println();
      System.out.println("   -------------");
    }
    System.out.println();
  }

  private Position readPosition() {
    Position position = null;

    while (position == null) {
      String line;
      try {
        System.out.print("Enter the position for the marker: ");
        line = consoleReader.readLine().trim().toLowerCase();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      if (line.length() != 2) {
        printInputError();
        continue;
      }

      int cIndex = Character.isAlphabetic(line.charAt(0)) ? 0 : 1;
      int dIndex = 1 - cIndex;

      char c = line.charAt(cIndex);
      char d = line.charAt(dIndex);

      if (!Character.isAlphabetic(c) || !Character.isDigit(d)
          || (c != 'a' && c != 'b' && c != 'c')
          || (d != '1' && d != '2' && d != '3')) {
        printInputError();
        continue;
      }

      position = new Position(d - '0' - 1, c - 'a');
    }

    return position;
  }

  private void printInputError() {
    System.out.println("Invalid position. Must have two letters: one character"
        + " and one digit for a column. Example: a2");
  }

  private String toCoordinates(Position position) {
    return new StringBuilder()
        .append(
            switch (position.column()) {
              case 0 -> 'a';
              case 1 -> 'b';
              case 2 -> 'c';
              default -> throw new IllegalStateException("Unexpected column: " + position.column());
            }
        )
        .append(position.row() + 1)
        .toString();
  }
}
