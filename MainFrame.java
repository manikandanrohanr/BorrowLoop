import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {

    private static final String[] RESOURCE_COLUMNS = {
            "Resource ID", "Name", "Category", "Total Qty", "Available Qty"
    };
    private static final String[] HISTORY_COLUMNS = {
            "Record ID", "Reg No", "Student", "Resource", "Borrow Date", "Due Date", "Return Status"
    };
    private static final String[] OVERDUE_COLUMNS = {
            "Record ID", "Reg No", "Student", "Resource", "Borrow Date", "Due Date", "Days Late"
    };

    private final ResourceManager resourceManager;
    private final BorrowManager borrowManager;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, UITheme.SidebarButton> navButtons = new LinkedHashMap<>();

    private JLabel totalCardValue;
    private JLabel availableCardValue;
    private JLabel borrowedCardValue;
    private JLabel overdueCardValue;

    private DefaultTableModel viewModel;
    private DefaultTableModel searchModel;
    private DefaultTableModel historyModel;
    private DefaultTableModel overdueModel;

    private JTextField nameField;
    private JTextField categoryField;
    private JTextField qtyField;
    private JTextField searchField;
    private JTextField borrowRegField;
    private JTextField borrowNameField;
    private JTextField borrowEmailField;
    private JTextField borrowPhoneField;
    private JTextField borrowDateField;
    private JTextField dueDateField;
    private JComboBox<ResourceItem> resourceCombo;
    private JTextField returnIdField;
    private JTextField returnDateField;

    public MainFrame(ResourceManager resourceManager, BorrowManager borrowManager) {
        super("BorrowLoop – Resource Sharing System");
        this.resourceManager = resourceManager;
        this.borrowManager = borrowManager;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.PAGE_BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseConnection.closeConnection();
            }
        });

        showScreen("dashboard");
        refreshDashboard();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 8, 20, 8));

        JLabel brand = new JLabel("BorrowLoop");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 16, 4, 0));

        JLabel subtitle = new JLabel("Resource Sharing");
        subtitle.setForeground(new Color(190, 210, 230));
        subtitle.setFont(UITheme.SMALL);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 16, 20, 0));

        sidebar.add(brand);
        sidebar.add(subtitle);

        addNav(sidebar, "Dashboard", "dashboard", this::refreshDashboard);
        addNav(sidebar, "Add Resource", "add");
        addNav(sidebar, "View Resources", "view", this::refreshViewResources);
        addNav(sidebar, "Search Resource", "search");
        addNav(sidebar, "Borrow Resource", "borrow", this::loadResourceCombo);
        addNav(sidebar, "Return Resource", "return");
        addNav(sidebar, "Borrowing History", "history", this::refreshHistory);
        addNav(sidebar, "Overdue Items", "overdue", () -> refreshOverdue(false));
        addNav(sidebar, "Exit", "exit", this::confirmExit);

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addNav(JPanel sidebar, String label, String key) {
        addNav(sidebar, label, key, null);
    }

    private void addNav(JPanel sidebar, String label, String key, Runnable onShow) {
        UITheme.SidebarButton button = new UITheme.SidebarButton(label);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.addActionListener(e -> {
            if ("exit".equals(key)) {
                if (onShow != null) {
                    onShow.run();
                }
                return;
            }
            showScreen(key);
            if (onShow != null) {
                onShow.run();
            }
        });
        navButtons.put(key, button);
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(6));
    }

    private JPanel buildContent() {
        content.setBackground(UITheme.PAGE_BG);
        content.add(pageWrap(buildDashboard()), "dashboard");
        content.add(pageWrap(buildAddResource()), "add");
        content.add(pageWrap(buildViewResources()), "view");
        content.add(pageWrap(buildSearch()), "search");
        content.add(pageWrap(buildBorrow()), "borrow");
        content.add(pageWrap(buildReturn()), "return");
        content.add(pageWrap(buildHistory()), "history");
        content.add(pageWrap(buildOverdue()), "overdue");
        return content;
    }

    private JPanel pageWrap(JPanel inner) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(UITheme.PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        page.add(inner, BorderLayout.CENTER);
        return page;
    }

    private void showScreen(String key) {
        cards.show(content, key);
        for (Map.Entry<String, UITheme.SidebarButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(key));
        }
    }

    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(UITheme.heading("Dashboard"));
        header.add(Box.createVerticalStrut(6));
        header.add(UITheme.caption("Live totals from the MySQL borrowloop database."));
        panel.add(header, BorderLayout.NORTH);

        JPanel cardsHolder = new JPanel(new BorderLayout());
        cardsHolder.setOpaque(false);
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 16));
        cardsRow.setOpaque(false);
        cardsRow.setPreferredSize(new Dimension(0, 150));
        totalCardValue = new JLabel("0");
        availableCardValue = new JLabel("0");
        borrowedCardValue = new JLabel("0");
        overdueCardValue = new JLabel("0");
        cardsRow.add(statCard("Total Resources", totalCardValue));
        cardsRow.add(statCard("Available Resources", availableCardValue));
        cardsRow.add(statCard("Borrowed Resources", borrowedCardValue));
        cardsRow.add(statCard("Overdue Records", overdueCardValue));
        cardsHolder.add(cardsRow, BorderLayout.NORTH);
        panel.add(cardsHolder, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        UITheme.RoundedButton refresh = new UITheme.RoundedButton("Refresh Dashboard");
        refresh.addActionListener(e -> refreshDashboard());
        south.add(refresh);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel statCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        UITheme.cardWrap(card);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.SMALL);
        titleLabel.setForeground(UITheme.MUTED);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(UITheme.PRIMARY);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(titleLabel);
        body.add(Box.createVerticalStrut(12));
        body.add(valueLabel);
        card.add(body, BorderLayout.NORTH);
        return card;
    }

    private JPanel buildAddResource() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(sectionHeader("Add Resource", "Save a new item to the resources table."), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 12, 12));
        UITheme.cardWrap(form);
        nameField = UITheme.field();
        categoryField = UITheme.field();
        qtyField = UITheme.field();
        form.add(formLabel("Resource Name"));
        form.add(nameField);
        form.add(formLabel("Category"));
        form.add(categoryField);
        form.add(formLabel("Quantity"));
        form.add(qtyField);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        UITheme.RoundedButton add = new UITheme.RoundedButton("Add Resource");
        add.addActionListener(e -> addResource());
        south.add(add);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(south, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildViewResources() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(sectionHeader("View Resources", "All rows from the resources table."), BorderLayout.NORTH);

        viewModel = emptyModel(RESOURCE_COLUMNS);
        JTable table = new JTable(viewModel);
        UITheme.styleTable(table);
        panel.add(scroll(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        UITheme.RoundedButton refresh = new UITheme.RoundedButton("Refresh");
        refresh.addActionListener(e -> refreshViewResources());
        south.add(refresh);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSearch() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(sectionHeader("Search Resource", "Find resources by name using SQL LIKE."), BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        searchField = UITheme.field();
        UITheme.RoundedButton search = new UITheme.RoundedButton("Search");
        search.addActionListener(e -> searchResources());
        searchField.addActionListener(e -> searchResources());
        top.add(searchField, BorderLayout.CENTER);
        top.add(search, BorderLayout.EAST);

        searchModel = emptyModel(RESOURCE_COLUMNS);
        JTable table = new JTable(searchModel);
        UITheme.styleTable(table);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(scroll(table), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBorrow() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(sectionHeader("Borrow Resource",
                "New students are registered automatically. Availability is checked in a synchronized thread."),
                BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(7, 2, 12, 12));
        UITheme.cardWrap(form);
        borrowRegField = UITheme.field();
        borrowNameField = UITheme.field();
        borrowEmailField = UITheme.field();
        borrowPhoneField = UITheme.field();
        resourceCombo = new JComboBox<>();
        resourceCombo.setFont(UITheme.BODY);
        borrowDateField = UITheme.field();
        dueDateField = UITheme.field();
        borrowDateField.setText(LocalDate.now().toString());
        dueDateField.setText(LocalDate.now().plusDays(7).toString());

        form.add(formLabel("Student Register Number"));
        form.add(borrowRegField);
        form.add(formLabel("Student Name (if new)"));
        form.add(borrowNameField);
        form.add(formLabel("Email (if new)"));
        form.add(borrowEmailField);
        form.add(formLabel("Phone (if new)"));
        form.add(borrowPhoneField);
        form.add(formLabel("Resource"));
        form.add(resourceCombo);
        form.add(formLabel("Borrow Date (YYYY-MM-DD)"));
        form.add(borrowDateField);
        form.add(formLabel("Due Date (YYYY-MM-DD)"));
        form.add(dueDateField);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        UITheme.RoundedButton borrow = new UITheme.RoundedButton("Borrow");
        borrow.addActionListener(e -> borrowResource());
        south.add(borrow);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(south, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReturn() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(sectionHeader("Return Resource", "Enter a borrowing record ID to mark the item returned."),
                BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 12, 12));
        UITheme.cardWrap(form);
        returnIdField = UITheme.field();
        returnDateField = UITheme.field();
        returnDateField.setText(LocalDate.now().toString());
        form.add(formLabel("Borrowing Record ID"));
        form.add(returnIdField);
        form.add(formLabel("Return Date (YYYY-MM-DD)"));
        form.add(returnDateField);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        UITheme.RoundedButton ret = new UITheme.RoundedButton("Return");
        ret.addActionListener(e -> returnResource());
        south.add(ret);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(south, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHistory() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(sectionHeader("Borrowing History", "Student, resource, dates and return status."), BorderLayout.NORTH);

        historyModel = emptyModel(HISTORY_COLUMNS);
        JTable table = new JTable(historyModel);
        UITheme.styleTable(table);
        panel.add(scroll(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        UITheme.RoundedButton refresh = new UITheme.RoundedButton("Refresh");
        refresh.addActionListener(e -> refreshHistory());
        south.add(refresh);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildOverdue() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(sectionHeader("Overdue Items",
                "Unreturned records where due_date is before today (DATEDIFF)."), BorderLayout.NORTH);

        overdueModel = emptyModel(OVERDUE_COLUMNS);
        JTable table = new JTable(overdueModel);
        UITheme.styleTable(table);
        panel.add(scroll(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.setOpaque(false);
        UITheme.RoundedButton refresh = new UITheme.RoundedButton("Refresh");
        refresh.addActionListener(e -> refreshOverdue(true));
        south.add(refresh);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel sectionHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(UITheme.heading(title));
        header.add(Box.createVerticalStrut(6));
        header.add(UITheme.caption(subtitle));
        return header;
    }

    private JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.BODY);
        label.setForeground(UITheme.TEXT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JScrollPane scroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private DefaultTableModel emptyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void fillTable(DefaultTableModel model, List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    private void refreshDashboard() {
        try {
            totalCardValue.setText(String.valueOf(resourceManager.getTotalResourceCount()));
            availableCardValue.setText(String.valueOf(resourceManager.getAvailableQuantityTotal()));
            borrowedCardValue.setText(String.valueOf(resourceManager.getBorrowedQuantityTotal()));
            overdueCardValue.setText(String.valueOf(borrowManager.getOverdueCount()));
        } catch (SQLException e) {
            showError("Could not load dashboard: " + e.getMessage());
        }
    }

    private void addResource() {
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Please enter a valid quantity (whole number).");
            return;
        }
        String result = resourceManager.addResource(nameField.getText(), categoryField.getText(), qty);
        if (result.contains("successfully")) {
            JOptionPane.showMessageDialog(this, result, "Add Resource", JOptionPane.INFORMATION_MESSAGE);
            nameField.setText("");
            categoryField.setText("");
            qtyField.setText("");
            refreshDashboard();
        } else {
            showError(result);
        }
    }

    private void refreshViewResources() {
        try {
            fillTable(viewModel, resourceManager.getAllResources());
        } catch (SQLException e) {
            showError("Error retrieving resources: " + e.getMessage());
        }
    }

    private void searchResources() {
        try {
            List<Object[]> rows = resourceManager.searchResources(searchField.getText());
            fillTable(searchModel, rows);
            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No resources matched your search.",
                        "Search Resource", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            showError("Error searching resources: " + e.getMessage());
        }
    }

    private void loadResourceCombo() {
        resourceCombo.removeAllItems();
        try {
            for (Object[] row : resourceManager.getAllResources()) {
                resourceCombo.addItem(new ResourceItem(
                        (Integer) row[0],
                        String.valueOf(row[1]),
                        (Integer) row[4]));
            }
        } catch (SQLException e) {
            showError("Could not load resources: " + e.getMessage());
        }
    }

    private void borrowResource() {
        ResourceItem item = (ResourceItem) resourceCombo.getSelectedItem();
        if (item == null) {
            showError("Please select a resource to borrow.");
            return;
        }
        String result = borrowManager.borrowResource(
                borrowRegField.getText(),
                borrowNameField.getText(),
                borrowEmailField.getText(),
                borrowPhoneField.getText(),
                item.id,
                borrowDateField.getText(),
                dueDateField.getText());
        if (result.contains("successfully")) {
            JOptionPane.showMessageDialog(this, result, "Borrow Resource", JOptionPane.INFORMATION_MESSAGE);
            loadResourceCombo();
            refreshDashboard();
        } else {
            showError(result);
        }
    }

    private void returnResource() {
        int recordId;
        try {
            recordId = Integer.parseInt(returnIdField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Please enter a valid borrowing record ID.");
            return;
        }
        String result = borrowManager.returnResource(recordId, returnDateField.getText());
        if (result.contains("successfully")) {
            JOptionPane.showMessageDialog(this, result, "Return Resource", JOptionPane.INFORMATION_MESSAGE);
            returnIdField.setText("");
            refreshDashboard();
        } else {
            showError(result);
        }
    }

    private void refreshHistory() {
        try {
            fillTable(historyModel, borrowManager.getHistory());
        } catch (SQLException e) {
            showError("Error retrieving history: " + e.getMessage());
        }
    }

    private void refreshOverdue(boolean notifyIfEmpty) {
        try {
            List<Object[]> rows = borrowManager.getOverdue();
            fillTable(overdueModel, rows);
            if (notifyIfEmpty && rows.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No overdue items. All resources are returned on time!",
                        "Overdue Items", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            showError("Error retrieving overdue items: " + e.getMessage());
        }
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Exit BorrowLoop?",
                "Exit",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            DatabaseConnection.closeConnection();
            dispose();
            System.exit(0);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "BorrowLoop", JOptionPane.ERROR_MESSAGE);
    }

    private static class ResourceItem {
        final int id;
        final String name;
        final int available;

        ResourceItem(int id, String name, int available) {
            this.id = id;
            this.name = name;
            this.available = available;
        }

        @Override
        public String toString() {
            return id + " – " + name + " (available: " + available + ")";
        }
    }
}
