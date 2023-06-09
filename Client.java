import java.net.ConnectException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Utilizzo: java Client <host> <port>");
            return;
        }
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            /*
            Creazione del socket, collegando il Client al Server
            */
            Socket s = new Socket(host, port);
            System.out.println("Connesso al server");
            System.out.println("Benvenuto nel servizio bancario grandeL!\n");

            /*
            Viene creato un Thread sender per inviare ed un thread receiver per ricevere i messaggi
            */
            Thread sender = new Thread(new Sender(s));
            Thread receiver = new Thread(new Receiver(s, sender));

            sender.start();
            receiver.start();

            /*
            Conclusa la comunicazione, i due thread vengono terminati ed il socket viene chiuso
            */
            try {
                sender.join();
                receiver.join();
                s.close();
                System.out.println("Socket chiuso.");
            } catch (InterruptedException e) {
                return;
            }

        } catch (ConnectException e) {
            System.err.println("Server non disponibile!");
        } catch (Exception e) {
            System.err.println("Utilizzo: java Client <host> <port>");
        }
    }
}
