package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import database.DatabaseHelper;

/**
 * Form dang ky tai khoan moi
 */
public class RegisterGUI extends JFrame {
    
    private JTextField txtUsername, txtEmail, txtFullName;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JButton btnRegister, btnCancel;
    private JLabel lblMessage;
    private JFrame parentFrame;
    
    private final Color PRIMARY_COLOR = new Color(40, 167, 69);
    private final Color ERROR_COLOR = new Color(220, 53, 69);
    private final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private final Color WHITE = Color.WHITE;
    
    public RegisterGUI(JFrame parent) {
        this.parentFrame = parent;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Dang ky tai khoan");
        setSize(450, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(52, 211, 153), 0, h, new Color(16, 185, 129));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(null);
        
        JLabel lblIcon = new JLabel("✨", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        lblIcon.setBounds(0, 30, 450, 70);
        mainPanel.add(lblIcon);
        
        JLabel lblTitle = new JLabel("TAO TAI KHOAN MOI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(WHITE);
        lblTitle.setBounds(0, 110, 450, 35);
        mainPanel.add(lblTitle);
        
        JPanel formPanel = new JPanel();
        formPanel.setBackground(WHITE);
        formPanel.setLayout(null);
        formPanel.setBounds(50, 165, 350, 420);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 10), 1, true),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        int yPos = 15;
        
        addFormField(formPanel, "Ho va ten", txtFullName = new JTextField(), yPos);
        yPos += 70;
        
        addFormField(formPanel, "Ten dang nhap", txtUsername = new JTextField(), yPos);
        yPos += 70;
        
        addFormField(formPanel, "Email", txtEmail = new JTextField(), yPos);
        yPos += 70;
        
        addFormField(formPanel, "Mat khau", txtPassword = new JPasswordField(), yPos);
        yPos += 70;
        
        addFormField(formPanel, "Nhap lai mat khau", txtConfirmPassword = new JPasswordField(), yPos);
        yPos += 70;
        
        btnRegister = createStyledButton("DANG KY", PRIMARY_COLOR);
        btnRegister.setBounds(30, yPos, 135, 40);
        btnRegister.addActionListener(e -> handleRegister());
        formPanel.add(btnRegister);
        
        btnCancel = createStyledButton("HUY", new Color(108, 117, 125));
        btnCancel.setBounds(180, yPos, 135, 40);
        btnCancel.addActionListener(e -> {
            dispose();
            parentFrame.setVisible(true);
        });
        formPanel.add(btnCancel);
        
        lblMessage = new JLabel("", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMessage.setBounds(30, yPos + 45, 285, 30);
        formPanel.add(lblMessage);
        
        mainPanel.add(formPanel);
        add(mainPanel);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentFrame.setVisible(true);
            }
        });
        
        setVisible(true);
    }
    
    private void addFormField(JPanel panel, String labelText, JTextField field, int yPos) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(108, 117, 125));
        label.setBounds(30, yPos, 285, 20);
        panel.add(label);
        
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBounds(30, yPos + 22, 285, 35);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        panel.add(field);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void handleRegister() {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Vui long dien day du thong tin!", ERROR_COLOR);
            return;
        }
        
        if (username.length() < 4) {
            showMessage("Ten dang nhap phai co it nhat 4 ky tu!", ERROR_COLOR);
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showMessage("Email khong hop le!", ERROR_COLOR);
            return;
        }
        
        if (password.length() < 6) {
            showMessage("Mat khau phai co it nhat 6 ky tu!", ERROR_COLOR);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showMessage("Mat khau khong khop!", ERROR_COLOR);
            return;
        }
        
        btnRegister.setEnabled(false);
        btnCancel.setEnabled(false);
        btnRegister.setText("Dang xu ly...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return DatabaseHelper.registerUser(username, password, email, fullName);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showMessage("Dang ky thanh cong!", SUCCESS_COLOR);
                        JOptionPane.showMessageDialog(RegisterGUI.this,
                            "Dang ky thanh cong!\nBan co the dang nhap ngay bay gio.",
                            "Thanh cong",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        parentFrame.setVisible(true);
                    } else {
                        showMessage("Ten dang nhap hoac email da ton tai!", ERROR_COLOR);
                        btnRegister.setEnabled(true);
                        btnCancel.setEnabled(true);
                        btnRegister.setText("DANG KY");
                    }
                } catch (Exception e) {
                    showMessage("Loi! Vui long thu lai.", ERROR_COLOR);
                    btnRegister.setEnabled(true);
                    btnCancel.setEnabled(true);
                    btnRegister.setText("DANG KY");
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void showMessage(String message, Color color) {
        lblMessage.setText("<html><center>" + message + "</center></html>");
        lblMessage.setForeground(color);
    }
}