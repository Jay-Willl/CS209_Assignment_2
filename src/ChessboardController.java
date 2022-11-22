import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class ChessboardController implements Initializable {

  private static final int PLAY_1 = 1;
  private static final int PLAY_2 = -1;
  private static final int EMPTY = 0;
  private static final int BOUND = 90;
  private static final int OFFSET = 15;
  private static int[][] chessBoard;
  @FXML
  private Pane background;
  @FXML
  private Rectangle mainBoard;
  @FXML
  private TextArea infoTextArea;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    mainBoard.setOnMouseClicked(event -> {
      if (Objects.equals(Client.getText(), "your turn") ||
          Objects.equals(Client.getText(), "start")) {
        int x = (int) (event.getX() / BOUND);
        int y = (int) (event.getY() / BOUND);
        chessBoard = Client.getChessboard();
        if (chessBoard[x][y] == 0) {
          Client.setAct(true);
          Client.setX(x);
          Client.setY(y);
        } else {
          System.out.println("#!  Not a valid place");
        }
      }
    });
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            // set timer to refresh board ad textarea
            refreshBoard(Client.getChessboard());
            refreshText(Client.getText());
          }
        });

      }
    }, 100, 500);
  }

  private void refreshBoard(int[][] chessBoard) {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        switch (chessBoard[i][j]) {
          case PLAY_1:
            drawCircle(i, j);
            break;
          case PLAY_2:
            drawLine(i, j);
            break;
          case EMPTY:
            break;
        }
      }
    }
  }

  private void refreshText(String content) {
    infoTextArea.clear();
    infoTextArea.appendText(content);
  }

  private void drawCircle(int i, int j) {
    Circle circle = new Circle();
    background.getChildren().add(circle);
    circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
    circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
    circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
    circle.setStroke(Color.RED);
    circle.setFill(Color.TRANSPARENT);
  }

  private void drawLine(int i, int j) {
    Line line_a = new Line();
    Line line_b = new Line();
    background.getChildren().add(line_a);
    background.getChildren().add(line_b);
    line_a.setStartX(i * BOUND + OFFSET * 1.5);
    line_a.setStartY(j * BOUND + OFFSET * 1.5);
    line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
    line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_a.setStroke(Color.BLUE);

    line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
    line_b.setStartY(j * BOUND + OFFSET * 1.5);
    line_b.setEndX(i * BOUND + OFFSET * 1.5);
    line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_b.setStroke(Color.BLUE);
  }

}
