import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class SimpleServer implements Runnable {
  private static final int PLAY_1 = 1; // X
  private static final int PLAY_2 = -1; // O
  Socket socket1;
  Socket socket2;
  Scanner in1;
  PrintWriter printWriter1;
  Scanner in2;
  PrintWriter printWriter2;
  int[][] chessboard;

  public SimpleServer(Socket socket1, Socket socket2) throws IOException {
    this.socket1 = socket1;
    this.socket2 = socket2;
  }

  @Override
  public void run() {
    try {
      in1 = new Scanner(socket1.getInputStream());
      in2 = new Scanner(socket2.getInputStream());
      printWriter1 = new PrintWriter(socket1.getOutputStream());
      printWriter2 = new PrintWriter(socket2.getOutputStream());
      initGame();
    } catch (NoSuchElementException | IOException e) {

    } finally {
      System.out.println("client program unexpected exit");
      System.out.println("close connection");
      try {
        socket1.close();
        socket2.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void initGame() throws IOException {
    try {
      int side = 1;
      String x;
      String y;
      chessboard = new int[3][3];
      while (true) {
        if (side == 1) {
          x = in1.nextLine();
          y = in1.nextLine();
          chessboard[Integer.parseInt(x)][Integer.parseInt(y)] = 1;
          send(printWriter2, x);
          send(printWriter2, y);
          side = -side;
        } else {
          x = in2.nextLine();
          y = in2.nextLine();
          chessboard[Integer.parseInt(x)][Integer.parseInt(y)] = -1;
          send(printWriter1, x);
          send(printWriter1, y);
          side = -side;
        }
        if (isTerminate(chessboard) == PLAY_1) {
          send(printWriter1, "O win");
          send(printWriter2, "O win");
          break;
        } else if (isTerminate(chessboard) == PLAY_2) {
          send(printWriter1, "X win");
          send(printWriter2, "X win");
          break;
        } else if (isTerminate(chessboard) == 0) {
          send(printWriter1, "draw");
          send(printWriter2, "draw");
          break;
        } else if (isTerminate(chessboard) == 505) {
          send(printWriter1, "continue");
          send(printWriter2, "continue");
        }
        try {
          if (Server.start) {
            socket1.sendUrgentData(0xFF);
            socket2.sendUrgentData(0xFF);
          }
        } catch (Exception e) {
          System.out.println("client program unexpected close");
          System.out.println("close connection");
          socket1.close();
          socket2.close();
          System.exit(2);
        }
      }
    } catch (NoSuchElementException e) {
//            e.printStackTrace();
    } finally {

    }

  }

  public static void send(PrintWriter printWriter, String content) {
    printWriter.println(content);
    printWriter.flush();
    return;
  }

  public static int isTerminate(int[][] board) {
    for (int i = 0; i < board.length; i++) {
      if (board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
        if (board[i][0] == PLAY_1) {
          return PLAY_1;
        } else if (board[i][0] == PLAY_2) {
          return PLAY_2;
        }
      }
      if (board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
        if (board[0][i] == PLAY_1) {
          return PLAY_1;
        } else if (board[0][i] == PLAY_2) {
          return PLAY_2;
        }
      }
    }
    if ((board[0][0] == board[1][1] && board[1][1] == board[2][2]) ||
        (board[0][2] == board[1][1] && board[1][1] == board[2][0])) {
      if (board[1][1] == PLAY_1) {
        return PLAY_1;
      } else if (board[1][1] == PLAY_2) {
        return PLAY_2;
      }
    }
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[0].length; j++) {
        if (board[i][j] == 0) {
          return 505;
        }
      }
    }
    return 0; // draw
  }
}


