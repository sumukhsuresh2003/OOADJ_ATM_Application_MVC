import java.util.Scanner;

public class ATM {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Create a new ATM model
        ATMModel model = new ATMModel();

        // Create a new ATM view
        ATMView view = new ATMView(model);

        // Create a new ATM controller
        ATMController controller = new ATMController(model, view);

        // Start the ATM simulation
        controller.start();
    }
}
