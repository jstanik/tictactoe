package bakeit.club.tictactoe.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {

  private static final int PORT = 9000;
  private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private static AtomicInteger gameIdSequence = new AtomicInteger(1);

  public static void main(String[] args) throws IOException {
    System.out.println("Tic-Tac-Toe Server v0.1");
    System.out.println("Accepting connections on: ");

    getHostAddresses().forEach(
        address -> System.out.println("\t" + address)
    );

    ServerSocket serverSocket = new ServerSocket(PORT);

    GameManager gameManager = null;
    while (true) {
      Socket socket = serverSocket.accept();
      if (gameManager == null) {
        gameManager = new GameManager(gameIdSequence.getAndIncrement());
        gameManager.setPlayer1(new ServerPlayer(socket));
      } else {
        gameManager.setPlayer2(new ServerPlayer(socket));
        executor.submit(gameManager);
        gameManager = null;
      }
    }
  }

  private static List<InetAddress> getHostAddresses() {
    List<InetAddress> result = new ArrayList<>();

    Enumeration<NetworkInterface> interfaces = null;
    try {
      interfaces = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      throw new UncheckedIOException(e);
    }
    while (interfaces.hasMoreElements()) {
      NetworkInterface ni = interfaces.nextElement();
      Enumeration<InetAddress> addresses = ni.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress address = addresses.nextElement();
        if (!address.isLinkLocalAddress() && !address.isLoopbackAddress() && address.isSiteLocalAddress()) {
          result.add(address);
        }
      }
    }

    return result;

  }
}
