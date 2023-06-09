import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Sender implements Runnable {

    private Socket s;

    public Sender(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        Scanner userInput = new Scanner(System.in);

        try {
            PrintWriter to = new PrintWriter(this.s.getOutputStream(), true);
            while (true) {
                String request = userInput.nextLine();
                if (Thread.interrupted()) {
                    to.println("quit");
                    break;
                }
                /*
                Viene inviata la comunicazione al Server
                */
                to.println(request);
                if (request.equals("quit")) {
                    break;
                }
            }
            System.out.println("Sender chiuso.");
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            userInput.close();
        }
    }

}
