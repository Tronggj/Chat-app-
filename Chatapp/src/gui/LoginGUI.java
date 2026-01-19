package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import database.DatabaseHelper;
import models.User;

/**
 * Giao dien dang nhap hien dai
 */
public class LoginGUI extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblMessage;
    
    private final Color PRIMARY_COLOR = new Color(45, 106, 255);
    private final Color ERROR_COLOR = new Color(220, 53, 69);
    private final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private final Color WHITE = Color.WHITE;
    
    public LoginGUI() {
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Chat App - Dang nhap");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(88, 134, 255), 0, h, new Color(45, 106, 255));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(null);
        
        JLabel lblLogo = new JLabel("💬", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        lblLogo.setBounds(0, 40, 450, 100);
        mainPanel.add(lblLogo);
        
        JLabel lblTitle = new JLabel("CHAT APP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(WHITE);
        lblTitle.setBounds(0, 150, 450, 40);
        mainPanel.add(lblTitle);
        
        JLabel lblSubtitle = new JLabel("Ket noi moi luc, moi noi", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(255, 255, 255, 200));
        lblSubtitle.setBounds(0, 190, 450, 25);
        mainPanel.add(lblSubtitle);
        
        JPanel formPanel = new JPanel();
        formPanel.setBackground(WHITE);
        formPanel.setLayout(null);
        formPanel.setBounds(50, 240, 350, 290);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 10), 1, true),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel lblUsername = new JLabel("Ten dang nhap");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsername.setForeground(new Color(108, 117, 125));
        lblUsername.setBounds(30, 20, 290, 25);
        formPanel.add(lblUsername);
        
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setBounds(30, 48, 290, 40);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        formPanel.add(txtUsername);
        
        JLabel lblPassword = new JLabel("Mat khau");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPassword.setForeground(new Color(108, 117, 125));
        lblPassword.setBounds(30, 100, 290, 25);
        formPanel.add(lblPassword);
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBounds(30, 128, 290, 40);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        formPanel.add(txtPassword);
        
        btnLogin = createStyledButton("DANG NHAP", PRIMARY_COLOR);
        btnLogin.setBounds(30, 185, 290, 45);
        btnLogin.addActionListener(e -> handleLogin());
        formPanel.add(btnLogin);
        
        lblMessage = new JLabel("", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMessage.setBounds(30, 235, 290, 25);
        formPanel.add(lblMessage);
        
        mainPanel.add(formPanel);
        
        JLabel lblRegisterPrompt = new JLabel("Chua co tai khoan?", SwingConstants.CENTER);
        lblRegisterPrompt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRegisterPrompt.setForeground(WHITE);
        lblRegisterPrompt.setBounds(100, 540, 130, 25);
        mainPanel.add(lblRegisterPrompt);
        
        JLabel lblRegisterLink = new JLabel("Dang ky ngay");
        lblRegisterLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRegisterLink.setForeground(WHITE);
        lblRegisterLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegisterLink.setBounds(235, 540, 100, 25);
        lblRegisterLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegisterForm();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblRegisterLink.setText("<html><u>Dang ky ngay</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblRegisterLink.setText("Dang ky ngay");
            }
        });
        mainPanel.add(lblRegisterLink);
        
        add(mainPanel);
        
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
        
        setVisible(true);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
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
    
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Vui long nhap day du thong tin!", ERROR_COLOR);
            return;
        }
        
        btnLogin.setEnabled(false);
        btnLogin.setText("Dang dang nhap...");
        
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return DatabaseHelper.login(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        showMessage("Dang nhap thanh cong!", SUCCESS_COLOR);
                        Timer timer = new Timer(500, e -> {
                            dispose();
                            new MainChatGUI(user);
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showMessage("Sai ten dang nhap hoac mat khau!", ERROR_COLOR);
                        btnLogin.setEnabled(true);
                        btnLogin.setText("DANG NHAP");
                    }
                } catch (Exception e) {
                    showMessage("Loi ket noi! Vui long thu lai.", ERROR_COLOR);
                    btnLogin.setEnabled(true);
                    btnLogin.setText("DANG NHAP");
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void showMessage(String message, Color color) {
        lblMessage.setText(message);
        lblMessage.setForeground(color);
    }
    
    private void openRegisterForm() {
        new RegisterGUI(this);
        setVisible(false);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new LoginGUI());
    }
}