package banking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BankManagement {
    private static final int NULL = 0;
    static Connection con = DatabaseConnection.getConnection();

    public static boolean createAccount(String name, int passCode) {
        if (name.isEmpty() || passCode == NULL) {
            System.out.println("All fields are required!");
            return false;
        }
        try {
            String sql = "INSERT INTO customer (cname, balance, pass_code) VALUES (?, 1000, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setInt(2, passCode);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                System.out.println(
                        "Account created successfully! You can now login.");
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Username already exists! Try another one.");
        }
        return false;
    }

    public static boolean loginAccount(String name, int passCode) throws IOException {
        if (name.isEmpty() || passCode == NULL) {
            System.out.println("All fields are required!");
            return false;
        }
        try {
            String sql = "SELECT * FROM customer WHERE cname = ? AND pass_code = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setInt(2, passCode);
            ResultSet rs = ps.executeQuery();

            BufferedReader sc = new BufferedReader(
                    new InputStreamReader(System.in));

            if (rs.next()) {
                int senderAc = rs.getInt("ac_no");
                int ch = 0;

                while (true) {
                    System.out.println(
                            "\n Hello, " + rs.getString("cname"));
                    System.out.println("1) Transfer Money");
                    System.out.println("2) View Balance");
                    System.out.println("3) Logout");
                    System.out.print("Enter Choice: ");
                    ch = Integer.parseInt(
                            sc.readLine());
                    if (ch == 1) {
                        System.out.println("Enter Receiver A/c No: ");
                        int receiverAc = Integer.parseInt(sc.readLine());
                        System.out.print("Enter Amount: ");
                        int amount = Integer.parseInt(sc.readLine());
                        if (transferMoney(senderAc, receiverAc, amount)) {
                            System.out.println(
                                    "Transaction successful!");
                        } else {
                            System.out.println(
                                    "Transaction failed! Please try again.");
                        }

                    } else if (ch == 2) {
                        getBalance(senderAc);
                    } else if (ch == 3) {
                        break;
                    } else {
                        System.out.println("Invalid choice! Try again.");
                    }
                }
                return true;
            } else {
                System.out.println(
                        "Invalid username or password!");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void getBalance(int accNo) {
        try {
            String sql = "SELECT * FROM customer WHERE ac_no = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accNo);
            ResultSet rs = ps.executeQuery();
            System.out.printf(
                    "%12s %15s %10s\n", "Account No", "Customer Name", "Balance");
            while (rs.next()) {
                System.out.printf("%12d %15s %10d.00\n",
                        rs.getInt("ac_no"),
                        rs.getString("cname"),
                        rs.getInt("balance"));
            }
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean transferMoney(int sender_ac, int receiver_ac, int amount) {
        if (sender_ac == NULL || receiver_ac == NULL || amount == NULL) {
            System.err.println("All fields are required!");
            return false;
        }
        try {
            con.setAutoCommit(false);
            String checkBalance = "SELECT balance FROM customer WHERE ac_no = ?";
            PreparedStatement ps = con.prepareStatement(checkBalance);
            ps.setInt(1, sender_ac);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getInt("balance") < amount) {
                System.out.println("Insufficient Balanced");
                return false;
            }

            String debit = "UPDATE customer SET balance = balance - ? WHERE ac_no = ?";
            PreparedStatement psDebit = con.prepareStatement(debit);
            psDebit.setInt(1, amount);
            psDebit.setInt(2, sender_ac);
            psDebit.executeUpdate();

            String credit = "UPDATE customer SET balance = balance + ? WHERE ac_no = ?";
            PreparedStatement psCredit = con.prepareStatement(credit);
            psCredit.setInt(1, amount);
            psCredit.setInt(2, receiver_ac);
            psCredit.executeUpdate();

            con.commit();
            return true;
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException sqlExp) {
                sqlExp.printStackTrace();
            }
            e.printStackTrace();
        }
        return false;
    }
}
