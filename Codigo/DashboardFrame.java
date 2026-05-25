package proyectotbd2;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.ArrayList;

public class DashboardFrame extends JFrame {

    private static final Color BG       = new Color(0xF5F4F0);
    private static final Color WHITE    = Color.WHITE;
    private static final Color BORDER   = new Color(0xDDDCDA);
    private static final Color TEXT     = new Color(0x1A1A18);
    private static final Color MUTED    = new Color(0x6B6B68);
    private static final Color BTN_BG   = new Color(0x2B6CB0);
    private static final Color TREE_SEL = new Color(0xDCEBF7);

    private static final Font F_MONO  = new Font("Consolas", Font.PLAIN, 13);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font F_TREE  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font F_BTN   = new Font("Segoe UI", Font.BOLD,  11);

    private static final String[] SQL_TYPES = {
        "INT", "BIGINT", "SMALLINT", "TINYINT", "DECIMAL", "FLOAT", "DOUBLE",
        "VARCHAR(255)", "CHAR(1)", "TEXT", "CLOB",
        "DATE", "TIME", "DATETIME", "TIMESTAMP", "BOOLEAN", "BLOB"
    };

    private JTree tree;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private JTextArea sqlEditor;
    private JTable resultTable;
    private JLabel lblStatus;
    private ArrayList<DBConnectionSession> sessions = new ArrayList<>();

