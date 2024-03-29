import java.util.Vector;

public class BankAccount {
    
    private String name;
    private double balance;
    private Vector<Transaction> transactions;
    private Transaction lastTransaction;

    public BankAccount(String name, double balance){
        this.name = name;
        this.balance = balance;
        transactions = new Vector<Transaction>();
    }

    /*
    Permette il trasferimento da un conto ad un altro
    */
    public synchronized boolean transfer(double amount, BankAccount b){
        if(balance >= amount){
            balance = balance - amount;
            b.setBalance(b.getBalance() + amount);
            this.addTransaction(new Transaction(-(amount), b.getName()));
            System.out.println("Transazione effettuata da " + name + ": " + lastTransaction);
            b.addTransaction(new Transaction(amount, this.getName()));
            return true;
        }else{
            System.out.println("Transazione negata, bilancio non sufficiente al trasferimento!");
            return false;
        }
    }

    public String getName(){
        return name;
    }

    public void setName(String n){
        this.name=n;
    }

    public double getBalance(){
        return balance;
    }
    
    public void setBalance(double b){
        this.balance=b;
    }

    public Transaction getLastTransaction(){
        return lastTransaction;
    }

    public Vector<Transaction> getTransactions(){
        return transactions;
    }

    public void addTransaction(Transaction t){
        transactions.add(t);
        lastTransaction = t;
    }

    @Override
    public String toString(){
        if(lastTransaction == null){
            return "Nome conto corrente: " + name + "\tBilancio: " + balance + "\nUltima transazione: nessuna transazione\n";
        }else{
            return "Nome conto corrente: " + name + "\tBilancio: " + balance + "\nUltima transazione: " + lastTransaction + "\n";
        }
    }
}
