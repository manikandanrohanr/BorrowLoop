import java.sql.*;

/**
 * BorrowTask – implements Runnable to perform a borrow operation in a thread.
 *
 * Unit-3 Multithreading concepts used:
 *   - Runnable interface  : the task to be run in a thread
 *   - run()              : contains the thread's work
 *   - synchronized       : prevents two threads from borrowing the last
 *                          item simultaneously (race-condition protection)
 *
 * How it is used (from BorrowManager):
 *   BorrowTask task   = new BorrowTask(conn, regNo, resourceId, borrowDate, dueDate);
 *   Thread     thread = new Thread(task);
 *   thread.start();
 *   thread.join();   // wait for the thread to finish before continuing
 *   boolean ok = task.isSuccess();
 */
public class BorrowTask implements Runnable {

    // Shared lock – every BorrowTask uses the same object, so only ONE
    // borrow can check-and-decrement available_qty at a time.
    private static final Object BORROW_LOCK = new Object();

    private final Connection conn;
    private final String     regNo;
    private final int        resourceId;
    private final String     borrowDate;   // "YYYY-MM-DD"
    private final String     dueDate;      // "YYYY-MM-DD"

    private boolean success = false;
    private String message = "";

    public BorrowTask(Connection conn, String regNo, int resourceId,
                      String borrowDate, String dueDate) {
        this.conn       = conn;
        this.regNo      = regNo;
        this.resourceId = resourceId;
        this.borrowDate = borrowDate;
        this.dueDate    = dueDate;
    }

    /**
     * The thread's entry point.
     * The synchronized block ensures that the availability check and the
     * INSERT + UPDATE are treated as one atomic unit — no other thread can
     * interleave between them on the same BORROW_LOCK.
     */
    @Override
    public void run() {
        synchronized (BORROW_LOCK) {
            try {
                // Step 1 – Check availability
                String checkSql = "SELECT available_qty FROM resources WHERE resource_id = ?";
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setInt(1, resourceId);
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (!rs.next()) {
                            message = "Resource not found.";
                            System.out.println("[Thread] Resource not found.");
                            return;
                        }
                        int available = rs.getInt("available_qty");
                        if (available <= 0) {
                            message = "Sorry, this resource is currently unavailable.";
                            System.out.println("[Thread] Sorry, this resource is currently unavailable.");
                            return;
                        }
                    }
                }

                // Step 2 – Insert borrow record
                String insertSql = "INSERT INTO borrow_records (reg_no, resource_id, borrow_date, due_date) "
                                 + "VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, regNo);
                    insertPs.setInt(2, resourceId);
                    insertPs.setString(3, borrowDate);
                    insertPs.setString(4, dueDate);
                    insertPs.executeUpdate();
                }

                // Step 3 – Decrease available quantity by 1
                String updateSql = "UPDATE resources SET available_qty = available_qty - 1 "
                                 + "WHERE resource_id = ? AND available_qty > 0";
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setInt(1, resourceId);
                    int rows = updatePs.executeUpdate();
                    if (rows > 0) {
                        success = true;
                        message = "Borrow recorded successfully.";
                        System.out.println("[Thread] Borrow recorded successfully.");
                    } else {
                        message = "Borrow failed — resource became unavailable.";
                        System.out.println("[Thread] Borrow failed — resource became unavailable.");
                    }
                }

            } catch (SQLException e) {
                message = "Database error during borrow: " + e.getMessage();
                System.out.println("[Thread] Database error during borrow: " + e.getMessage());
            }
        } // end synchronized
    }

    /** Returns true if the borrow operation completed without errors. */
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
