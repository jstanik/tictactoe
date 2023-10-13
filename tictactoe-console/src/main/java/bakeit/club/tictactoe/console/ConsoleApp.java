package bakeit.club.tictactoe.console;

import bakeit.club.tictactoe.client.ClientPlayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class ConsoleApp {

  private static final int PORT = 9000;
  private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));

  public static void main(String[] args) {
    System.out.println("Welcome to Tic-Tac-Toe game.");

    String playerName = readName();
    InetAddress address = readAddress();
    InetSocketAddress socketAddress = new InetSocketAddress(address, PORT);

    System.out.println("Waiting for the game to start.");
    ClientPlayer player = new ClientPlayer(new ConsolePlayer(playerName), socketAddress);
    player.play();
  }

  private static InetAddress readAddress() {
    int attempts = 0;
    System.out.print("Enter Game Server host name or IP address: ");
    while (attempts++ < 3) {
      String hostname = readLine();
      try {
        return InetAddress.getByName(hostname);
      } catch (UnknownHostException e) {
        System.out.println("Failed to find the host '" + hostname + "'. Please try again: ");
      }
    }

    throw new IllegalStateException(
        "Failed to resolve game server host name after " + attempts + " attempts.");
  }

  private static String readName() {
    System.out.print("Enter your name: ");
    return readLine();
  }

  private static String readLine() {
    try {
      return READER.readLine();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
