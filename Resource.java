import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Resource {

    private Vector<BankAccount> bankAccounts;
    private ConcurrentHashMap<String, BankAccount> busyAccounts;

    public Resource() {
        bankAccounts = new Vector<BankAccount>();
        busyAccounts = new ConcurrentHashMap<String, BankAccount>();
    }

    /*
     * Stampa la lista dei conti correnti
     */
    public synchronized String list() {
        String list = "";
        if(!bankAccounts.isEmpty()){
            for (BankAccount b : bankAccounts) {
                list = list + b.toString();
            }
        }

        return list;
    }

    /*
     * Stampa la lista delle transazioni di un singolo conto corrente
     */
    public synchronized String listT(String name) throws InterruptedException {
        BankAccount b = getAccountByName(name);

        while (busyAccounts.contains(b)) {
            wait();
        }

        String transactions = "";
        if(!b.getTransactions().isEmpty()){
            for (Transaction t : b.getTransactions()) {
                transactions = transactions + "\t" + t.toString() + "\n";
            }
        }
        
        notifyAll();

        return transactions;
    }

    /*
     * Permette l'apertura di un conto corrente (se già presente, non viene creato)
     */
    public synchronized boolean open(String name, double amount) {
        if(contoPresente(name)){
            return false;
        }
        
        BankAccount b = new BankAccount(name, amount);
        bankAccounts.add(b);
        
        return true;
    }

    /*
     * Permette la chiusura di un conto corrente
     */
    public synchronized boolean close(String name) throws InterruptedException {
        if(!contoPresente(name)){
            return false;
        }
        
        BankAccount b = getAccountByName(name);

        while (busyAccounts.contains(b)) {
            wait();
        }
        bankAccounts.remove(b);
        notifyAll();

        return true;
    }

    /*
     * Permette il trasferimento da un conto ad un altro
     */
    public synchronized boolean transfer(String name1, String name2, double amount) throws InterruptedException {
        BankAccount b1 = getAccountByName(name1);
        BankAccount b2 = getAccountByName(name2);

        while (busyAccounts.contains(b1) || busyAccounts.contains(b2)) {
            wait();
        }

        busyAccounts.put(b1.getName(), b1);
        busyAccounts.put(b2.getName(), b2);

        boolean bool = b1.transfer(amount, b2);

        busyAccounts.remove(b1.getName());
        busyAccounts.remove(b2.getName());

        notifyAll();

        return bool;
    }

    /*
     * Permette di iniziare la sessione interattiva tra due conti
     */
    public synchronized void start_transfer_i(String name1, String name2) throws InterruptedException {
        BankAccount b1 = getAccountByName(name1);
        BankAccount b2 = getAccountByName(name2);

        while (busyAccounts.contains(b1) || busyAccounts.contains(b2)) {
            wait();
        }

        busyAccounts.put(b1.getName(), b1);
        busyAccounts.put(b2.getName(), b2);

        notifyAll();
    }

    /*
     * Effettua i trasferimenti nella sessione interattiva
     */
    public synchronized boolean transfer_i(String name1, String name2, double amount) throws InterruptedException {
        BankAccount b1 = getAccountByName(name1);
        BankAccount b2 = getAccountByName(name2);

        return b1.transfer(amount, b2);
    }

    /*
     * Permette di chiudere la sessione interattiva tra due conti
     */
    public synchronized void end_transfer_i(String name1, String name2) throws InterruptedException {
        BankAccount b1 = getAccountByName(name1);
        BankAccount b2 = getAccountByName(name2);

        busyAccounts.remove(b1.getName());
        busyAccounts.remove(b2.getName());

        notifyAll();
    }

    /*
     * Controlla la presenza di un conto corrente nella struttura dati
     */
    public boolean contoPresente(String name) {
        for (BankAccount b : bankAccounts) {
            if (b.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Restituisce il conto corrente tramite l'identificativo nome del conto
     */
    public BankAccount getAccountByName(String name) {
        for (BankAccount b : bankAccounts) {
            if (b.getName().equalsIgnoreCase(name)) {
                return b;
            }
        }
        return null;
    }

    public Vector<BankAccount> getBankAccounts() {
        return bankAccounts;
    }

}
