package proyectotbd2;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;

public class LoginFrame extends JFrame {

    private static final Color BG      = new Color(0xF5F4F0);
    private static final Color WHITE   = Color.WHITE;
    private static final Color BORDER  = new Color(0xDDDCDA);
    private static final Color TEXT    = new Color(0x1A1A18);
    private static final Color MUTED    = new Color(0x6B6B68);
    private static final Color SEG_ON  = new Color(0xEDECE8);
    private static final Color BTN_BG  = new Color(0x2B6CB0);
    private static final Color BTN_FG  = Color.WHITE;
    private static final Color DOT_OFF = new Color(0xB0AFA8);
    private static final Color DOT_ON  = new Color(0x2F7D32);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font F_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_BTN   = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private DashboardFrame dashboard;
    private JTextField      txtName, txtHost, txtPort, txtEngine, txtDBPath, txtUser;
    private JPasswordField txtPassword;
    private JButton        btnLocal, btnTCP, btnConnect;
    private JLabel          lblDot, lblStatus;
    private JPanel          pnlTCP, pnlFile;
    private String          connType = "FILE";

    public LoginFrame(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setTitle("Database Connection");
        setSize(460, 570);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(24, 28, 24, 28));
        setContentPane(root);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1; gc.gridx = 0; gc.gridy = 0;

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        header.setBackground(WHITE);
        header.setBorder(new MatteBorder(1, 1, 0, 1, BORDER));

