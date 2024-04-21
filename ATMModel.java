import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractATMModel {
    protected Connection connection;

    public AbstractATMModel() {
        try {
            // Establish database connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/java_project", "root", "QueSt20$6*ad#4");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public abstract List<String> getStatement(long debitCardNum);
    public abstract double getBalance(long debitCardNum, int pin, String transactionType, boolean flag);
    public abstract boolean withdrawCash(long debitCardNum, int pin, double amount, String transactionType);
    public abstract boolean changePIN(long debitCardNum, int oldPIN, int newPIN, String transactionType);
    public abstract boolean deactivateAccount(long debitCardNum, int pin, String transactionType);

    protected boolean checkCardValidity(long debitCardNum) {
        try {
            String query = "SELECT expiry_date, status_of_card FROM ATM_ACCOUNTS_DB WHERE debit_card_num = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, debitCardNum);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                LocalDateTime expiryDateTime = resultSet.getTimestamp("expiry_date").toLocalDateTime();
                boolean cardStatus = resultSet.getBoolean("status_of_card");
                return !LocalDateTime.now().isAfter(expiryDateTime) && cardStatus;
            } else {
                System.out.println("Invalid debit card number.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void updateTransactionType(long debitCardNum, String transactionType) {
        try {
            String query = "UPDATE ATM_ACCOUNTS_DB SET transaction_type = ? WHERE debit_card_num = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, transactionType);
            statement.setLong(2, debitCardNum);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void insertTransactionHistory(long debitCardNum, String transactionType, double amount) {
        try {
            String query = "INSERT INTO TRANSACTION_HISTORY (debit_card_num, transaction_type, amount, transaction_time) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, debitCardNum);
            statement.setString(2, transactionType);
            statement.setDouble(3, amount);
            statement.setObject(4, LocalDateTime.now());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void printTransactionInfo(long debitCardNum, String transactionType) {
        try {
            String query = "SELECT acc_number, acc_name, acc_address, acc_type FROM ATM_ACCOUNTS_DB WHERE debit_card_num = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, debitCardNum);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String accNumber = resultSet.getString("acc_number");
                String accName = resultSet.getString("acc_name");
                String accAddress = resultSet.getString("acc_address");
                String accType = resultSet.getString("acc_type");
                LocalDateTime transactionTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTransactionTime = transactionTime.format(formatter);

                // Generate PDF with account details and transaction
                //generateTransactionPDF(accNumber, accName, accAddress, accType, transactionType, formattedTransactionTime, "" + debitCardNum);
            } else {
                System.out.println("Invalid debit card number.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void generateTransactionPDF(String accNumber, String accName, String accAddress, String accType, String transactionType, String transactionTime, String debitCardNum) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("transaction_details.pdf"));
            document.open();
            Font bigHeadingFont = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD);
            Font bigFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
            Font bigF = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
            Paragraph bigHeading = new Paragraph("Transaction Details", bigHeadingFont);
            Paragraph bigdebit = new Paragraph("Debit Card Number: " + debitCardNum, bigFont);
            Paragraph bigfo = new Paragraph("Account Details:", bigF);
            Paragraph bigfo2 = new Paragraph("Transaction Details:", bigF);

            bigHeading.setAlignment(Element.ALIGN_CENTER);
            bigdebit.setAlignment(Element.ALIGN_CENTER);
            bigfo.setAlignment(Element.ALIGN_LEFT);

            document.add(bigHeading);
            document.add(bigdebit);
            document.add(Chunk.NEWLINE);
            document.add(bigfo);

            //document.add(new Paragraph("Account Details:"));
            document.add(new Paragraph("Account Number: " + accNumber));
            document.add(new Paragraph("Name: " + accName));
            document.add(new Paragraph("Address: " + accAddress));
            document.add(new Paragraph("Account Type: " + accType));
            document.add(new Paragraph("\n"));
            //document.add(new Paragraph("Transaction History:"));
            document.add(bigfo2);
            // Create a table for transaction details
            PdfPTable table = new PdfPTable(3); // 3 columns: Transaction Type, Amount, Transaction Time
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Define table header font style
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);

            // Set table header style
            PdfPCell headerCell1 = new PdfPCell(new Phrase("Transaction Type", headerFont));
            headerCell1.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell1.setPadding(8);
            headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            PdfPCell headerCell2 = new PdfPCell(new Phrase("Amount", headerFont));
            headerCell2.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell2.setPadding(8);
            headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            PdfPCell headerCell3 = new PdfPCell(new Phrase("Transaction Time", headerFont));
            headerCell3.setBackgroundColor(BaseColor.DARK_GRAY);
            headerCell3.setPadding(8);
            headerCell3.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Add table headers
            table.addCell(headerCell1);
            table.addCell(headerCell2);
            table.addCell(headerCell3);

            // Define alternate row colors
            BaseColor lightGray = new BaseColor(240, 240, 240);
            BaseColor whiteColor = BaseColor.WHITE;

            // Get transaction history
            String query = "SELECT transaction_type, amount, transaction_time FROM TRANSACTION_HISTORY WHERE debit_card_num = ? ORDER BY transaction_time";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, debitCardNum); // Use setString instead of setLong
            ResultSet resultSet = statement.executeQuery();

            // Flag for alternating row colors
            boolean isEvenRow = true;

            while (resultSet.next()) {
                String transType = resultSet.getString("transaction_type");
                double amount = resultSet.getDouble("amount");
                LocalDateTime transTime = resultSet.getTimestamp("transaction_time").toLocalDateTime();
                DateTimeFormatter transFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTransTime = transTime.format(transFormatter);

                // Set row background color
                BaseColor rowColor = isEvenRow ? whiteColor : lightGray;

                // Set row style
                PdfPCell cell1 = new PdfPCell(new Phrase(transType));
                cell1.setBackgroundColor(rowColor);
                cell1.setPadding(8);
                PdfPCell cell2 = new PdfPCell(new Phrase(Double.toString(amount)));
                cell2.setBackgroundColor(rowColor);
                cell2.setPadding(8);
                PdfPCell cell3 = new PdfPCell(new Phrase(formattedTransTime));
                cell3.setBackgroundColor(rowColor);
                cell3.setPadding(8);

                // Add transaction details to the table
                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);

                // Toggle row color
                isEvenRow = !isEvenRow;
            }

            // Add table to document
            document.add(table);

            document.close();
            System.out.println("PDF file generated successfully.");

            // Open the PDF file
            File file = new File("transaction_details.pdf");
            Desktop desktop = Desktop.getDesktop();
            if (file.exists()) {
                desktop.open(file);
            } else {
                System.out.println("PDF file not found.");
            }
        } catch (DocumentException | IOException | SQLException e) {
            e.printStackTrace();
        }
    }

}

public class ATMModel extends AbstractATMModel {
    private static ATMModel instance;
    private double atmCashBalance; // Global variable to store ATM cash balance
    private static final double MAX_WITHDRAWAL_AMOUNT = 20000.0; // Maximum withdrawal amount per transaction


    // Private constructor to prevent instantiation from outside the class
    ATMModel() {
        super();
        atmCashBalance = 80000.0; // Initialize ATM cash balance to a default value
    }

    // Static method to provide access to the single instance of ATMModel
    public static ATMModel getInstance() {
        if (instance == null) {
            instance = new ATMModel();
        }
        return instance;
    }

    @Override
    public List<String> getStatement(long debitCardNum) {
        List<String> statementList = new ArrayList<>();
        try {
            // Get account details
            if(checkCardValidity(debitCardNum))
            {
                String accountQuery = "SELECT acc_number, acc_name, acc_address, acc_type FROM ATM_ACCOUNTS_DB WHERE debit_card_num = ?";
                PreparedStatement accountStatement = connection.prepareStatement(accountQuery);
                accountStatement.setLong(1, debitCardNum);
                ResultSet accountResult = accountStatement.executeQuery();
                if (accountResult.next()) {
                    String accNumber = accountResult.getString("acc_number");
                    String accName = accountResult.getString("acc_name");
                    String accAddress = accountResult.getString("acc_address");
                    String accType = accountResult.getString("acc_type");

                    // Append account details to statement list
                    statementList.add("Account Number: " + accNumber);
                    statementList.add("Name: " + accName);
                    statementList.add("Address: " + accAddress);
                    statementList.add("Account Type: " + accType);

                    // Get transaction history
                    String query = "SELECT transaction_type, amount, transaction_time FROM TRANSACTION_HISTORY WHERE debit_card_num = ? ORDER BY transaction_time";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setLong(1, debitCardNum);
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        String transactionType = resultSet.getString("transaction_type");
                        double amount = resultSet.getDouble("amount");
                        LocalDateTime transactionTime = resultSet.getTimestamp("transaction_time").toLocalDateTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedTransactionTime = transactionTime.format(formatter);
                        String transactionInfo = "Transaction Type: " + transactionType + ", Amount: " + (amount == 0 ? "N/A" : amount) + ", Transaction Time: " + formattedTransactionTime;
                        statementList.add(transactionInfo);
                    }

                    // Generate PDF with account details and transaction
                    generateTransactionPDF(accNumber, accName, accAddress, accType, "Account Statement", LocalDateTime.now().toString(), "" + debitCardNum);

                } else {
                    System.out.println("Invalid debit card number.");
                }
            }
            else
            {
                System.out.println("Debit card is expired or inactive.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statementList;
    }

    @Override
    public double getBalance(long debitCardNum, int pin, String transactionType, boolean flag) {
        try {
            if (checkCardValidity(debitCardNum)) {
                updateTransactionType(debitCardNum, transactionType);
                //printTransactionInfo(debitCardNum, transactionType);
                String query = "SELECT acc_balance FROM ATM_ACCOUNTS_DB WHERE debit_card_num = ? AND acc_pin = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setLong(1, debitCardNum);
                statement.setInt(2, pin);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    double balance = resultSet.getDouble("acc_balance");
                    if (!flag) {
                        insertTransactionHistory(debitCardNum, transactionType, balance); // Record balance inquiry with amount 0
                    }
                    return balance;
                } else {
                    System.out.println("Invalid debit card number or PIN.");
                    return -1; // Indicate error
                }
            } else {
                System.out.println("Debit card is expired or inactive.");
                return -1; // Indicate error
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Indicate error
        }
    }

    @Override
    public boolean withdrawCash(long debitCardNum, int pin, double amount, String transactionType) {
        boolean flag = true;
        try {
            if (checkCardValidity(debitCardNum)) {
                if (amount > MAX_WITHDRAWAL_AMOUNT || amount > atmCashBalance) {
                    if(amount > MAX_WITHDRAWAL_AMOUNT)
                    {
                        System.out.println("Maximum withdrawal amount per transaction is Rs." + MAX_WITHDRAWAL_AMOUNT);
                    }
                    if(amount > atmCashBalance)
                    {
                        System.out.println("ATM Out Of Cash. Please try after sometime!");
                    }
                    return false;
                }
                updateTransactionType(debitCardNum, transactionType);
                printTransactionInfo(debitCardNum, transactionType);
                double currentBalance = getBalance(debitCardNum, pin, transactionType, flag);
                if (currentBalance >= amount) {
                    atmCashBalance -= amount; // Deduct the withdrawn amount from ATM cash balance
                    String query = "UPDATE ATM_ACCOUNTS_DB SET acc_balance = acc_balance - ?, date_time = ? WHERE debit_card_num = ? AND acc_pin = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setDouble(1, amount);
                    statement.setObject(2, LocalDateTime.now());
                    statement.setLong(3, debitCardNum);
                    statement.setInt(4, pin);
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        insertTransactionHistory(debitCardNum, transactionType, amount);
                        System.out.println("Cash withdrawal successful.");
                        System.out.println("Updated Account Balance: " + (currentBalance - amount));
                        return true;
                    } else {
                        System.out.println("Error updating account balance.");
                        return false;
                    }
                } else {
                    System.out.println("Insufficient balance.");
                    return false;
                }
            } else {
                System.out.println("Debit card is expired or inactive.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            //System.out.println("ATM has no available cash.");
            return false;
        }


    //return flag;
    }

    @Override
    public boolean changePIN(long debitCardNum, int oldPIN, int newPIN, String transactionType) {
        try {
            if (checkCardValidity(debitCardNum)) {
                updateTransactionType(debitCardNum, transactionType);
                printTransactionInfo(debitCardNum, transactionType);
                LocalDateTime transactionTime = LocalDateTime.now();
                String query = "UPDATE ATM_ACCOUNTS_DB SET acc_pin = ?, date_time = ? WHERE debit_card_num = ? AND acc_pin = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, newPIN);
                statement.setObject(2, transactionTime);
                statement.setLong(3, debitCardNum);
                statement.setInt(4, oldPIN);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    insertTransactionHistory(debitCardNum, transactionType, 0); // No amount for PIN change
                    //System.out.println("PIN changed successfully.");
                    return true;
                } else {
                    System.out.println("Error changing PIN.");
                    return false;
                }
            } else {
                System.out.println("Debit card is expired or inactive.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deactivateAccount(long debitCardNum, int pin, String transactionType) {
        try {
            if (checkCardValidity(debitCardNum)) {
                updateTransactionType(debitCardNum, transactionType);
                printTransactionInfo(debitCardNum, transactionType);
                LocalDateTime transactionTime = LocalDateTime.now();
                String query = "UPDATE ATM_ACCOUNTS_DB SET status_of_card = false, date_time = ? WHERE debit_card_num = ? AND acc_pin = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setObject(1, transactionTime);
                statement.setLong(2, debitCardNum);
                statement.setInt(3, pin);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    insertTransactionHistory(debitCardNum, transactionType, 0); // No amount for account deactivation
                    //System.out.println("Account deactivated successfully.");
                    return true;
                } else {
                    System.out.println("Error deactivating account.");
                    return false;
                }
            } else {
                System.out.println("Debit card is expired or inactive.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

