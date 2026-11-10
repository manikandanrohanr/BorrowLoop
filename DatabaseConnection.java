import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = System.getenv().getOrDefault(
            "BORROWLOOP_DB_URL", "jdbc:mysql://localhost:3306/borrowloop");
    private static final String USER = System.getenv().getOrDefault("BORROWLOOP_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("BORROWLOOP_DB_PASSWORD", "");

    private static Connection connection = null;
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                System.out.println("[DB] Connected to MySQL successfully.");
            } catch (ClassNotFoundException e) {
                System.out.println("[DB] MySQL driver not found: " + e.getMessage());
                System.out.println("     Make sure mysql-connector-j-*.jar is on the classpath.");
            } catch (SQLException e) {
                System.out.println("[DB] Connection failed: " + e.getMessage());
                System.out.println("     Check your URL, username, and password.");
            }
        }
        return connection;
    }
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.out.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
