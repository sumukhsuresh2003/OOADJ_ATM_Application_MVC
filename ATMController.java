import java.util.List;
import java.util.Scanner;

public class ATMController {
    private ATMModel model;
    private ATMView view;
    private Scanner scanner;

    public ATMController(ATMModel model, ATMView view) {
        this.model = model;
        this.view = view;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the ATM!");

        while (true) {
            System.out.println("\nPlease select an option:");
            System.out.println("1. Cash Withdrawal");
            System.out.println("2. Balance Inquiry");
            System.out.println("3. Change PIN");
            System.out.println("4. Account Deactivation");
            System.out.println("5. View Statement");
            System.out.println("6. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    performCashWithdrawal();
                    break;
                case 2:
                    performBalanceInquiry();
                    break;
                case 3:
                    performChangePIN();
                    break;
                case 4:
                    performAccountDeactivation();
                    break;
                case 5:
                    performViewStatement();
                    break;
                case 6:
                    System.out.println("Thank you for using the ATM. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void performCashWithdrawal() {
        System.out.println("Enter your debit card number:");
        long debitCardNum = scanner.nextLong();
        System.out.println("Enter your PIN:");
        int pin = scanner.nextInt();
        System.out.println("Enter the amount you want to withdraw:");
        double amount = scanner.nextDouble();

        boolean success = model.withdrawCash(debitCardNum, pin, amount, "Cash Withdrawal");
        if (success) {
            System.out.println("Withdrawal successful. Please take your cash.");
        } else {
            System.out.println("Withdrawal failed. Please try again.");
        }
    }

    private void performBalanceInquiry() {
        System.out.println("Enter your debit card number:");
        long debitCardNum = scanner.nextLong();
        System.out.println("Enter your PIN:");
        int pin = scanner.nextInt();

        double balance = model.getBalance(debitCardNum, pin, "Balance Inquiry", false);
        if (balance != -1) {
            System.out.println("Your current balance is: " + balance);
        }
    }

    private void performChangePIN() {
        System.out.println("Enter your debit card number:");
        long debitCardNum = scanner.nextLong();
        System.out.println("Enter your current PIN:");
        int oldPIN = scanner.nextInt();
        System.out.println("Enter your new PIN:");
        int newPIN = scanner.nextInt();

        boolean success = model.changePIN(debitCardNum, oldPIN, newPIN, "Change PIN");
        if (success) {
            System.out.println("PIN changed successfully.");
        } else {
            System.out.println("Failed to change PIN. Please try again.");
        }
    }

    private void performAccountDeactivation() {
        System.out.println("Enter your debit card number:");
        long debitCardNum = scanner.nextLong();
        System.out.println("Enter your PIN:");
        int pin = scanner.nextInt();

        boolean success = model.deactivateAccount(debitCardNum, pin, "Account Deactivation");
        if (success) {
            System.out.println("Account deactivated successfully.");
        } else {
            System.out.println("Failed to deactivate account. Please try again.");
        }
    }

    private void performViewStatement() {
        System.out.println("Enter your debit card number:");
        long debitCardNum = scanner.nextLong();

        List<String> statement = model.getStatement(debitCardNum);
        if (!statement.isEmpty()) {
            System.out.println("Statement for Debit Card Number: " + debitCardNum);
            for (String transaction : statement) {
                System.out.println(transaction);
            }
        } else {
            System.out.println("No transactions found for the given debit card number.");
        }
    }
}