    public DashboardFrame() {
        setTitle("Database Manager");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildContent(),   BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(WHITE);
        bar.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER), new EmptyBorder(8,14,8,14)));

        JLabel title = new JLabel("Database Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(toolBtn("New Connection",  e -> new LoginFrame(this)));
        btns.add(toolBtn("Create Table",    e -> createTableDialog()));
        btns.add(toolBtn("Create View",     e -> createViewDialog()));
        btns.add(execBtn());

        bar.add(title, BorderLayout.WEST);
        bar.add(btns,  BorderLayout.EAST);
        return bar;
    }

    private JButton toolBtn(String text, ActionListener action) {
        JButton b = new JButton(text);
        b.setFont(F_BTN); b.setForeground(MUTED); b.setBackground(WHITE);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER,1), new EmptyBorder(5,12,5,12)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(action);
        return b;
    }

    private JButton execBtn() {
        JButton b = new JButton("Execute");
        b.setFont(F_BTN); b.setForeground(WHITE); b.setBackground(BTN_BG);
        b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(5,14,5,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(0x1A56A0)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(BTN_BG); }
        });
        b.addActionListener(e -> executeQuery());
        return b;
    }

    private JSplitPane buildContent() {
        JSplitPane vert = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildEditor(), buildResults());
        vert.setDividerLocation(300); vert.setDividerSize(1); vert.setBorder(null);
        JSplitPane horiz = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildSidebar(), vert);
        horiz.setDividerLocation(300); horiz.setDividerSize(1); horiz.setBorder(null);
        return horiz;
    }

    private JPanel buildSidebar() {
        rootNode  = new DefaultMutableTreeNode("Connections");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(true); tree.setFont(F_TREE);
        tree.setBackground(WHITE); tree.setRowHeight(22);
        tree.setBorder(new EmptyBorder(6,4,6,4));

        DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
        r.setBackgroundSelectionColor(TREE_SEL); r.setBorderSelectionColor(TREE_SEL);
        r.setTextSelectionColor(TEXT); r.setTextNonSelectionColor(TEXT);
        r.setBackgroundNonSelectionColor(WHITE);
        r.setLeafIcon(null); r.setOpenIcon(null); r.setClosedIcon(null);
        tree.setCellRenderer(r);

        tree.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path == null) return;
                DefaultMutableTreeNode node   = (DefaultMutableTreeNode) path.getLastPathComponent();
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                if (parent == null) return;
                String parentName = parent.getUserObject().toString();
                String nodeName   = node.getUserObject().toString();
                DBConnectionSession session   = getSessionFromPath(path);
                if (session == null) return;

                if (parentName.equals("Tables")) {
                    sqlEditor.setText("SELECT * FROM " + nodeName);
                    executeQuery();
                }
            }

            @Override public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path == null) return;
                tree.setSelectionPath(path);
                DefaultMutableTreeNode node   = (DefaultMutableTreeNode) path.getLastPathComponent();
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
                if (parent == null) return;

                String parentName = parent.getUserObject().toString();
                String nodeName   = node.getUserObject().toString();
                DBConnectionSession session   = getSessionFromPath(path);
                if (session == null) return;

                JPopupMenu menu = new JPopupMenu();

                String ddlLabel = null;
                if (parentName.equals("Tables"))               ddlLabel = "Generate DDL";
                else if (parentName.equals("Views"))           ddlLabel = "Generate DDL";
                else if (parentName.equals("Procedures & Functions")) ddlLabel = "Generate DDL";
                else if (parentName.equals("Triggers"))        ddlLabel = "Generate DDL";
                else if (parentName.equals("Indexes"))         ddlLabel = "Generate DDL";

                if (ddlLabel != null) {
                    final String pName = parentName;
                    final String oName = nodeName;
                    JMenuItem ddlItem = new JMenuItem(ddlLabel);
                    ddlItem.addActionListener(ev -> {
                        String ddl;
                        switch (pName) {
                            case "Tables":               ddl = MetaDataManager.generateTableDDL(session.getConnection(), oName); break;
                            case "Views":                ddl = MetaDataManager.generateViewDDL(session.getConnection(), oName); break;
                            case "Procedures & Functions": ddl = MetaDataManager.generateProcedureDDL(session.getConnection(), oName); break;
                            case "Triggers":             ddl = MetaDataManager.generateTriggerDDL(session.getConnection(), oName); break;
                            case "Indexes":              ddl = MetaDataManager.generateIndexDDL(session.getConnection(), oName); break;
                            default:                     ddl = "-- DDL not available"; break;
                        }
                        showDDL(oName, ddl);
                    });
                    menu.add(ddlItem);

                    JMenuItem editItem = new JMenuItem("Edit (export DDL to editor)");
                    editItem.addActionListener(ev -> {
                        String ddl;
                        switch (pName) {
                            case "Tables":               ddl = MetaDataManager.generateTableDDL(session.getConnection(), oName); break;
                            case "Views":                ddl = MetaDataManager.generateViewDDL(session.getConnection(), oName); break;
                            case "Procedures & Functions": ddl = MetaDataManager.generateProcedureDDL(session.getConnection(), oName); break;
                            case "Triggers":             ddl = MetaDataManager.generateTriggerDDL(session.getConnection(), oName); break;
                            case "Indexes":              ddl = MetaDataManager.generateIndexDDL(session.getConnection(), oName); break;
                            default:                     ddl = "-- DDL not available"; break;
                        }
                        sqlEditor.setText(ddl);
                        lblStatus.setText("DDL exported to editor — modify and execute");
                    });
                    menu.add(editItem);
                }

                if (parentName.equals("Tables") || parentName.equals("Views") ||
                    parentName.equals("Indexes") || parentName.equals("Triggers")) {
                    menu.addSeparator();
                    JMenuItem dropItem = new JMenuItem("Drop " + parentName.replaceAll("s$","").replaceAll("se$","s").replace("Indexes","Index").replace("Procedures & Functions","Procedure/Function"));
                    dropItem.setForeground(new Color(0xA32D2D));
                    dropItem.addActionListener(ev -> {
                        int ok = JOptionPane.showConfirmDialog(DashboardFrame.this,
                            "Drop " + nodeName + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (ok != JOptionPane.YES_OPTION) return;
                        String dropSql;
                        switch (parentName) {
                            case "Tables":   dropSql = "DROP TABLE " + nodeName; break;
                            case "Views":    dropSql = "DROP VIEW " + nodeName; break;
                            case "Indexes":  dropSql = "DROP INDEX " + nodeName; break;
                            case "Triggers": dropSql = "DROP TRIGGER " + nodeName; break;
                            default:         dropSql = null; break;
                        }
                        if (dropSql == null) return;
                        try {
                            QueryExecutor.executeQuery(session.getConnection(), dropSql);
                            reloadTree();
                            lblStatus.setText("Dropped: " + nodeName);
                        } catch (Exception ex) { JOptionPane.showMessageDialog(DashboardFrame.this, ex.getMessage()); }
                    });
                    menu.add(dropItem);
                }

                if (menu.getComponentCount() > 0) menu.show(tree, e.getX(), e.getY());
            }
        });

        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(WHITE);
        side.setBorder(new MatteBorder(0,0,0,1,BORDER));
        JLabel header = new JLabel("Connections");
        header.setFont(F_LABEL); header.setForeground(MUTED); header.setOpaque(true);
        header.setBackground(BG);
        header.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER), new EmptyBorder(9,14,9,14)));
        JScrollPane scroll = new JScrollPane(tree);
        scroll.setBorder(null); scroll.getViewport().setBackground(WHITE);
        side.add(header, BorderLayout.NORTH);
        side.add(scroll, BorderLayout.CENTER);
        return side;
    }

    private JPanel buildEditor() {
        sqlEditor = new JTextArea();
        sqlEditor.setFont(F_MONO); sqlEditor.setBackground(WHITE); sqlEditor.setForeground(TEXT);
        sqlEditor.setCaretColor(BTN_BG); sqlEditor.setBorder(new EmptyBorder(12,14,12,14));
        sqlEditor.setLineWrap(true);

        JScrollPane scroll = new JScrollPane(sqlEditor);
        scroll.setBorder(null); scroll.getViewport().setBackground(WHITE);

        JLabel lbl = new JLabel("SQL Editor");
        lbl.setFont(F_LABEL); lbl.setForeground(MUTED); lbl.setOpaque(true); lbl.setBackground(BG);
        lbl.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER), new EmptyBorder(9,14,9,14)));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(WHITE);
        p.add(lbl,    BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildResults() {
        resultTable = new JTable();
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultTable.setBackground(WHITE); resultTable.setForeground(TEXT);
        resultTable.setGridColor(BORDER); resultTable.setRowHeight(24);
        resultTable.setSelectionBackground(TREE_SEL); resultTable.setSelectionForeground(TEXT);
        resultTable.setShowHorizontalLines(true); resultTable.setShowVerticalLines(false);
        resultTable.setFillsViewportHeight(true);
        resultTable.getTableHeader().setFont(F_LABEL);
        resultTable.getTableHeader().setBackground(BG); resultTable.getTableHeader().setForeground(MUTED);
        resultTable.getTableHeader().setBorder(new MatteBorder(0,0,1,0,BORDER));

        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.setBorder(null); scroll.getViewport().setBackground(WHITE);

        JLabel lbl = new JLabel("Results");
        lbl.setFont(F_LABEL); lbl.setForeground(MUTED); lbl.setOpaque(true); lbl.setBackground(BG);
        lbl.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,BORDER), new EmptyBorder(9,14,9,14)));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(WHITE);
        p.add(lbl,    BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG);
        bar.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,BORDER), new EmptyBorder(6,14,6,14)));
        lblStatus = new JLabel("Ready");
        lblStatus.setFont(F_SMALL); lblStatus.setForeground(MUTED);
        bar.add(lblStatus, BorderLayout.WEST);
        return bar;
    }

    public void addDatabaseConnection(String connectionName, Connection connection) {
        sessions.add(new DBConnectionSession(connectionName, connection));
        reloadTree();
    }

    private void reloadTree() {
        rootNode.removeAllChildren();
        for (DBConnectionSession session : sessions) {
            Connection conn = session.getConnection();
            DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(session);

            DefaultMutableTreeNode tablesNode = new DefaultMutableTreeNode("Tables");
            for (String table : MetaDataManager.getTables(conn)) {
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
                for (String col : MetaDataManager.getColumns(conn, table))
                    tableNode.add(new DefaultMutableTreeNode(col));
                tablesNode.add(tableNode);
            }
            dbNode.add(tablesNode);

            DefaultMutableTreeNode viewsNode = new DefaultMutableTreeNode("Views");
            for (String view : MetaDataManager.getViews(conn))
                viewsNode.add(new DefaultMutableTreeNode(view));
            dbNode.add(viewsNode);

            addCat(dbNode, "Procedures & Functions", MetaDataManager.getProceduresAndFunctions(conn));
            addCat(dbNode, "Triggers",    MetaDataManager.getTriggers(conn));
            addCat(dbNode, "Indexes",     MetaDataManager.getIndexes(conn));
            addCat(dbNode, "Sequences",   MetaDataManager.getSequences(conn));
            addCat(dbNode, "Tablespaces", MetaDataManager.getTablespaces(conn));
            addCat(dbNode, "Users",       MetaDataManager.getUsers(conn));
            rootNode.add(dbNode);
        }
        treeModel.reload();
    }

    private void addCat(DefaultMutableTreeNode parent, String name, java.util.List<String> items) {
        DefaultMutableTreeNode cat = new DefaultMutableTreeNode(name);
        for (String item : items) cat.add(new DefaultMutableTreeNode(item));
        parent.add(cat);
    }

    private void executeQuery() {
        DBConnectionSession session = getSelectedSession();
        if (session == null) { lblStatus.setText("Select a database first"); return; }
        String sql = sqlEditor.getText().trim();
        if (sql.isEmpty()) return;
        try {
            javax.swing.table.DefaultTableModel model = QueryExecutor.executeQuery(session.getConnection(), sql);
            if (model != null) {
                resultTable.setModel(model);
                lblStatus.setText("Done — " + model.getRowCount() + " row(s)");
            } else {
                lblStatus.setText("Query executed successfully");
                reloadTree();
            }
        } catch (Exception e) {
            lblStatus.setText("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void createTableDialog() {
        JTextField txtTable = new JTextField();
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel() {
            @Override public Class<?> getColumnClass(int col) {
                return col == 2 || col == 3 ? Boolean.class : String.class;
            }
        };
        model.addColumn("Column"); model.addColumn("Type");
        model.addColumn("PK"); model.addColumn("NOT NULL");

        JComboBox<String> typeCombo = new JComboBox<>(SQL_TYPES);
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(typeCombo));
        table.getColumnModel().getColumn(1).setCellRenderer((t, value, sel, focus, row, col) -> {
            JComboBox<String> c = new JComboBox<>(SQL_TYPES);
            c.setSelectedItem(value);
            c.setBackground(sel ? table.getSelectionBackground() : table.getBackground());
            return c;
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(520, 200));
        JButton btnAdd = new JButton("Add Column");
        btnAdd.addActionListener(e -> model.addRow(new Object[]{"", "INT", false, false}));
        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(new JLabel("Table Name"), BorderLayout.NORTH);
        top.add(txtTable, BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnAdd, BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, panel, "Create Table", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        DBConnectionSession session = getSelectedSession();
        if (session == null) { JOptionPane.showMessageDialog(this, "Select a connection first"); return; }
        try {
            StringBuilder sql = new StringBuilder("CREATE TABLE " + txtTable.getText() + " (\n");
            for (int i = 0; i < model.getRowCount(); i++) {
                sql.append("    ").append(model.getValueAt(i,0)).append(" ").append(model.getValueAt(i,1));
                if ((boolean) model.getValueAt(i,3)) sql.append(" NOT NULL");
                if ((boolean) model.getValueAt(i,2)) sql.append(" PRIMARY KEY");
                sql.append(",\n");
            }
            int last = sql.lastIndexOf(",");
            if (last != -1) sql.deleteCharAt(last);
            sql.append("\n)");
            QueryExecutor.executeQuery(session.getConnection(), sql.toString());
            reloadTree();
            lblStatus.setText("Table created: " + txtTable.getText());
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void createViewDialog() {
        JTextField txtView = new JTextField();
        JTextArea txtQuery = new JTextArea(10, 40);
        txtQuery.setFont(F_MONO);
        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(new JLabel("View Name"), BorderLayout.NORTH);
        top.add(txtView, BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtQuery), BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(this, panel, "Create View", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        DBConnectionSession session = getSelectedSession();
        if (session == null) { JOptionPane.showMessageDialog(this, "Select a connection first"); return; }
        try {
            QueryExecutor.executeQuery(session.getConnection(),
                "CREATE VIEW " + txtView.getText() + " AS\n" + txtQuery.getText());
            reloadTree();
            lblStatus.setText("View created: " + txtView.getText());
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
    }

    private void showDDL(String name, String ddl) {
        JTextArea area = new JTextArea(ddl);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBackground(new Color(30,30,30));
        area.setForeground(Color.WHITE);
        area.setCaretColor(Color.WHITE);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 350));

        JButton btnEdit = new JButton("Send to editor");
        btnEdit.addActionListener(e -> {
            sqlEditor.setText(ddl);
            lblStatus.setText("DDL exported to editor");
            SwingUtilities.getWindowAncestor(btnEdit).dispose();
        });

        JPanel panel = new JPanel(new BorderLayout(0,8));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnEdit, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, panel, "DDL — " + name, JOptionPane.INFORMATION_MESSAGE);
    }

    private DBConnectionSession getSelectedSession() {
        TreePath path = tree.getSelectionPath();
        if (path == null) return null;
        return getSessionFromPath(path);
    }

    private DBConnectionSession getSessionFromPath(TreePath path) {
        for (Object obj : path.getPath()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
            if (node.getUserObject() instanceof DBConnectionSession)
                return (DBConnectionSession) node.getUserObject();
        }
        return null;
    }
}