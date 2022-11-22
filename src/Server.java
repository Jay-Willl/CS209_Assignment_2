import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Server {
  public static boolean start;

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    InputStream temp = ClassLoader.getSystemResourceAsStream("ports.properties");
    properties.load(temp);
    int port = Integer.parseInt(properties.getProperty("1"));
    ServerSocket server = new ServerSocket(port);
    System.out.println("Waiting");
    boolean flag = false;
    while (true) {
      while (flag == false) {
        Socket socket1 = server.accept();
        PrintWriter printWriter1 = new PrintWriter(socket1.getOutputStream());
        System.out.println("accept client 1");
        send(printWriter1, "1");
        send(printWriter1, "wait for opponent");
        Socket socket2 = server.accept();
        PrintWriter printWriter2 = new PrintWriter(socket2.getOutputStream());
        System.out.println("accept client 2");
        send(printWriter2, "-1");
        send(printWriter1, "start");
        send(printWriter2, "start");
        start = true;
        new Thread(new SimpleServer(socket1, socket2)).start();
      }
    }
  }

  public static void send(PrintWriter printWriter, String content) {
    printWriter.println(content);
    printWriter.flush();
    return;
  }
}
