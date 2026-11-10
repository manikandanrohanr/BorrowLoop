import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ResourceManager – Add, view, and search resources.
 *
 * JDBC concepts used:
 *   - PreparedStatement  : parameterized SQL to prevent SQL-injection
 *   - executeUpdate()    : INSERT operations
 *   - executeQuery()     : SELECT operations
 *   - ResultSet          : iterate over rows returned by a query
 */
public class ResourceManager {

    private final Connection conn;

    public ResourceManager(Connection conn) {
        this.conn = conn;
    }

    public String addResource(String name, String category, int qty) {
        if (name == null || name.trim().isEmpty()) {
            return "Please enter a resource name.";
        }
        if (category == null || category.trim().isEmpty()) {
            return "Please enter a category.";
        }
        if (qty < 1) {
            return "Quantity must be at least 1.";
        }

        String sql = "INSERT INTO resources (resource_name, category, total_qty, available_qty) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, category.trim());
            ps.setInt(3, qty);
            ps.setInt(4, qty); // initially all items are available
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return "Resource added successfully!";
            }
            return "Resource could not be added.";
        } catch (SQLException e) {
            return "Error adding resource: " + e.getMessage();
        }
    }

    public List<Object[]> getAllResources() throws SQLException {
        String sql = "SELECT resource_id, resource_name, category, total_qty, available_qty FROM resources ORDER BY resource_id";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(resourceRow(rs));
            }
        }
        return rows;
    }

    public List<Object[]> searchResources(String keyword) throws SQLException {
        String sql = "SELECT resource_id, resource_name, category, total_qty, available_qty "
                + "FROM resources WHERE resource_name LIKE ? ORDER BY resource_id";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + (keyword == null ? "" : keyword.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(resourceRow(rs));
                }
            }
        }
        return rows;
    }

    public int getTotalResourceCount() throws SQLException {
        return scalarInt("SELECT COUNT(*) FROM resources");
    }

    public int getAvailableQuantityTotal() throws SQLException {
        return scalarInt("SELECT COALESCE(SUM(available_qty), 0) FROM resources");
    }

    public int getBorrowedQuantityTotal() throws SQLException {
        return scalarInt("SELECT COALESCE(SUM(total_qty - available_qty), 0) FROM resources");
    }

    private int scalarInt(String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Object[] resourceRow(ResultSet rs) throws SQLException {
        return new Object[]{
                rs.getInt("resource_id"),
                rs.getString("resource_name"),
                rs.getString("category"),
                rs.getInt("total_qty"),
                rs.getInt("available_qty")
        };
    }
}
