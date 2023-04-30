import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class SocketListener implements Runnable {
    private ServerSocket server;
    private ArrayList<Thread> children = new ArrayList<>();
    private volatile Resource r; //VOLATILE: SERVE PER SCRIVERE SEMPRE SULLA STESSA STRUTTURA DATI E NON SU UNA COPIA LOCALE DI UN THREAD

    public SocketListener(ServerSocket server) {
        this.server = server;
        r = new Resource();
    }

    @Override
    public void run() {
        try {
            this.server.setSoTimeout(5000);
            
            while (!Thread.interrupted()) {
                try {
                    System.out.println("In attesa di un nuovo client...");
                    Socket s = this.server.accept();
                    if (!Thread.interrupted()) {
                        System.out.println("Client connesso");
                        Thread handlerThread = new Thread(new ClientHandler(s, r));
                        handlerThread.start();
                        this.children.add(handlerThread);
                    } else {
                        s.close();
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout, continuing...");
                    continue;
                } catch (IOException e) {
                    break;
                }
            }
            this.server.close();
        } catch (IOException e) {
            System.err.println("SocketListener: IOException caught: " + e);
            e.printStackTrace();
        }

        System.out.println("Interruzione dei thread figli...");
        for (Thread child : this.children) {
            System.out.println("Interrotto " + child + "...");
            child.interrupt();
        }

    }

}
