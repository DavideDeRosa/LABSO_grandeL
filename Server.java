import java.net.ServerSocket;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Utilizzo: java Server <port>");
            return;
        }

        Scanner userInput = new Scanner(System.in);

        try {
            int port = Integer.parseInt(args[0]);
            /*
            Viene creato il ServerSocket, per ricevere le richieste
            */
            ServerSocket server = new ServerSocket(port);
            /*
            Viene creato un Thread che gestisce le richieste
            */
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
        } catch (Exception e) {
            System.err.println("Utilizzo: java Server <port>");
        } finally {
            userInput.close();
            System.exit(0);
        }
    }
}