import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Receiver implements Runnable {

    Socket s;
    Thread sender;

    public Receiver(Socket s, Thread sender) {
        this.s = s;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            Scanner from = new Scanner(this.s.getInputStream());
            while (true) {
                String response = from.nextLine();
                System.out.println(response);
                if (response.equals("quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        } finally {
            System.out.println("Receiver chiuso.");
            this.sender.interrupt();
            // COME MAI NON VIENE CHIUSO ANCHE IL RECEIVER, MA SOLO IL SENDER?
            // IL RECEIVER VIENE CHIUSO ALLA FINE DEL RUN!
        }
    }
}
