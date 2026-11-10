import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BorrowManager {

    private final Connection conn;

    public BorrowManager(Connection conn) {
        this.conn = conn;
    }

    public boolean studentExists(String regNo) throws SQLException {
        String sql = "SELECT reg_no FROM students WHERE reg_no = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void registerStudent(String regNo, String name, String email, String phone) throws SQLException {
        String sql = "INSERT INTO students (reg_no, name, email, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.executeUpdate();
        }
    }

    /**
     * Borrow a resource using a dedicated thread (BorrowTask) and synchronized lock.
     */
    public String borrowResource(String regNo, String name, String email, String phone,
                                 int resourceId, String borrowDate, String dueDate) {
        if (regNo == null || regNo.trim().isEmpty()) {
            return "Please enter the student register number.";
        }
        if (borrowDate == null || borrowDate.trim().isEmpty() || dueDate == null || dueDate.trim().isEmpty()) {
            return "Please enter both borrow date and due date (YYYY-MM-DD).";
        }

        try {
            if (!studentExists(regNo.trim())) {
                if (name == null || name.trim().isEmpty()) {
                    return "Student not found. Please enter student name, email and phone to register.";
                }
                registerStudent(regNo.trim(), name.trim(),
                        email == null ? "" : email.trim(),
                        phone == null ? "" : phone.trim());
            }
        } catch (SQLException e) {
            return "Error checking student: " + e.getMessage();
        }

        BorrowTask task = new BorrowTask(conn, regNo.trim(), resourceId, borrowDate.trim(), dueDate.trim());
        Thread thread = new Thread(task);

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Borrow operation was interrupted.";
        }

        if (task.isSuccess()) {
            return "Borrow operation completed successfully.";
        }
        String detail = task.getMessage();
        if (detail == null || detail.isEmpty()) {
            return "Borrow operation could not be completed.";
        }
        return detail;
    }

    public String returnResource(int recordId, String returnDate) {
        if (returnDate == null || returnDate.trim().isEmpty()) {
            return "Please enter the return date (YYYY-MM-DD).";
        }

        String fetchSql = "SELECT resource_id, return_date FROM borrow_records WHERE record_id = ?";
        try (PreparedStatement fetchPs = conn.prepareStatement(fetchSql)) {
            fetchPs.setInt(1, recordId);
            try (ResultSet rs = fetchPs.executeQuery()) {
                if (!rs.next()) {
                    return "No borrow record found with ID " + recordId;
                }
                if (rs.getString("return_date") != null) {
                    return "This record has already been returned.";
                }
                int resourceId = rs.getInt("resource_id");

                String updateRecordSql = "UPDATE borrow_records SET return_date = ? WHERE record_id = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateRecordSql)) {
                    updatePs.setString(1, returnDate.trim());
                    updatePs.setInt(2, recordId);
                    updatePs.executeUpdate();
                }

                String updateQtySql = "UPDATE resources SET available_qty = available_qty + 1 WHERE resource_id = ?";
                try (PreparedStatement updateQtyPs = conn.prepareStatement(updateQtySql)) {
                    updateQtyPs.setInt(1, resourceId);
                    updateQtyPs.executeUpdate();
                }

                return "Resource returned successfully. Thank you!";
            }
        } catch (SQLException e) {
            return "Error processing return: " + e.getMessage();
        }
    }

    public List<Object[]> getHistory() throws SQLException {
        String sql = "SELECT br.record_id, br.reg_no, s.name, r.resource_name, "
                + "       br.borrow_date, br.due_date, br.return_date "
                + "FROM borrow_records br "
                + "JOIN students s  ON br.reg_no = s.reg_no "
                + "JOIN resources r ON br.resource_id = r.resource_id "
                + "ORDER BY br.record_id";

        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String returnDate = rs.getString("return_date") != null
                        ? rs.getString("return_date")
                        : "Not returned";
                rows.add(new Object[]{
                        rs.getInt("record_id"),
                        rs.getString("reg_no"),
                        rs.getString("name"),
                        rs.getString("resource_name"),
                        rs.getString("borrow_date"),
                        rs.getString("due_date"),
                        returnDate
                });
            }
        }
        return rows;
    }

    public List<Object[]> getOverdue() throws SQLException {
        String sql = "SELECT br.record_id, br.reg_no, s.name, r.resource_name, "
                + "       br.borrow_date, br.due_date, DATEDIFF(CURDATE(), br.due_date) AS days_late "
                + "FROM borrow_records br "
                + "JOIN students s  ON br.reg_no = s.reg_no "
                + "JOIN resources r ON br.resource_id = r.resource_id "
                + "WHERE br.return_date IS NULL AND br.due_date < CURDATE() "
                + "ORDER BY days_late DESC";

        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("record_id"),
                        rs.getString("reg_no"),
                        rs.getString("name"),
                        rs.getString("resource_name"),
                        rs.getString("borrow_date"),
                        rs.getString("due_date"),
                        rs.getInt("days_late")
                });
            }
        }
        return rows;
    }

    public int getOverdueCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records "
                + "WHERE return_date IS NULL AND due_date < CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
