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

    public void transfer(double amount, BankAccount b){
        if(balance >= amount){
            balance = balance - amount;
            b.setBalance(b.getBalance() + amount);
            this.addTransaction(new Transaction(-(amount), b.getName()));   //AMMONTARE IN VALORE ASSOLUTO O DECIDIAMO DI TENERE NEGATIVO PER CHI FA IL TRASFERIMENTO?
            System.out.println("Transazione effettuata da " + name + ": " + lastTransaction);
            b.addTransaction(new Transaction(amount, this.getName()));
        }else{
            System.out.println("Transazione negata, bilancio non sufficiente al trasferimento!");
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
        return "Nome conto corrente: " + name + "\tBilancio: " + balance + "\tUltima transazione: " + lastTransaction + "\n\t";
    }
}
