import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Utilizzo: java Server <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        Scanner userInput = new Scanner(System.in);

        try {
            ServerSocket server = new ServerSocket(port);
            Thread serverThread = new Thread(new SocketListener(server));
            serverThread.start();

            String command = "";

            while (!command.equals("quit")) {
                command = userInput.nextLine();
            }

            try {
                serverThread.interrupt();
                serverThread.join();
            } catch (InterruptedException e) {
                return;
            }
            System.out.println("Main thread terminato.");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            userInput.close();
        }
        
    }
}