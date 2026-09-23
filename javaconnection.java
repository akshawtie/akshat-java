import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class javaconnection {
    public static void main(String[] args) {
        
        // 1. Set your connection variables
        // If you downloaded the MySQL connector, use: "jdbc:mysql://localhost:3306/your_database_name"
        // If you installed the Arch mariadb-jdbc package, use: "jdbc:mariadb://localhost:3306/your_database_name"
        String dbUrl = "jdbc:mysql://localhost:3306/akshat";
        String username = "akshat";
        String password = "5202";

        // 2. Attempt to connect
        try (Connection conn = DriverManager.getConnection(dbUrl, username, password)) {
            
            System.out.println("Success! Connected to the database.");
            
            // This is where you will eventually write your SQL queries
            
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
    }
}