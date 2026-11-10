import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep the default look and feel if the system theme is unavailable.
            }

            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(null,
                        "Unable to connect to the MySQL database.\n"
                                + "Check that MySQL is running and configure BORROWLOOP_DB_USER and BORROWLOOP_DB_PASSWORD if needed.",
                        "BorrowLoop",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            ResourceManager resourceManager = new ResourceManager(conn);
            BorrowManager borrowManager = new BorrowManager(conn);
            MainFrame frame = new MainFrame(resourceManager, borrowManager);
            frame.setVisible(true);
        });
    }
}
