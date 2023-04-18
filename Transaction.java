import java.time.LocalDateTime;

public class Transaction {

    private double amount;
    private String date; 
    private String otherBankAccount;   

    public Transaction(double amount, String otherBankAccount){
        this.amount = amount;
        this.otherBankAccount = otherBankAccount;
        
        LocalDateTime ldt = LocalDateTime.now();
        date = ldt.getDayOfMonth() + "/" + ldt.getMonthValue() + "/" + ldt.getYear();
    }

    @Override
    public String toString(){
        return "Data transazione: " + date + "\tAmmontare di denaro: " + amount + "\tAltro conto corrente: " + otherBankAccount;
    }
}
 