import java.io.IOException;
import java.net.Socket;

/*
    QUANDO CHIUDIAMO IL SERVER, I VARI CLIENT NON HANNO UN MESSAGGIO DI CHIUSURA O DI TERMINAZIONE DEL SERVIZIO. E' UNA COSA VOLUTA O VA FIXATA?
    UNA VOLTA CHIUSO IL SERVER, MANDATI DUE MESSAGGI VIENE TERMINATO IL CLIENT. CAPIRE SE IL COMPORTAMENTO VA CAMBIATO.
 */

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Utilizzo: java Client <host> <port>");
            return;
        }

        String host = args[0]; // QUANDO DO UN IP VALIDO MA NON CON UN SERVER IN ASCOLTO IL PROGRAMMA NON FA NULLA, COME MAI?
        int port = Integer.parseInt(args[1]);

        try {
            Socket s = new Socket(host, port);
            System.out.println("Connesso al server");
            System.out.println("Benvenuto nel servizio bancario grandeL!");

            Thread sender = new Thread(new Sender(s));
            Thread receiver = new Thread(new Receiver(s, sender));

            sender.start();
            receiver.start();

            try {
                sender.join();
                receiver.join();
                s.close();
                System.out.println("Socket chiuso.");
            } catch (InterruptedException e) {
                return;
            }

        } catch (IOException e) {
            System.err.println("IOException caught: " + e);
            e.printStackTrace();
        }
    }
}
