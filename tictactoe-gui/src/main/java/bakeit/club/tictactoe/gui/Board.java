package bakeit.club.tictactoe.gui;

import bakeit.club.tictactoe.game.BoardState;
import bakeit.club.tictactoe.game.Marker;
import bakeit.club.tictactoe.game.Position;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.JPanel;

public class Board extends JPanel {

  private static final int WIDTH = 300;
  private static final int HEIGHT = 300;

  private MoveHandler moveHandler = null;

  private final Cell[] cells;

  Board() {
    setMinimumSize(new Dimension(WIDTH, HEIGHT));
    cells = new Cell[9];
    Arrays.setAll(cells, (i) -> new Cell());

    MouseListener mouseListener = new MouseListener();

    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
  }

  public void setBoardState(BoardState boardState) {
    for (int i = 0; i < cells.length; i++) {
      Position position = new Position(i / 3, i % 3);
      Marker marker = boardState.getMarker(position);
      cells[i].hasChanged = cells[i].marker != marker;
      cells[i].marker = marker;
    }

    repaint();
  }

  public void getMove(MoveHandler moveHandler) {
    this.moveHandler = moveHandler;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Rectangle boardArea = getBoardArea();

    Graphics2D g2 = (Graphics2D) g.create(
        boardArea.x,
        boardArea.y,
        boardArea.width,
        boardArea.height
    );
    g2.setColor(Color.BLACK);

    final int cellSize = Math.min(WIDTH / 3, HEIGHT / 3);

    for (int i = 0; i < 9; i++) {
      int row = i / 3;
      int column = i % 3;
      int x = column * cellSize;
      int y = row * cellSize;

      paintCell(cells[i], (Graphics2D) g2.create(x, y, cellSize, cellSize), cellSize);
    }

  }

  private void paintCell(Cell cell, Graphics2D g, int size) {
    g.drawRect(0, 0, size, size);
    paintMarker(cell.marker, (Graphics2D) g.create(), size);

    BasicStroke stroke = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    g.setStroke(stroke);

    if (cell.mouseOver && cell.marker == Marker.EMPTY) {
      g.setColor(Color.GREEN);
      g.drawRect(2, 2, size - 2 * 2, size - 2 * 2);
    }

    if (cell.hasChanged) {
      g.setColor(Color.ORANGE);
      g.drawRect(2, 2, size - 2 * 2, size - 2 * 2);
    }
  }

  private void paintMarker(Marker marker, Graphics2D g, int size) {
    BasicStroke stroke = new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    g.setStroke(stroke);

    final int indent = 10;

    switch (marker) {
      case X -> {
        g.setColor(Color.RED);
        g.drawLine(indent, indent, size - indent, size - indent);
        g.drawLine(indent, size - indent, size - indent, indent);
      }
      case O -> {
        g.setColor(Color.BLUE);
        g.drawOval(indent, indent, size - 2 * indent, size - 2 * indent);
      }
    }
  }

  private int toColumn(int x) {
    var area = getBoardArea();
    if (!area.contains(x, area.y)) {
      return -1;
    }

    int cellWidth = area.width / 3;

    return (x - area.x) / cellWidth;
  }

  private int toRow(int y) {
    var area = getBoardArea();
    if (!area.contains(area.x, y)) {
      return -1;
    }

    int cellHeight = area.height / 3;

    return (y - area.y) / cellHeight;
  }

  private Rectangle getBoardArea() {
    int width = getWidth();
    int height = getHeight();

    int clipX = Math.max(0, (width - WIDTH) / 2);
    int clipY = Math.max(0, (height - HEIGHT) / 2);

    return new Rectangle(clipX, clipY, WIDTH, HEIGHT);
  }

  private class MouseListener extends MouseAdapter {

    @Override
    public void mouseClicked(MouseEvent e) {

      if (moveHandler == null) {
        return;
      }

      int rowClicked = toRow(e.getY());
      int columnClicked = toColumn(e.getX());

      if (rowClicked == -1 || columnClicked == -1) {
        return;
      }

      for (int i = 0; i < 9; i++) {
        int row = i / 3;
        int column = i % 3;

        if (row == rowClicked && column == columnClicked) {
          if (cells[i].marker == Marker.EMPTY) {
            moveHandler.moveMade(new Position(rowClicked, columnClicked));
            Arrays.stream(cells).forEach(c -> c.hasChanged = false);
            moveHandler = null;
          }
        }
      }

      repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      int rowOver = toRow(e.getY());
      int columnOver = toColumn(e.getX());

      if (rowOver == -1 || columnOver == -1 || moveHandler == null) {
        Arrays.stream(cells).forEach(c -> c.mouseOver = false);
        repaint();
        return;
      }

      for (int i = 0; i < 9; i++) {
        int row = i / 3;
        int column = i % 3;

        if (row == rowOver && column == columnOver) {
          if (cells[i].marker == Marker.EMPTY) {
            cells[i].mouseOver = true;
          }
        } else {
          cells[i].mouseOver = false;
        }
      }

      repaint();
    }
  }

  private static class Cell {

    private Marker marker;
    private boolean mouseOver;
    private boolean hasChanged;

    Cell() {
      marker = Marker.EMPTY;
      mouseOver = false;
      hasChanged = false;
    }
  }

  /**
   * A handler of a made move.
   */
  public interface MoveHandler {

    /**
     * Notifies the move handler about the made move
     *
     * @param position the position of the placed marker
     */
    void moveMade(Position position);
  }
}
