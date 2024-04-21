import java.util.List;
import java.util.Scanner;

public class ATMView {
    private ATMModel model;
    private Scanner scanner;

    public ATMView(ATMModel model) {
        this.model = model;
        this.scanner = new Scanner(System.in);
    }

    public int promptForPIN() {
        System.out.println("Enter your PIN: ");
        return scanner.nextInt();
    }

    public void displayBalance(double balance) {
        System.out.println("Your balance is: " + balance);
    }

    public void promptForAmount() {
        System.out.println("Enter the amount you want to withdraw: ");
        double amount = scanner.nextDouble();
        // Process the amount
    }

    public void displayStatement(List<String> statement) {
        if (!statement.isEmpty()) {
            System.out.println("Statement:");
            for (String transaction : statement) {
                System.out.println(transaction);
            }
        } else {
            System.out.println("No transactions found for the given account.");
        }
    }
}
