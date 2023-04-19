import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class ClientHandler implements Runnable {

    Socket s;
    private Vector<BankAccount> bankAccounts;

    public ClientHandler(Socket s) {
        this.s = s;
        bankAccounts = new Vector<BankAccount>();
    }

    @Override
    public void run() {
        try {
            Scanner from = new Scanner(s.getInputStream());
            PrintWriter to = new PrintWriter(s.getOutputStream(), true);

            System.out.println("Thread " + Thread.currentThread() + " in ascolto...");

            boolean closed = false;
            while (!closed) {
                String request = from.nextLine();
                if (!Thread.interrupted()) {
                    System.out.println("Richiesta: " + request);
                    String[] parts = request.split(" ");
                    switch (parts[0]) {
                        case "quit":
                            closed = true;
                            break;
                        case "help":
                            to.println("Comandi del servizio:");
                            to.println("'list': mostra l'elenco di tutti i conti correnti presenti.\nUtilizzo: 'list'");
                            to.println("'open': permette di creare un conto corrente.\nUtilizzo: 'open' 'nome conto' 'bilancio iniziale'");
                            to.println("'transfer': permette di transferire denaro da un conto ad un altro.\nUtilizzo: 'transfer' 'somma' 'nome conto 1' 'nome conto 2'");
                            to.println("'transfer_i': permette di transferire denaro da un conto ad un altro, ma in maniera interattiva.\nUtilizzo: 'transfer' 'nome conto 1' 'nome conto 2'");
                            to.println("Una volta aperta la sessione interattiva, si possono usare i comandi:\n\t':move': per spostare denaro. Utilizzo ':move' 'somma'\n\t':end': per terminare la sessione interattiva. Utilizzo: ':end'");
                            to.print("");
                            break;
                        case "open":
                            if (parts.length == 3) {
                                String accountName = parts[1];
                                if(!contoPresente(accountName.toLowerCase())){
                                    double accountAmount = Double.parseDouble(parts[2]);
                                    to.println("Account creato!\tNome: " + accountName + "\tBilancio iniziale: " + accountAmount);
                                    BankAccount b = new BankAccount(accountName, accountAmount);
                                    bankAccounts.add(b);
                                }else{
                                    to.println("Esiste già un conto con questo nome! Conto non creato correttamente.");
                                }
                            } else {
                                to.println("Comando scritto in maniera errata! Scrivere 'help' per ulteriori informazioni.");
                            }
                            break;
                        case "list":
                        // PERCHE NON VA UN CICLO O UN ITERATOR SUL VECTOR?
                            
                            break;
                        case "transfer":
                            if (parts.length == 4) {
                                double amount = Double.parseDouble(parts[1]);
                                String name1 = parts[2];
                                String name2 = parts[3];
                                BankAccount b1 = getAccountByName(name1.toLowerCase());
                                BankAccount b2 = getAccountByName(name2.toLowerCase());
                                if(b1 == null || b2 == null){
                                    to.println("Uno dei due conti o entrambi i conti non sono corretti!");
                                }else{
                                    if(b1.transfer(amount, b2)){
                                        to.println("Operazione effettuata con successo! Trasferiti " + amount + " dal conto " + name1 + " al conto " + name2 + "!");
                                    }else{
                                        to.println("Transazione negata, bilancio non sufficiente al trasferimento!");
                                    }   
                                }
                            } else {
                                to.println("Comando scritto in maniera errata! Scrivere 'help' per ulteriori informazioni.");
                            }
                            break;
                        case "transfer_i":
                                
                                break;
                        default:
                            to.println("Comando sconosciuto! Scrivere 'help' per ulteriori informazioni.");
                    }
                } else {
                    to.println("quit");
                    break;
                }
            }

            to.println("quit");
            s.close();
            System.out.println("Terminato");
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

    private BankAccount getAccountByName(String name){
        for (BankAccount b : bankAccounts) {
            if(b.getName().toLowerCase().equals(name)){
                return b;
            }
        }
        return null;
    }

}