        JLabel iconLbl = new JLabel("DB");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        iconLbl.setForeground(MUTED);
        iconLbl.setOpaque(true);
        iconLbl.setBackground(new Color(0xF1EFE8));
        iconLbl.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(5, 9, 5, 9)));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("New Database Connection");
        lblTitle.setFont(F_TITLE); lblTitle.setForeground(TEXT);
        JLabel lblSub = new JLabel("Connect to SQL Anywhere");
        lblSub.setFont(F_SMALL); lblSub.setForeground(MUTED);
        titleBox.add(lblTitle); titleBox.add(lblSub);

        header.add(iconLbl); header.add(titleBox);
        root.add(header, gc);

        gc.gridy++;
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(WHITE);
        body.setBorder(new CompoundBorder(new MatteBorder(1, 1, 0, 1, BORDER), new EmptyBorder(16, 18, 16, 18)));

        GridBagConstraints bc = new GridBagConstraints();
        bc.fill = GridBagConstraints.HORIZONTAL;
        bc.weightx = 1; bc.gridx = 0; bc.gridy = 0;
        bc.insets = new Insets(0, 0, 6, 0);

        body.add(lbl("CONNECTION TYPE"), bc); bc.gridy++;
        bc.insets = new Insets(0, 0, 14, 0);
        JPanel seg = new JPanel(new GridLayout(1, 2));
        seg.setBackground(WHITE); seg.setBorder(new LineBorder(BORDER, 1));
        seg.setPreferredSize(new Dimension(0, 34));
        btnLocal = segBtn("Local File", true);
        btnTCP   = segBtn("TCP / IP",  false);
        seg.add(btnLocal); seg.add(btnTCP);
        body.add(seg, bc); bc.gridy++;
        bc.insets = new Insets(0, 0, 6, 0);

        body.add(lbl("CONNECTION NAME"), bc); bc.gridy++;
        bc.insets = new Insets(0, 0, 14, 0);
        txtName = field();
        body.add(txtName, bc); bc.gridy++;
        bc.insets = new Insets(0, 0, 6, 0);

        pnlTCP = new JPanel(new GridBagLayout());
        pnlTCP.setOpaque(false); pnlTCP.setVisible(false);
        GridBagConstraints tc = new GridBagConstraints();
        tc.fill = GridBagConstraints.HORIZONTAL;
        tc.weightx = 1; tc.gridx = 0; tc.gridy = 0;

        JPanel hpRow = new JPanel(new GridLayout(1, 2, 10, 0));
        hpRow.setOpaque(false);
        JPanel colH = colPanel("HOST"); txtHost = field(); colH.add(txtHost);
        JPanel colP = colPanel("PORT"); txtPort = field(); colP.add(txtPort);
        hpRow.add(colH); hpRow.add(colP);
        tc.insets = new Insets(0, 0, 14, 0);
        pnlTCP.add(hpRow, tc); tc.gridy++;
        tc.insets = new Insets(0, 0, 6, 0);
        pnlTCP.add(lbl("ENGINE"), tc); tc.gridy++;
        tc.insets = new Insets(0, 0, 0, 0);
        txtEngine = field();
        pnlTCP.add(txtEngine, tc);

        bc.insets = new Insets(0, 0, 14, 0);
        body.add(pnlTCP, bc); bc.gridy++;
        bc.insets = new Insets(0, 0, 6, 0);

        pnlFile = new JPanel(new GridBagLayout());
        pnlFile.setOpaque(false);
        GridBagConstraints fc2 = new GridBagConstraints();
        fc2.fill = GridBagConstraints.HORIZONTAL;
        fc2.weightx = 1; fc2.gridx = 0; fc2.gridy = 0;
        fc2.insets = new Insets(0, 0, 6, 0);
        pnlFile.add(lbl("DATABASE FILE"), fc2); fc2.gridy++;
        fc2.insets = new Insets(0, 0, 0, 0);

        txtDBPath = field();
        txtDBPath.setEditable(false);

        JButton btnBrowse = new JButton("Browse");
        btnBrowse.setFont(F_SMALL); btnBrowse.setForeground(MUTED); btnBrowse.setBackground(new Color(0xF1EFE8));
        btnBrowse.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(0, 12, 0, 12)));
        btnBrowse.setFocusPainted(false); btnBrowse.setPreferredSize(new Dimension(82, 32));
        btnBrowse.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBrowse.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                txtDBPath.setText(ch.getSelectedFile().getAbsolutePath());
        });

        JPanel fileRow = new JPanel(new BorderLayout(8, 0));
        fileRow.setOpaque(false);
        fileRow.add(txtDBPath, BorderLayout.CENTER);
        fileRow.add(btnBrowse, BorderLayout.EAST);
        pnlFile.add(fileRow, fc2);

        bc.insets = new Insets(0, 0, 14, 0);
        body.add(pnlFile, bc); bc.gridy++;
        bc.insets = new Insets(0, 0, 6, 0);

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(BORDER);
        bc.insets = new Insets(0, 0, 14, 0);
        body.add(sep2, bc); bc.gridy++;
        bc.insets = new Insets(0, 0, 6, 0);

        JPanel credRow = new JPanel(new GridLayout(1, 2, 10, 0));
        credRow.setOpaque(false);
        JPanel colU = colPanel("USER"); txtUser = field(); colU.add(txtUser);
        JPanel colPw = colPanel("PASSWORD"); txtPassword = new JPasswordField(); styleField(txtPassword); colPw.add(txtPassword);
        credRow.add(colU); credRow.add(colPw);
        bc.insets = new Insets(0, 0, 18, 0);
        body.add(credRow, bc); bc.gridy++;

        bc.insets = new Insets(0, 0, 0, 0);
        btnConnect = new JButton("CONNECT");
        btnConnect.setFont(F_BTN); btnConnect.setForeground(BTN_FG); btnConnect.setBackground(BTN_BG);
        btnConnect.setOpaque(true); btnConnect.setFocusPainted(false); btnConnect.setBorderPainted(false);
        btnConnect.setPreferredSize(new Dimension(0, 38));
        btnConnect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConnect.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnConnect.setBackground(new Color(0x1A56A0)); }
            public void mouseExited(MouseEvent e)  { btnConnect.setBackground(BTN_BG); }
        });
        btnConnect.addActionListener(e -> connectDatabase());
        body.add(btnConnect, bc);
        root.add(body, gc);

        gc.gridy++;
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 7));
        footer.setBackground(WHITE); footer.setBorder(new MatteBorder(1, 1, 1, 1, BORDER));
        lblDot = new JLabel("●"); lblDot.setFont(new Font("Segoe UI", Font.PLAIN, 9)); lblDot.setForeground(DOT_OFF);
        lblStatus = new JLabel("Disconnected"); lblStatus.setFont(F_SMALL); lblStatus.setForeground(MUTED);
        footer.add(lblDot); footer.add(lblStatus);
        root.add(footer, gc);

        btnLocal.addActionListener(e -> setType("FILE"));
        btnTCP.addActionListener(e -> setType("TCP/IP"));

        setType("FILE");
        setVisible(true);
    }

    private void connectDatabase() {
        Connection conn = DataBaseConnection.connect(
            connType, txtHost.getText(), txtPort.getText(), txtEngine.getText(),
            txtDBPath.getText(), txtUser.getText(), new String(txtPassword.getPassword())
        );

        if (conn != null) {
            lblDot.setForeground(DOT_ON); lblStatus.setText("Connected");
            JOptionPane.showMessageDialog(this, "Connection successful!");
            dashboard.addDatabaseConnection(txtName.getText(), conn);
            dispose();
        } else {
            lblDot.setForeground(DOT_OFF); lblStatus.setText("Connection failed");
            JOptionPane.showMessageDialog(this, "Error connecting to database");
        }
    }

    private void setType(String type) {
        connType = type;
        boolean tcp = type.equals("TCP/IP");

        txtHost.setEnabled(tcp); txtPort.setEnabled(tcp); txtEngine.setEnabled(tcp); txtDBPath.setEnabled(!tcp);
        pnlTCP.setVisible(tcp); pnlFile.setVisible(!tcp);

        btnLocal.setBackground(!tcp ? SEG_ON : WHITE); btnLocal.setForeground(!tcp ? TEXT : MUTED);
        btnTCP.setBackground(tcp ? SEG_ON : WHITE); btnTCP.setForeground(tcp ? TEXT : MUTED);

        revalidate(); repaint();
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL); l.setForeground(MUTED);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JComponent c) {
        c.setFont(F_INPUT); c.setBackground(WHITE); c.setForeground(TEXT);
        c.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(5, 9, 5, 9)));
        c.setPreferredSize(new Dimension(0, 32));
        c.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                c.setBorder(new CompoundBorder(new LineBorder(new Color(0x888780), 1), new EmptyBorder(5, 9, 5, 9)));
            }
            public void focusLost(FocusEvent e) {
                c.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(5, 9, 5, 9)));
            }
        });
    }

    private JButton segBtn(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setBackground(active ? SEG_ON : WHITE); b.setForeground(active ? TEXT : MUTED);
        b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel colPanel(String labelText) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
        p.setOpaque(false); p.add(lbl(labelText));
        return p;
    }
}