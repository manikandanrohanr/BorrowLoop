import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Shared colours, fonts and simple rounded buttons for the desktop UI.
 */
public final class UITheme {

    public static final Color SIDEBAR = new Color(21, 52, 92);
    public static final Color SIDEBAR_HOVER = new Color(32, 72, 122);
    public static final Color SIDEBAR_ACTIVE = new Color(40, 90, 150);
    public static final Color PAGE_BG = new Color(245, 247, 250);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color PRIMARY = new Color(21, 52, 92);
    public static final Color PRIMARY_HOVER = new Color(32, 72, 122);
    public static final Color TEXT = new Color(33, 37, 41);
    public static final Color MUTED = new Color(90, 98, 110);
    public static final Color BORDER = new Color(220, 226, 234);

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private UITheme() {
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(MUTED);
        return label;
    }

    public static JTextField field() {
        JTextField field = new JTextField();
        field.setFont(BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return field;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setRowHeight(28);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(220, 232, 246));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setUI(new BasicTableHeaderUI());
        header.setDefaultRenderer(new HeaderRenderer());
        header.setOpaque(true);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
    }

    /**
     * Windows look-and-feel paints a light header and ignores setForeground,
     * so column titles stay white-on-white until hover. This renderer always
     * paints a dark blue background with white text.
     */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(value == null ? "" : value.toString());
            setOpaque(true);
            setBackground(PRIMARY);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(15, 40, 72)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            return this;
        }
    }

    public static class RoundedButton extends JButton {
        private Color fill;
        private final Color normal;
        private final Color hover;

        public RoundedButton(String text) {
            this(text, PRIMARY, PRIMARY_HOVER, Color.WHITE);
        }

        public RoundedButton(String text, Color normal, Color hover, Color foreground) {
            super(text);
            this.normal = normal;
            this.hover = hover;
            this.fill = normal;
            setFont(BODY);
            setForeground(foreground);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    fill = hover;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    fill = normal;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class SidebarButton extends JButton {
        private boolean active;
        private Color fill;

        public SidebarButton(String text) {
            super(text);
            this.fill = SIDEBAR;
            setFont(BODY);
            setForeground(Color.WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!active) {
                        fill = SIDEBAR_HOVER;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!active) {
                        fill = SIDEBAR;
                        repaint();
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            fill = active ? SIDEBAR_ACTIVE : SIDEBAR;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JComponent cardWrap(JComponent inner) {
        inner.setBackground(CARD_BG);
        inner.setOpaque(true);
        inner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return inner;
    }
}
