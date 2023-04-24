import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Resource {
    
    private Vector<BankAccount> bankAccounts;
    private ConcurrentHashMap<String, BankAccount> busyAccounts;

    public Resource(){
        bankAccounts = new Vector<BankAccount>();
        busyAccounts = new ConcurrentHashMap<String, BankAccount>();
    }

    public synchronized String list() throws InterruptedException { // CHIEDERE SE VA BENE GESTIRE SENZA WHILE
        //while(!busyAccounts.isEmpty()){
        //    wait();
        //}

        String list = "";
        for (BankAccount b : bankAccounts) {
            list = list + b.toString();
        }

        //notifyAll();

        return list;
    }

    public synchronized String listT(BankAccount b) throws InterruptedException {
        while(busyAccounts.contains(b)){
            wait();
        }

        String transactions = "Transazioni conto corrente " + b.getName() + " :\n";
        for (Transaction t : b.getTransactions()) {
            transactions = transactions + "\t" + t.toString() + "\n";
        }

        notifyAll();

        return transactions;
    }

    public synchronized void open(BankAccount b){
        bankAccounts.add(b);
    }

    public synchronized void close(BankAccount b) throws InterruptedException {
        while(busyAccounts.contains(b)) {
            wait();
        }

        bankAccounts.remove(b);

        notifyAll();
    }

    public synchronized boolean transfer(BankAccount b1, BankAccount b2, double amount) throws InterruptedException {
        while(busyAccounts.contains(b1) || busyAccounts.contains(b2)){
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

    public synchronized void start_transfer_i(BankAccount b1, BankAccount b2) throws InterruptedException {
        while(busyAccounts.contains(b1) || busyAccounts.contains(b2)){
            wait();
        }

        busyAccounts.put(b1.getName(), b1);
        busyAccounts.put(b2.getName(), b2);

        notifyAll();
    }

    public synchronized boolean transfer_i(BankAccount b1, BankAccount b2, double amount) throws InterruptedException {
        return b1.transfer(amount, b2);
    }

    public synchronized void end_transfer_i(BankAccount b1, BankAccount b2) throws InterruptedException {
        
        busyAccounts.remove(b1.getName());
        busyAccounts.remove(b2.getName());

        notifyAll();
    }

    public Vector<BankAccount> getBankAccounts(){
        return bankAccounts;
    }

}
