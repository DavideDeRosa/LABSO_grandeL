import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Receiver implements Runnable {

    private Socket s;
    private Thread sender;

    public Receiver(Socket s, Thread sender) {
        this.s = s;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            /*
            Viene preso lo stream di input (i messaggi) dal socket, per trasmettere sul Client le risposte del Server
            */
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
        } catch(Exception e){
            System.out.println("Server non raggiungibile, premere il tasto invio per terminare l'esecuzione."); 
        } finally {
            System.out.println("Receiver chiuso.");
            /*
            Se termina la comunicazione con il receiver, deve terminare anche il sender
            */
            this.sender.interrupt();
        }
    }
}
