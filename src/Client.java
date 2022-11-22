import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public class Client extends Application {
    private Stage stage;
    private Parent root;
    private Scene scene;
    private static Socket socket;
    private static Scanner scanner;
    private static PrintWriter printWriter;

    private static String text;

    private static int side; // 1 - X,  -1 - O

    private static boolean act; // denote whether player has played

    private static int x;
    private static int y;

    private static int turns;

    private static int[][] chessboard;

    public static int getSide() {
        return side;
    }

    public static void setSide(int side) {
        Client.side = side;
    }

    public static boolean getAct() {
        return act;
    }

    public static void setAct(boolean act) {
        Client.act = act;
    }

    public static int getX() {
        return x;
    }

    public static void setX(int x) {
        Client.x = x;
    }

    public static int getY() {
        return y;
    }

    public static void setY(int y) {
        Client.y = y;
    }

    public static int getTurns() {
        return turns;
    }

    public static void setTurns(int turns) {
        Client.turns = turns;
    }

    public static int[][] getChessboard() {
        return chessboard;
    }

    public static void setChessboard(int[][] chessboard) {
        Client.chessboard = chessboard;
    }

    public static String getText() {
        return text;
    }

    public static void setText(String text) {
        Client.text = text;
    }

    @Override
    public void start(Stage primaryStage) {
        try{
            stage = primaryStage;
            stage.setTitle("Game");
            root = FXMLLoader.load(getClass().getClassLoader().getResource("Chessboard.fxml"));
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }catch(Exception e){
            e.printStackTrace();
        }
        new Thread(() -> {
            // determine which side
            side = Integer.parseInt(scanner.nextLine());
            if(side == 1){
                text = scanner.nextLine();
                System.out.println(text);
            }
            text = scanner.nextLine();
            System.out.println(text);
            while(true){
                try{
                    if (side == 1) { // first user, send first then recv
                        // perform act
                        try {
                            while(!act){
                                Thread.sleep(500);
                            }
                            act = false;
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        send(printWriter, String.valueOf(x));
                        send(printWriter, String.valueOf(y));
                        System.out.println("friendly: " + x + " " + y);
                        chessboard[x][y] = 1;
                        // review results
                        String info1 = scanner.nextLine();
                        System.out.println(info1);
                        if(Objects.equals(info1, "X win") || Objects.equals(info1, "O win")
                                || Objects.equals(info1, "draw")){
                            text = info1 + "! game finish!";
                            break;
                        }
                        text = "opponent turn";
                        int tempx = Integer.parseInt(scanner.nextLine());
                        int tempy = Integer.parseInt(scanner.nextLine());
                        System.out.println("opponent: " + tempx + " " + tempy);
                        chessboard[tempx][tempy] = -1;
                        text = "your turn";
                        // review results
                        String info2 = scanner.nextLine();
                        System.out.println(info2);
                        if(Objects.equals(info2, "X win") || Objects.equals(info2, "O win")
                                || Objects.equals(info1, "draw")){
                            text = info2 + "! game finish!";
                            break;
                        }
                    } else if (side == -1) { // second user, recv first then send
                        text = "opponent turn";
                        int tempx = Integer.parseInt(scanner.nextLine());
                        int tempy = Integer.parseInt(scanner.nextLine());
                        System.out.println("opponent: " + tempx + " " + tempy);
                        chessboard[tempx][tempy] = 1;
                        // review results
                        String info1 = scanner.nextLine();
                        System.out.println(info1);
                        if(Objects.equals(info1, "X win") || Objects.equals(info1, "O win")
                                || Objects.equals(info1, "draw")){
                            text = info1 + "! game finish!";
                            break;
                        }
                        text = "your turn";
                        // perform act
                        try {
                            while(!act){
                                Thread.sleep(500);
                            }
                            act = false;
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        send(printWriter, String.valueOf(x));
                        send(printWriter, String.valueOf(y));
                        System.out.println("friendly: " + x + " " + y);
                        chessboard[x][y] = -1;
                        // review results
                        String info2 = scanner.nextLine();
                        System.out.println(info2);
                        if(Objects.equals(info2, "X win") || Objects.equals(info2, "O win")
                                || Objects.equals(info1, "draw")){
                            text = info2 + "! game finish!";
                            break;
                        }
                    }
                }catch(NoSuchElementException e){
                    System.out.println("server program unexpected close");
                    System.out.println("close connection");
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.exit(2);
                }
            }
            try{
                socket.sendUrgentData(0xFF);
            }catch(Exception e){
                System.out.println("server program unexpected close");
                System.out.println("close connection");
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(e);
                }
                System.exit(2);
            }
        }).start();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.print("window close, stop game and notify server");
                try {
                    socket.close();
                    System.exit(2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void send(PrintWriter printWriter, String content){
        printWriter.println(content);
        printWriter.flush();
    }

    public static void reviewResults(String str){

    }

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        InputStream temp = ClassLoader.getSystemResourceAsStream("ports.properties");
        properties.load(temp);
        socket = new Socket("localhost", Integer.parseInt(properties.getProperty("1")));
        scanner = new Scanner(socket.getInputStream());
        printWriter = new PrintWriter(socket.getOutputStream());
        chessboard = new int[3][3];
        launch(args);
    }
}
