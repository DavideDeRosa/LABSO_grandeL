import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class ClientHandler implements Runnable {

    Socket s;
    private Vector<BankAccount> bankAccounts;
    private String accountsList;

    public ClientHandler(Socket s) {
        this.s = s;
        bankAccounts = new Vector<BankAccount>();
        accountsList = "";
    }

    @Override
    public void run() {
        try {
            Scanner from = new Scanner(s.getInputStream());
            PrintWriter to = new PrintWriter(s.getOutputStream(), true);

            System.out.println("Thread " + Thread.currentThread() + " listening...");

            boolean closed = false;
            while (!closed) {
                String request = from.nextLine();
                if (!Thread.interrupted()) {
                    System.out.println("Request: " + request);
                    String[] parts = request.split(" ");
                    switch (parts[0]) {
                        case "quit":
                            closed = true;
                            break;
                        case "help":
                            // FARE HELP CHE SPIEGHI I VARI COMANDI E I LORO UTILIZZI
                            break;
                        case "open":
                            if (parts.length == 3) {
                                String accountName = parts[1];
                                if(!contoPresente(accountName.toLowerCase())){
                                    double accountAmount = Double.parseDouble(parts[2]);
                                    to.println("Account creato!\tNome: " + accountName + "\tBilancio iniziale: " + accountAmount);
                                    BankAccount b = new BankAccount(accountName, accountAmount);
                                    bankAccounts.add(b);
                                    accountsList = accountsList + b.toString();
                                }else{
                                    to.println("Esiste già un conto con questo nome! Conto non creato correttamente.");
                                }
                            } else {
                                to.println("Comando scritto in maniera errata! Scrivere 'help' per ulteriori informazioni.");
                            }
                            break;
                        case "list":
                        // PERCHE NON VA UN CICLO O UN ITERATOR SUL VECTOR? CHIEDERE AL PROF (metodo concatenare stringhe in questo modo
                        // non permette di fare il punto opzionale per rimuovere un conto corrente)
                            if(!bankAccounts.isEmpty()){
                                to.println(accountsList);
                            }else{
                                to.println("Lista vuota!");
                            }
                            break;
                        default:
                            to.println("Unknown cmd");
                    }
                } else {
                    to.println("quit");
                    break;
                }
            }

            to.println("quit");
            s.close();
            System.out.println("Closed");
        } catch (IOException e) {
            System.err.println("ClientHandler: IOException caught: " + e);
            e.printStackTrace();
        }
    }

    private boolean contoPresente(String name){
        for (BankAccount b : bankAccounts) {
            if(b.getName().toLowerCase().equals(name)){
                return true;
            }
        }
        return false;
    }

}
