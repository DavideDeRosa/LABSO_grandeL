import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Resource {

    private Vector<BankAccount> bankAccounts;
    private ConcurrentHashMap<String, BankAccount> busyAccounts;

    public Resource() {
        bankAccounts = new Vector<BankAccount>();
        busyAccounts = new ConcurrentHashMap<String, BankAccount>();
    }

    public synchronized String list() {
        String list = "";
        if(!bankAccounts.isEmpty()){
            for (BankAccount b : bankAccounts) {
                list = list + b.toString();
            }
        }

        return list;
    }

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

    public synchronized boolean open(String name, double amount) {
        if(contoPresente(name)){
            return false;
        }
        
        BankAccount b = new BankAccount(name, amount);
        bankAccounts.add(b);
        
        return true;
    }

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

    public synchronized boolean transfer_i(String name1, String name2, double amount) throws InterruptedException {
        BankAccount b1 = getAccountByName(name1);
        BankAccount b2 = getAccountByName(name2);

        return b1.transfer(amount, b2);
    }

    public synchronized void end_transfer_i(String name1, String name2) throws InterruptedException {
        BankAccount b1 = getAccountByName(name1);
        BankAccount b2 = getAccountByName(name2);

        busyAccounts.remove(b1.getName());
        busyAccounts.remove(b2.getName());

        notifyAll();
    }

    public boolean contoPresente(String name) {
        for (BankAccount b : bankAccounts) {
            if (b.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

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
