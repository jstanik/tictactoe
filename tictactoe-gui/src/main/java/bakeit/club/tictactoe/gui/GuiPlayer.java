package bakeit.club.tictactoe.gui;

import static javax.swing.SwingUtilities.invokeLater;

import bakeit.club.tictactoe.client.ClientPlayer;
import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Player;
import bakeit.club.tictactoe.game.PlayerGameInfo;
import bakeit.club.tictactoe.game.PlayersResult;
import bakeit.club.tictactoe.game.Position;
import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class GuiPlayer extends JFrame implements Player {

  private static final int PORT = 9000;

  public static void main(String[] args) {
    GuiPlayer frame = new GuiPlayer();
    frame.setSize(600, 600);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    invokeLater((() -> {
      String name = readName();
      frame.setName(name);
      frame.setTitle(name);
      InetAddress address = readAddress();
      InetSocketAddress socketAddress = new InetSocketAddress(address, PORT);
      ClientPlayer player = new ClientPlayer(frame, socketAddress);
      frame.setVisible(true);

      Thread thread = new Thread(() -> {
        try {
          player.play();
        } catch (Exception exception) {
          invokeLater(() -> {
            JOptionPane.showMessageDialog(
                frame,
                exception.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
          });
          invokeLater(frame::dispose);
        }
      });
      thread.start();
    }));
  }

  private static String readName() {
    String name = JOptionPane.showInputDialog("Please, enter your name:");
    if (name == null) {
      System.exit(0);
    }

    return name;
  }

  private static InetAddress readAddress() {
    int attempts = 0;
    while (attempts++ < 3) {
      String hostname = JOptionPane.showInputDialog("Enter Game Server host name or IP address:");
      if (hostname == null) {
        System.exit(0);
      }
      try {
        return InetAddress.getByName(hostname);
      } catch (UnknownHostException e) {
        JOptionPane.showMessageDialog(null,
            "Failed to find the host '" + hostname + "'.",
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }

    throw new IllegalStateException(
        "Failed to resolve game server host name after " + attempts + " attempts.");
  }

  private final Board board = new Board();
  private final JLabel labelStatus = new JLabel("Waiting for the game to start.");
  private String opponentName;

  GuiPlayer() {
    super("Tic Tac Toe");
    var pane = new JScrollPane(board);
    pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(labelStatus, BorderLayout.NORTH);
    getContentPane().add(pane, BorderLayout.CENTER);
  }

  @Override
  public void gameStarted(PlayerGameInfo gameInfo) {
    invokeAndWait(() -> {
      labelStatus.setText("Game started!");
      opponentName = gameInfo.opponentsName();
      JOptionPane.showMessageDialog(this, "Your opponent is " + gameInfo.opponentsName());
    });
  }

  @Override
  public void waitOpponentsMove(BoardState boardState) {
    invokeAndWait(() -> labelStatus.setText(opponentName + " is making a move."));
    board.setBoardState(boardState);
  }

  @Override
  public Position placeMarker(BoardState boardState) {
    invokeAndWait(() -> labelStatus.setText("Please, make a move."));
    board.setBoardState(boardState);
    CompletableFuture<Position> positionFuture = new CompletableFuture<>();

    board.getMove(positionFuture::complete);

    try {
      return positionFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void placementAccepted(Position position, BoardState boardState) {
    board.setBoardState(boardState);
  }

  @Override
  public void placementRejected(Position position, String reason) {
    invokeAndWait(() -> {
      JOptionPane.showMessageDialog(this,
          "Your move has been rejected",
          "Error",
          JOptionPane.ERROR_MESSAGE);
    });
  }

  @Override
  public void gameEnded(BoardState boardState, PlayersResult result) {
    board.setBoardState(boardState);

    invokeAndWait(() -> {
      labelStatus.setText("Game ended.");
      switch (result) {
        case VICTORY -> JOptionPane.showMessageDialog(this,
            "Congratulations " + getName() + ". You won the game!",
            "Winner!",
            JOptionPane.INFORMATION_MESSAGE);
        case DEFEAT -> JOptionPane.showMessageDialog(this,
            "Game Over, " + getName() + ". You lost the game!",
            "Looser!",
            JOptionPane.INFORMATION_MESSAGE);
        case DRAW -> JOptionPane.showMessageDialog(this,
            "Game Over, " + getName() + " - Draw!",
            "Draw!",
            JOptionPane.INFORMATION_MESSAGE);
      }
    });
  }

  private void invokeAndWait(Runnable runnable) {
    try {
      SwingUtilities.invokeAndWait(runnable);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
