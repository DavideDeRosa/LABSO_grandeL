import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler implements Runnable {

    private Socket s;
    private Resource r;
    private boolean state;
    private String b1;
    private String b2;
    private Scanner from;
    private PrintWriter to;
    private Timer timer;
    private boolean closed;

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
                    /*
                    Viene prima determinato quale set di comandi leggere, tramite la variabile state
                    Successivamente i due switch gestiscono i diversi comandi implementati
                    */
                    if (state) {
                        /*
                        Comandi inerenti alla sessione interattiva
                        */
                        switch (parts[0]) {
                            case "quit":
                                closed = true;
                                closeAll();
                                break;
                            case ":help":
                                to.println(
                                        "Una volta aperta la sessione interattiva, si possono usare i comandi:\n"
                                                + ":move: per spostare denaro.\n\tUtilizzo :move <1000>.\n"
                                                + ":end: per terminare la sessione interattiva.\n\tUtilizzo: :end.\n");
                                break;
                            case ":move":
                                if (parts.length == 2) {
                                    try {
                                        double amount = Double.parseDouble(parts[1]);
                                        if (r.transfer_i(b1, b2, amount)) {
                                            to.println("Operazione effettuata con successo!\nTrasferiti " + amount
                                                    + " dal conto " + b1 + " al conto " + b2
                                                    + ".\n");
                                        } else {
                                            to.println(
                                                    "Attenzione!\nTransazione negata, bilancio non sufficiente al trasferimento.\n");
                                        }
                                    } catch (Exception e) {
                                        to.println(
                                                "Comando scritto in maniera errata!\n:move <1000>.\n");
                                    }
                                } else {
                                    to.println(
                                            "Comando scritto in maniera errata!\n:move <1000>.\n");
                                }
                                break;
                            case ":end":
                                to.println("Stato di transazione interattiva concluso tra il conto " + b1
                                        + " ed il conto " + b2 + "\n");
                                state = false;
                                r.end_transfer_i(b1, b2);
                                b1 = "";
                                b2 = "";
                                timer.cancel();
                                break;
                            default:
                                to.println("Comando non riconosciuto! Scrivere :help per saperne di piu'.\n");
                        }
                    } else {
                        /*
                        Comandi generali del programma
                        */
                        switch (parts[0]) {
                            case "quit":
                                closed = true;
                                break;
                            case "help":
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
                                        double accountAmount = Double.parseDouble(parts[2]);
                                        if (r.open(accountName, accountAmount)) {
                                            to.println(
                                                    "Account creato!\nNome: " + accountName + "\tBilancio iniziale: "
                                                            + accountAmount + "\n");
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
                                if (parts.length == 1) {
                                    if (!r.list().equals("")) {
                                        to.println(r.list());
                                    } else {
                                        to.println(
                                                "Attenzione!\nLa struttura dati non contiene alcuno conto corrente.\n");
                                    }
                                } else if (parts.length == 2) {
                                    if (!r.list().equals("")) {
                                        String name = parts[1];
                                        if (!r.contoPresente(name)) {
                                            to.println(
                                                    "Attenzione!\nConto corrente non esistente o scritto in maniera errata.\n");
                                        } else {
                                            if (r.listT(name).equals("")) {
                                                to.println(
                                                    "Attenzione!\nIl conto corrente non ha ancora effettuato alcuna transazione.\n");
                                            } else {
                                                to.println(r.listT(name));
                                            }
                                        }
                                    } else {
                                        to.println(
                                                "Attenzione!\nLa struttura dati non contiene tale conto corrente.\n");
                                    }
                                } else {
                                    to.println(
                                            "Attenzione!\nConto corrente non esistente o scritto in maniera errata.\n");
                                }
                                break;
                            case "transfer":
                                if (parts.length == 4) {
                                    try {
                                        double amount = Double.parseDouble(parts[1]);
                                        String name1 = parts[2];
                                        String name2 = parts[3];
                                        
                                        if (!r.contoPresente(name1) || !r.contoPresente(name2)) {
                                            to.println(
                                                    "Attenzione!\nUno dei due conti e' inesistente o non e' scritto in maniera corretta.\n");
                                        }
                                        if (name1.equalsIgnoreCase(name2)) { 
                                            to.println(
                                                    "Attenzione!\nNon e' possibile compiere un trasferimento nello stesso conto.\n");
                                        } else {
                                            if (r.transfer(name1, name2, amount)) {
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
                                        b1 = parts[1];
                                        b2 = parts[2];
                                        
                                        if (!r.contoPresente(b1) || !r.contoPresente(b2)) {
                                            to.println(
                                                    "Attenzione!\nUno dei due conti e' inesistente o non e' scritto in maniera corretta.\n");
                                        }
                                        if (b1.equalsIgnoreCase(b2)) {
                                            to.println(
                                                    "Attenzione!\nNon e' possibile compiere un trasferimento nello stesso conto.\n");
                                        } else {
                                            r.start_transfer_i(b1, b2);
                                            state = true;
                                            to.println("Stato di transazione interattiva attivato tra il conto "
                                                    + b1 + " ed il conto " + b2 + ":");
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
                                    if (r.close(name)) {
                                        to.println("Conto chiuso correttamente!\n");
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
                                to.println("Comando non riconosciuto!\nDigita help per saperne di piu'.\n");
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

    /*
    Permette di chiudere la sessione interattiva in caso di comando "quit" durante la sessione stessa
    */
    private void closeAll() throws InterruptedException {
        to.println("Stato di transazione interattiva concluso tra il conto " + b1 + " ed il conto "
                + b2 + "\n");
        state = false;
        r.end_transfer_i(b1, b2);
        b1 = "";
        b2 = "";
        timer.cancel();
    }

    /*
    Imposta un timer all'avvio della sessione interattiva, che permette la chiusura della sessione entro 1 minuto dal suo avvio
    */
    private void startTimer() {

        timer = new Timer();

        TimerTask tt = new TimerTask() {
            public void run() {
                try {
                    to.println("Stato di transazione interattiva concluso tra il conto " + b1
                            + " ed il conto " + b2 + "\n");
                    state = false;
                    r.end_transfer_i(b1, b2);
                    b1 = "";
                    b2 = "";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.schedule(tt, 60000);
    }

}