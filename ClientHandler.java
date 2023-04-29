import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler implements Runnable {

    Socket s;
    Resource r;
    boolean state;
    BankAccount b1;
    BankAccount b2;
    Scanner from;
    PrintWriter to;
    Timer timer;
    boolean closed;

    public ClientHandler(Socket s, Resource r) {
        this.s = s;
        this.r = r;
        state = false;
        closed = false;
    }

    @Override
    public void run() {
        try {
            from = new Scanner(s.getInputStream());
            to = new PrintWriter(s.getOutputStream(), true);

            System.out.println("Thread " + Thread.currentThread() + " in ascolto...");

            while (!closed) {
                String request = from.nextLine();
                if (!Thread.interrupted()) {
                    System.out.println("Richiesta: " + request);
                    String[] parts = request.split(" ");
                    if (state) {
                        switch (parts[0]) {
                            case "quit":
                                closed = true;
                                closeAll();
                                break;
                            case ":help":
                                to.println(
                                        "Una volta aperta la sessione interattiva, si possono usare i comandi:\n"
                                                + ":move: per spostare denaro.\n\tUtilizzo :move <1000>.\n "
                                                + ":end: per terminare la sessione interattiva.\n\tUtilizzo: :end.\n");
                                break;
                            case ":move":
                                if (parts.length == 2) {
                                    try {
                                        double amount = Double.parseDouble(parts[1]);
                                        if (r.transfer_i(b1, b2, amount)) {
                                            to.println("Operazione effettuata con successo!\nTrasferiti " + amount
                                                    + " dal conto " + b1.getName() + " al conto " + b2.getName()
                                                    + ".\n");
                                        } else {
                                            to.println(
                                                    "Attenzione!\nTransazione negata, bilancio non sufficiente al trasferimento.\n");
                                        }
                                    } catch (Exception e) {
                                        to.println(
                                                "Comando scritto in maniera errata!\n:move <1000>.");
                                    }
                                } else {
                                    to.println(
                                            "Comando scritto in maniera errata!\n:move <1000>.");
                                }
                                break;
                            case ":end":
                                to.println("Stato di transazione interattiva concluso tra il conto " + b1.getName()
                                        + " ed il conto " + b2.getName() + "\n");
                                state = false;
                                r.end_transfer_i(b1, b2);
                                b1 = null;
                                b2 = null;
                                timer.cancel();
                                break;
                            default:
                                to.println("Comando non riconosciuto! Scrivere :help per saperne di piu'.\n");
                        }
                    } else {
                        switch (parts[0]) {
                            case "quit":
                                closed = true;
                                break;
                            case "help": // TO-DO: Modificare se ci sono stati cambiamenti!!!
                                to.println(
                                        "Comandi del servizio:");
                                to.println(
                                        "- list: mostra l'elenco di tutti i conti correnti presenti.\n\tUtilizzo: list.");
                                to.println(
                                        "- list conto corrente: mostra l'elenco di tutte le transazioni effettuate in un conto corrente.\n\tUtilizzo: list <nome_conto>.");
                                to.println(
                                        "- open: permette di creare un conto corrente.\n\tUtilizzo: open <nome_conto> <1000>.");
                                to.println(
                                        "- transfer: permette di transferire denaro da un conto ad un altro.\n\tUtilizzo: transfer <1000> <nome_conto_3> <nome_conto_2>.");
                                to.println(
                                        "- transfer_i: permette di transferire denaro da un conto ad un altro, ma in maniera interattiva.\n\tUtilizzo: transfer_i <nome_conto_1> <nome_conto_2>.");
                                to.println(
                                        "\tUna volta aperta la sessione interattiva, si possono usare i comandi:\n\t\t-- :move: per spostare denaro.\n\t\t\tUtilizzo :move <somma>.\n\t\t-- :end: per terminare la sessione interattiva.\n\t\t\tUtilizzo: :end.\n\t\t-- :help: per ricevere informazioni sui comandi.\n\t\t\tUtilizzo: :help.");
                                to.println(
                                        "- quit: termina l'esecuzione del programma.\n\tUtilizzo: quit.\n");
                                break;
                            case "open":
                                if (parts.length == 3) {
                                    try {
                                        String accountName = parts[1];
                                        if (!contoPresente(accountName.toLowerCase())) {
                                            double accountAmount = Double.parseDouble(parts[2]);
                                            to.println(
                                                    "Account creato!\nNome: " + accountName + "\tBilancio iniziale: "
                                                            + accountAmount + "\n");
                                            BankAccount b = new BankAccount(accountName, accountAmount);
                                            r.open(b);
                                        } else {
                                            to.println(
                                                    "Esiste già un conto con questo nome!\nConto non creato correttamente.\n");
                                        }
                                    } catch (Exception e) {
                                        to.println(
                                                "Comando scritto in maniera errata!\nopen <nome_conto> <1000>.\n");
                                    }
                                } else {
                                    to.println(
                                            "Comando scritto in maniera errata!\nopen <nome_conto> <1000>.\n");
                                }
                                break;
                            case "list":
                                if (parts.length == 1) { // LIST COMANDO NORMALE
                                    if (!r.getBankAccounts().isEmpty()) {
                                        to.println(r.list());
                                    } else {
                                        to.println(
                                                "Attenzione!\nLa struttura dati non contiene alcuno conto corrente.\n");
                                    }
                                } else if (parts.length == 2) { // LIST 'NOMECONTOCORRENTE' COMANDO CHE TI DA L'ELENCO
                                                                // DELLE TRANSAZIONI DI QUEL CONTO
                                    if (!r.getBankAccounts().isEmpty()) {
                                        String name = parts[1];
                                        BankAccount b = getAccountByName(name.toLowerCase());
                                        if (b == null) {
                                            to.println(
                                                    "Attenzione!\nConto corrente non esistente o non scritto in maniera errata.\n");
                                        } else {
                                            if (b.getTransactions().isEmpty()) {
                                                to.println(
                                                        "Attenzione!\nIl conto corrente non ha ancora effettuato alcuna transazione.\n");
                                            } else {
                                                to.println(r.listT(b));
                                            }
                                        }
                                    } else {
                                        to.println(
                                                "Attenzione!\nLa struttura dati non contiene tale conto corrente.\n");
                                    }
                                } else {
                                    to.println(
                                            "Attenzione!\nConto corrente non esistente o non scritto in maniera errata.\n");
                                }
                                break;
                            case "transfer":
                                if (parts.length == 4) {
                                    try {
                                        double amount = Double.parseDouble(parts[1]);
                                        String name1 = parts[2];
                                        String name2 = parts[3];
                                        BankAccount acc1 = getAccountByName(name1.toLowerCase());
                                        BankAccount acc2 = getAccountByName(name2.toLowerCase());
                                        if (acc1 == null || acc2 == null) {
                                            to.println(
                                                    "Attenzione!\nUno dei due conti e' inesistente o non e' scritto in maniera corretta.\n");
                                        }
                                        if (acc1 == acc2) { // NON SI PUO FARE COME SOTTO acc1.equals(acc2)???
                                            to.println(
                                                    "Attenzione!\nNon e' possibile compiere un trasferimento nello stesso conto.\n");
                                        } else {
                                            if (r.transfer(acc1, acc2, amount)) {
                                                to.println("Operazione effettuata con successo!\nTrasferiti " + amount
                                                        + " dal conto " + name1 + " al conto " + name2 + ".\n");
                                            } else {
                                                to.println(
                                                        "Attenzione!\nTransazione negata, bilancio non sufficiente al trasferimento.\n");
                                            }
                                        }
                                    } catch (Exception e) {
                                        to.println(
                                                "Comando scritto in maniera errata!\ntransfer <1000> <conto corrente mandante> <conto corrente destinatario>.\n");
                                    }
                                } else {
                                    to.println(
                                            "Comando scritto in maniera errata!\ntransfer <1000> <conto corrente mandante> <conto corrente destinatario>.\n");
                                }
                                break;
                            case "transfer_i":
                                if (parts.length == 3) {
                                    try {
                                        String name1 = parts[1];
                                        String name2 = parts[2];
                                        b1 = getAccountByName(name1.toLowerCase());
                                        b2 = getAccountByName(name2.toLowerCase());
                                        if (b1 == null || b2 == null) {
                                            to.println(
                                                    "Attenzione!\nUno dei due conti e' inesistente o non e' scritto in maniera corretta.\n");
                                        }
                                        if (b1.equals(b2)) {
                                            to.println(
                                                    "Attenzione!\nNon e' possibile compiere un trasferimento nello stesso conto.\n");
                                        } else {
                                            state = true;
                                            r.start_transfer_i(b1, b2);
                                            to.println("Stato di transazione interattiva attivato tra il conto "
                                                    + b1.getName() + " ed il conto " + b2.getName() + ":");
                                            to.println("Transazione interattiva valida per 1 minuto...\n");
                                            startTimer();
                                        }
                                    } catch (Exception e) {
                                        to.println(e.getMessage());
                                        to.println(
                                                "Comando scritto in maniera errata!\ntransfer_i <conto corrente mandante> <conto corrente destinatario>.\n");
                                    }
                                } else {
                                    to.println(
                                            "Comando scritto in maniera errata!\ntransfer_i <conto corrente mandante> <conto corrente destinatario>.\n");
                                }
                                break;
                            case "close":
                                if (parts.length == 2) {
                                    String name = parts[1];
                                    if (contoPresente(name)) {
                                        r.close(getAccountByName(name));
                                        to.println("Conto chiuso correttamente!");
                                    } else {
                                        to.println(
                                                "Attenzione!\nConto corrente e' inesistente o non e' scritto in maniera corretta.\n");
                                    }
                                } else {
                                    to.println(
                                            "Comando scritto in maniera errata!\nclose <conto corrente>.\n");
                                }
                                break;
                            default:
                                to.println("Comando non riconosciuto!\ndDigita help per saperne di piu'.\n");
                        }
                    }
                } else {
                    to.println("quit");
                    break;
                }
            }
            to.println("quit");
            s.close();
            System.out.println("Terminato...");
        } catch (IOException e) {
            System.err.println("ClientHandler: IOException caught: " + e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean contoPresente(String name) {
        for (BankAccount b : r.getBankAccounts()) {
            if (b.getName().toLowerCase().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private BankAccount getAccountByName(String name) {
        for (BankAccount b : r.getBankAccounts()) {
            if (b.getName().toLowerCase().equals(name)) {
                return b;
            }
        }
        return null;
    }

    private void closeAll() throws InterruptedException {
        to.println("Stato di transazione interattiva concluso tra il conto " + b1.getName() + " ed il conto "
                + b2.getName() + "\n");
        state = false;
        r.end_transfer_i(b1, b2);
        b1 = null;
        b2 = null;
        timer.cancel();
    }

    private void startTimer() {

        timer = new Timer();

        TimerTask tt = new TimerTask() {
            public void run() {
                try {
                    to.println("Stato di transazione interattiva concluso tra il conto " + b1.getName()
                            + " ed il conto " + b2.getName() + "\n");
                    state = false;
                    r.end_transfer_i(b1, b2);
                    b1 = null;
                    b2 = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.schedule(tt, 60000);
    }

}
