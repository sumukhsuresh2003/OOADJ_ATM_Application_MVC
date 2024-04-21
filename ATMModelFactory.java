public class ATMModelFactory {
    private static ATMModelFactory instance;

    // Private constructor to prevent instantiation from outside the class
    private ATMModelFactory() {
    }

    // Static method to provide access to the single instance of ATMModelFactory
    public static ATMModelFactory getInstance() {
        if (instance == null) {
            instance = new ATMModelFactory();
        }
        return instance;
    }

    // Factory method to create instances of ATMModel
    public AbstractATMModel createATMModel() {
        return ATMModel.getInstance();
    }
}
