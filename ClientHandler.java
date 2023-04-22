import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    Socket s;
    Resource r;

    public ClientHandler(Socket s, Resource r) {
        this.s = s;
        this.r = r;
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
                        case "help": // TO-DO: Modificare se ci sono stati cambiamenti!!!
                            to.println("Comandi del servizio:");
                            to.println("'list': mostra l'elenco di tutti i conti correnti presenti.\n\tUtilizzo: 'list'");
                            to.println("'list nome conto': mostra l'elenco di tutte le transazioni effettuate in un conto corrente.\n\tUtilizzo: 'list' 'nome conto'");
                            to.println("'open': permette di creare un conto corrente.\n\tUtilizzo: 'open' 'nome conto' 'bilancio iniziale'");
                            to.println("'transfer': permette di transferire denaro da un conto ad un altro.\n\tUtilizzo: 'transfer' 'somma' 'nome conto 1' 'nome conto 2'");
                            to.println("'transfer_i': permette di transferire denaro da un conto ad un altro, ma in maniera interattiva.\n\tUtilizzo: 'transfer' 'nome conto 1' 'nome conto 2'");
                            to.println("Una volta aperta la sessione interattiva, si possono usare i comandi:\n\t':move': per spostare denaro. Utilizzo ':move' 'somma'\n\t':end': per terminare la sessione interattiva. Utilizzo: ':end'");
                            to.println("'quit': termina l'esecuzione del programma.\n\tUtilizzo: 'quit'");
                            to.print("");
                            break;
                        case "open":
                            if (parts.length == 3) {
                                try{
                                    String accountName = parts[1];
                                    if(!contoPresente(accountName.toLowerCase())){
                                        double accountAmount = Double.parseDouble(parts[2]);
                                        to.println("Account creato!\tNome: " + accountName + "\tBilancio iniziale: " + accountAmount);
                                        BankAccount b = new BankAccount(accountName, accountAmount);
                                        r.open(b);
                                    }else{
                                        to.println("Esiste già un conto con questo nome! Conto non creato correttamente.");
                                    }
                                }catch(Exception e){
                                    to.println("Comando scritto in maniera errata! Scrivere 'help' per ulteriori informazioni.");
                                }
                            } else {
                                to.println("Comando scritto in maniera errata! Scrivere 'help' per ulteriori informazioni.");
                            }
                            break;
                        case "list":
                            if (parts.length == 1) { // LIST COMANDO NORMALE
                                if(!r.getBankAccounts().isEmpty()){
                                    to.println(r.list());
                                   }else{
                                    to.println("Lista vuota!");
                                   }
                            }else if(parts.length == 2){ // LIST 'NOMECONTOCORRENTE' COMANDO CHE TI DA L'ELENCO DELLE TRANSAZIONI DI QUEL CONTO
                                if(!r.getBankAccounts().isEmpty()){
                                    String name = parts[1];
                                    BankAccount b = getAccountByName(name.toLowerCase());
                                    if(b == null){
                                        to.println("Il conto corrente non esiste o non è corretto!");
                                    }else{
                                        if(b.getTransactions().isEmpty()){
                                            to.println("Il conto corrente non ha transazioni!");
                                        }else{
                                            to.println(r.listT(b));
                                        }
                                    }
                                   }else{
                                    to.println("Non sono presenti conti correnti!");
                                   }
                            }else{
                                to.println("Comando scritto in maniera errata! Scrivere 'help' per ulteriori informazioni.");
                            }
                            break;
                        case "transfer":
                            if (parts.length == 4) {
                                try{
                                    double amount = Double.parseDouble(parts[1]);
                                    String name1 = parts[2];
                                    String name2 = parts[3];
                                    BankAccount b1 = getAccountByName(name1.toLowerCase());
                                    BankAccount b2 = getAccountByName(name2.toLowerCase());
                                    if(b1 == null || b2 == null){
                                        to.println("Uno dei due conti o entrambi i conti non sono corretti!");
                                    }else{
                                        if(r.transfer(b1, b2, amount)){
                                            to.println("Operazione effettuata con successo! Trasferiti " + amount + " dal conto " + name1 + " al conto " + name2 + "!");
                                        }else{
                                            to.println("Transazione negata, bilancio non sufficiente al trasferimento!");
                                        }   
                                    }
                                }catch(Exception e){
                                    to.println("Comando scritto in maniera errata! Scrivere 'help' per ulteriori informazioni.");
                                }
                            } else {
                                to.println("Comando scritto in maniera errata! Scrivere 'help' per ulteriori informazioni.");
                            }
                            break;
                        case "transfer_i":
                                // TO-DO
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
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    private boolean contoPresente(String name){
        for (BankAccount b : r.getBankAccounts()) {
            if(b.getName().toLowerCase().equals(name)){
                return true;
            }
        }
        return false;
    }

    private BankAccount getAccountByName(String name){
        for (BankAccount b : r.getBankAccounts()) {
            if(b.getName().toLowerCase().equals(name)){
                return b;
            }
        }
        return null;
    }

}
