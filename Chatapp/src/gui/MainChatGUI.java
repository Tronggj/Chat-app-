package gui;

import database.DatabaseHelper;
import models.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainChatGUI extends JFrame {

    private final User currentUser;
    private Contact selectedContact;

    // ====== THEME ======
    private final Color BG_MAIN = new Color(23, 33, 43);
    private final Color BG_SIDEBAR = new Color(32, 42, 53);
    private final Color BG_HEADER = new Color(38, 48, 59);
    private final Color BG_INPUT = new Color(42, 55, 68);
    private final Color BG_BUBBLE_ME = new Color(87, 141, 250);
    private final Color BG_BUBBLE_OTHER = new Color(48, 60, 74);

    private final Color BLUE = new Color(87, 141, 250);
    private final Color TEXT = new Color(240, 240, 240);
    private final Color TEXT_DIM = new Color(150, 160, 170);
    private final Color DIVIDER = new Color(60, 70, 80);
    private final Color MENU_BG = new Color(250, 250, 250);
    private final Color MENU_TEXT = new Color(30, 30, 30);

    private JList<Contact> contactList;
    private DefaultListModel<Contact> contactModel;

    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextField txtMessage;
    private JLabel lblChatTitle, lblChatStatus;
    private JButton btnOptions;

    private Message selectedMessage = null;

    public MainChatGUI(User user) {
        this.currentUser = user;
        initUI();
        loadContacts();
    }

    private void initUI() {
        setTitle("Chat App - " + currentUser.getFullName());
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        add(createSidebar(), BorderLayout.WEST);
        add(createChatArea(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseHelper.updateUserStatus(currentUser.getUserId(), "offline");
            }
        });

        setVisible(true);
    }

    // ================= SIDEBAR =================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(360, 850));
        sidebar.setBackground(BG_SIDEBAR);

        JPanel header = createSidebarHeader();
        JPanel searchPanel = createSearchPanel();

        contactModel = new DefaultListModel<>();
        contactList = new JList<>(contactModel);
        contactList.setCellRenderer(new ContactRenderer());
        contactList.setBackground(BG_SIDEBAR);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Contact c = contactList.getSelectedValue();
                if (c != null) openChat(c);
            }
        });

        JScrollPane scroll = new JScrollPane(contactList);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        sidebar.add(header, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_SIDEBAR);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);
        
        sidebar.add(centerPanel, BorderLayout.CENTER);

        return sidebar;
    }

    private JPanel createSidebarHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setPreferredSize(new Dimension(360, 70));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnMenu = createIconButton("menu");
        btnMenu.setToolTipText("Menu");
        btnMenu.addActionListener(e -> showProfileMenu(btnMenu));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG_HEADER);
        
        JLabel lblTitle = new JLabel(currentUser.getFullName());
        lblTitle.setForeground(TEXT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel lblOnline = new JLabel("Online");
        lblOnline.setForeground(TEXT_DIM);
        lblOnline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        titlePanel.add(lblTitle, BorderLayout.NORTH);
        titlePanel.add(lblOnline, BorderLayout.CENTER);

        JButton btnAddFriend = createIconButton("add");
        btnAddFriend.setToolTipText("Them ban be");
        btnAddFriend.addActionListener(e -> showAddFriendDialog());

        header.add(btnMenu, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(btnAddFriend, BorderLayout.EAST);

        return header;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDEBAR);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JTextField txtSearch = new JTextField();
        txtSearch.setBackground(BG_INPUT);
        txtSearch.setForeground(TEXT);
        txtSearch.setCaretColor(TEXT);
        txtSearch.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        txtSearch.setText("Tim kiem...");
        txtSearch.setForeground(TEXT_DIM);
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tim kiem...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tim kiem...");
                    txtSearch.setForeground(TEXT_DIM);
                }
            }
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = txtSearch.getText().trim();
                if (!query.isEmpty() && !query.equals("Tim kiem...")) {
                    searchContacts(query);
                } else {
                    loadContacts();
                }
            }
        });

        panel.add(txtSearch);
        return panel;
    }

    // ================= CHAT AREA =================
    private JPanel createChatArea() {
        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(BG_MAIN);

        JPanel header = createChatHeader();

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(BG_MAIN);
        chatPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel inputPanel = createInputPanel();

        chatArea.add(header, BorderLayout.NORTH);
        chatArea.add(chatScrollPane, BorderLayout.CENTER);
        chatArea.add(inputPanel, BorderLayout.SOUTH);

        return chatArea;
    }

    private JPanel createChatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setPreferredSize(new Dimension(1040, 70));
        header.setBorder(new EmptyBorder(10, 25, 10, 25));

        JPanel contactInfo = new JPanel(new BorderLayout(10, 0));
        contactInfo.setBackground(BG_HEADER);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(BG_HEADER);

        lblChatTitle = new JLabel("Chon mot cuoc tro chuyen");
        lblChatTitle.setForeground(TEXT);
        lblChatTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        lblChatStatus = new JLabel("");
        lblChatStatus.setForeground(TEXT_DIM);
        lblChatStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        textPanel.add(lblChatTitle, BorderLayout.NORTH);
        textPanel.add(lblChatStatus, BorderLayout.CENTER);

        contactInfo.add(textPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(BG_HEADER);

        JButton btnSearch = createIconButton("search");
        btnSearch.setToolTipText("Tim kiem trong chat");
        btnSearch.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chuc nang tim kiem chua kha dung"));

        JButton btnCall = createIconButton("call");
        btnCall.setToolTipText("Goi dien");
        btnCall.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chuc nang goi dien chua kha dung"));

        btnOptions = createIconButton("options");
        btnOptions.setToolTipText("Tuy chon");
        btnOptions.addActionListener(e -> showChatOptions(btnOptions));

        actions.add(btnSearch);
        actions.add(btnCall);
        actions.add(btnOptions);

        header.add(contactInfo, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BG_HEADER);
        panel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JButton btnAttach = createIconButton("attach");
        btnAttach.setToolTipText("Dinh kem file");
        btnAttach.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chuc nang gui file chua kha dung"));

        txtMessage = new JTextField();
        txtMessage.setBackground(BG_INPUT);
        txtMessage.setForeground(TEXT);
        txtMessage.setCaretColor(TEXT);
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtMessage.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(12, 18, 12, 18)
        ));
        txtMessage.addActionListener(e -> sendMessage());

        JButton btnEmoji = createIconButton("emoji");
        btnEmoji.setToolTipText("Chon bieu tuong cam xuc");
        btnEmoji.addActionListener(e -> showEmojiPicker());

        JButton btnSend = createIconButton("send");
        btnSend.setBackground(BLUE);
        btnSend.setOpaque(true);
        btnSend.setToolTipText("Gui tin nhan");
        btnSend.addActionListener(e -> sendMessage());

        JPanel inputWrapper = new JPanel(new BorderLayout(10, 0));
        inputWrapper.setBackground(BG_HEADER);
        inputWrapper.add(btnAttach, BorderLayout.WEST);
        inputWrapper.add(txtMessage, BorderLayout.CENTER);
        inputWrapper.add(btnEmoji, BorderLayout.EAST);

        panel.add(inputWrapper, BorderLayout.CENTER);
        panel.add(btnSend, BorderLayout.EAST);

        return panel;
    }

    // ================= HELPER METHODS =================
    private JButton createTextIconButton(String text, int fontSize) {
        JButton btn = new JButton(text);
        
        // Dùng font đơn giản, bold để dễ nhìn
        btn.setFont(new Font("Dialog", Font.BOLD, fontSize));
        btn.setForeground(TEXT);
        btn.setBackground(BG_HEADER);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(text.length() > 2 ? 50 : 40, 40));
        
        btn.addMouseListener(new MouseAdapter() {
            Color originalBg = btn.getBackground();
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BG_INPUT);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalBg);
            }
        });
        
        return btn;
    }
    
    // Tạo button với icon vẽ bằng Graphics (không cần file ảnh)
    private JButton createIconButton(String iconType) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getForeground());
                g2.setStroke(new BasicStroke(2));
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                switch (iconType) {
                    case "menu": // 3 gạch ngang
                        g2.drawLine(cx - 8, cy - 6, cx + 8, cy - 6);
                        g2.drawLine(cx - 8, cy, cx + 8, cy);
                        g2.drawLine(cx - 8, cy + 6, cx + 8, cy + 6);
                        break;
                        
                    case "add": // Dấu cộng
                        g2.setStroke(new BasicStroke(3));
                        g2.drawLine(cx, cy - 8, cx, cy + 8);
                        g2.drawLine(cx - 8, cy, cx + 8, cy);
                        break;
                        
                    case "search": // Kính lúp
                        g2.drawOval(cx - 6, cy - 6, 10, 10);
                        g2.drawLine(cx + 4, cy + 4, cx + 8, cy + 8);
                        break;
                        
                    case "call": // Điện thoại
                        g2.drawArc(cx - 7, cy - 8, 6, 6, 0, 90);
                        g2.drawLine(cx - 7, cy - 2, cx - 5, cy);
                        g2.drawLine(cx - 5, cy, cx + 3, cy + 8);
                        g2.drawArc(cx + 1, cy + 2, 6, 6, 180, 90);
                        break;
                        
                    case "options": // 3 chấm dọc
                        g2.fillOval(cx - 2, cy - 8, 4, 4);
                        g2.fillOval(cx - 2, cy - 2, 4, 4);
                        g2.fillOval(cx - 2, cy + 4, 4, 4);
                        break;
                        
                    case "attach": // Kẹp giấy đẹp hơn
                        // Vẽ hình kẹp giấy
                        g2.drawLine(cx + 2, cy - 8, cx + 2, cy + 2);
                        g2.drawArc(cx - 6, cy - 2, 8, 8, 0, 180);
                        g2.drawLine(cx - 6, cy + 2, cx - 6, cy - 6);
                        g2.drawArc(cx - 4, cy - 8, 4, 4, 180, 180);
                        break;
                        
                    case "emoji": // Mặt cười
                        g2.drawOval(cx - 8, cy - 8, 16, 16);
                        g2.fillOval(cx - 4, cy - 3, 2, 2);
                        g2.fillOval(cx + 2, cy - 3, 2, 2);
                        g2.drawArc(cx - 5, cy - 1, 10, 6, 0, -180);
                        break;
                        
                    case "send": // Mũi tên
                        int[] xPoints = {cx - 6, cx + 8, cx - 6};
                        int[] yPoints = {cy - 6, cy, cy + 6};
                        g2.fillPolygon(xPoints, yPoints, 3);
                        break;
                }
            }
        };
        
        btn.setForeground(TEXT);
        btn.setBackground(BG_HEADER);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 40));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setOpaque(true);
                btn.setBackground(BG_INPUT);
                btn.repaint();
            }
            public void mouseExited(MouseEvent e) {
                btn.setOpaque(false);
                btn.repaint();
            }
        });
        
        return btn;
    }

    // ================= DATA LOADING =================
    private void loadContacts() {
        contactModel.clear();
        List<Contact> contacts = DatabaseHelper.getFriendsList(currentUser.getUserId());
        for (Contact c : contacts) {
            contactModel.addElement(c);
        }
    }

    private void searchContacts(String query) {
        contactModel.clear();
        List<Contact> all = DatabaseHelper.getFriendsList(currentUser.getUserId());
        for (Contact c : all) {
            if (c.getDisplayName().toLowerCase().contains(query.toLowerCase()) ||
                c.getUsername().toLowerCase().contains(query.toLowerCase())) {
                contactModel.addElement(c);
            }
        }
    }

    private void openChat(Contact c) {
        selectedContact = c;
        lblChatTitle.setText(c.getDisplayName());
        
        String status = "offline";
        if (c.getStatus() != null) {
            status = c.getStatus();
        }
        
        String statusText = status.equals("online") ? "Dang hoat dong" : "Khong hoat dong";
        lblChatStatus.setText(statusText);
        
        // Đánh dấu tin nhắn đã đọc
        DatabaseHelper.markMessagesAsRead(currentUser.getUserId(), c.getUserId());
        
        // Load tin nhắn
        loadMessages();
        
        // Cập nhật unread count cho contact này về 0
        c.setUnreadCount(0);
        contactList.repaint();
    }

    private void loadMessages() {
        chatPanel.removeAll();

        List<Message> allMsgs = DatabaseHelper.getChatHistory(
                currentUser.getUserId(),
                selectedContact.getUserId(),
                100
        );

        // Filter tin nhắn: loại bỏ trùng lặp và tin nhắn đã xóa
        java.util.Set<Integer> seenIds = new java.util.HashSet<>();
        List<Message> msgs = new java.util.ArrayList<>();
        
        for (Message m : allMsgs) {
            // Chỉ thêm nếu chưa thấy messageId này và chưa bị xóa
            if (!seenIds.contains(m.getMessageId())) {
                seenIds.add(m.getMessageId());
                
                // Kiểm tra xem tin nhắn có bị xóa không
                boolean isDeleted = false;
                if (m.getMessageType() != null && m.getMessageType().equals("deleted")) {
                    isDeleted = true;
                }
                
                if (!isDeleted) {
                    msgs.add(m);
                }
            }
        }

        if (msgs.isEmpty()) {
            JLabel empty = new JLabel("Chua co tin nhan nao");
            empty.setForeground(TEXT_DIM);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 15));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            chatPanel.add(Box.createVerticalGlue());
            chatPanel.add(empty);
            chatPanel.add(Box.createVerticalGlue());
        } else {
            for (Message m : msgs) {
                boolean isMe = m.getSenderId() == currentUser.getUserId();
                chatPanel.add(createMessageBubble(m, isMe));
                chatPanel.add(Box.createVerticalStrut(8));
            }
        }

        chatPanel.revalidate();
        chatPanel.repaint();
        
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private JPanel createMessageBubble(Message msg, boolean isMe) {
        // Wrapper panel - căn lề trái hoặc phải
        JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapper.setBackground(BG_MAIN);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Bubble panel
        JPanel bubble = new JPanel(new BorderLayout(8, 0));
        bubble.setBackground(isMe ? BG_BUBBLE_ME : BG_BUBBLE_OTHER);
        bubble.setBorder(new CompoundBorder(
            new LineBorder(isMe ? BG_BUBBLE_ME : BG_BUBBLE_OTHER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        // Text + Time container
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.setOpaque(false);

        // Text label
        String content = msg.getContent().replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        JLabel txtLabel = new JLabel("<html>" + content + "</html>");
        txtLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtLabel.setForeground(Color.WHITE);
        
        // Time label
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String timeStr = msg.getTimestamp() != null ? sdf.format(msg.getTimestamp()) : "";
        
        JLabel lblTime = new JLabel(timeStr);
        lblTime.setForeground(new Color(255, 255, 255, 160));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTime.setBorder(new EmptyBorder(0, 8, 0, 0));

        // Add components với spacing
        contentPanel.add(txtLabel);
        contentPanel.add(Box.createHorizontalGlue());
        contentPanel.add(lblTime);

        bubble.add(contentPanel, BorderLayout.CENTER);

        // Right-click menu cho tin nhắn của mình
        if (isMe) {
            bubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
            bubble.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        selectedMessage = msg;
                        showMessageContextMenu(bubble, e.getX(), e.getY());
                    }
                }
            });
        }

        wrapper.add(bubble);
        return wrapper;
    }

    // ================= ACTIONS =================
    private void sendMessage() {
        if (selectedContact == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon nguoi de nhan tin!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String msg = txtMessage.getText().trim();
        if (msg.isEmpty()) return;

        boolean success = DatabaseHelper.sendMessage(
                currentUser.getUserId(),
                selectedContact.getUserId(),
                msg
        );
        
        if (success) {
            txtMessage.setText("");
            loadMessages();
        } else {
            JOptionPane.showMessageDialog(this, "Loi gui tin nhan!", "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMessageContextMenu(Component comp, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(MENU_BG);
        menu.setBorder(new LineBorder(DIVIDER, 1));

        JMenuItem editItem = new JMenuItem("Sua tin nhan");
        editItem.setBackground(MENU_BG);
        editItem.setForeground(MENU_TEXT);
        editItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        editItem.addActionListener(e -> editMessage());

        JMenuItem deleteItem = new JMenuItem("Xoa tin nhan");
        deleteItem.setBackground(MENU_BG);
        deleteItem.setForeground(new Color(220, 53, 69));
        deleteItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteItem.addActionListener(e -> deleteMessage());

        menu.add(editItem);
        menu.add(deleteItem);
        menu.show(comp, x, y);
    }

    private void editMessage() {
        if (selectedMessage == null) return;

        String newContent = JOptionPane.showInputDialog(this, 
            "Nhap noi dung moi:", 
            selectedMessage.getContent());

        if (newContent != null && !newContent.trim().isEmpty()) {
            boolean success = DatabaseHelper.editMessage(selectedMessage.getMessageId(), newContent.trim());
            if (success) {
                loadMessages();
                JOptionPane.showMessageDialog(this, "Da sua tin nhan thanh cong!");
            } else {
                JOptionPane.showMessageDialog(this, "Loi sua tin nhan!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteMessage() {
        if (selectedMessage == null) return;

        int choice = JOptionPane.showConfirmDialog(this, 
            "Ban co chac muon xoa tin nhan nay?", 
            "Xac nhan", 
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = DatabaseHelper.deleteMessage(selectedMessage.getMessageId());
            if (success) {
                loadMessages();
                JOptionPane.showMessageDialog(this, "Da xoa tin nhan thanh cong!");
            } else {
                JOptionPane.showMessageDialog(this, "Loi xoa tin nhan!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddFriendDialog() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblInstruction = new JLabel("Tim kiem nguoi dung:");
        lblInstruction.setForeground(MENU_TEXT);
        lblInstruction.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JTextField txtSearch = new JTextField();
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setForeground(MENU_TEXT);
        txtSearch.setCaretColor(MENU_TEXT);
        txtSearch.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        DefaultListModel<User> userModel = new DefaultListModel<>();
        JList<User> userList = new JList<>(userModel);
        userList.setBackground(Color.WHITE);
        userList.setForeground(MENU_TEXT);
        userList.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane scroll = new JScrollPane(userList);
        scroll.setPreferredSize(new Dimension(300, 200));
        scroll.setBorder(new LineBorder(DIVIDER, 1));

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = txtSearch.getText().trim();
                userModel.clear();
                if (!query.isEmpty()) {
                    List<User> users = DatabaseHelper.searchUsers(query, currentUser.getUserId());
                    for (User u : users) {
                        userModel.addElement(u);
                    }
                }
            }
        });

        panel.add(lblInstruction, BorderLayout.NORTH);
        panel.add(txtSearch, BorderLayout.CENTER);
        panel.add(scroll, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Them ban be", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION && userList.getSelectedValue() != null) {
            User selectedUser = userList.getSelectedValue();
            
            String nickname = JOptionPane.showInputDialog(this, 
                "Nhap biet danh cho " + selectedUser.getFullName() + ":", 
                selectedUser.getFullName());

            if (nickname != null && !nickname.trim().isEmpty()) {
                boolean success = DatabaseHelper.addFriend(
                    currentUser.getUserId(), 
                    selectedUser.getUserId(), 
                    nickname.trim()
                );
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Da them ban be thanh cong!");
                    loadContacts();
                } else {
                    JOptionPane.showMessageDialog(this, "Loi them ban be hoac da la ban be!", "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showChatOptions(Component comp) {
        if (selectedContact == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon nguoi de tro chuyen truoc!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(MENU_BG);
        menu.setBorder(new LineBorder(DIVIDER, 1));

        JMenuItem clearChat = new JMenuItem("Xoa tat ca tin nhan");
        clearChat.setBackground(MENU_BG);
        clearChat.setForeground(MENU_TEXT);
        clearChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearChat.addActionListener(e -> clearChatHistory());

        JMenuItem removeFriend = new JMenuItem("Xoa ban be");
        removeFriend.setBackground(MENU_BG);
        removeFriend.setForeground(new Color(220, 53, 69));
        removeFriend.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        removeFriend.addActionListener(e -> removeFriend());

        menu.add(clearChat);
        menu.addSeparator();
        menu.add(removeFriend);
        
        menu.show(comp, 0, comp.getHeight());
    }

    private void clearChatHistory() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Ban co chac muon xoa tat ca tin nhan voi " + selectedContact.getDisplayName() + "?",
            "Xac nhan",
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = DatabaseHelper.deleteAllMessages(
                currentUser.getUserId(),
                selectedContact.getUserId()
            );
            
            if (success) {
                loadMessages();
                JOptionPane.showMessageDialog(this, "Da xoa tat ca tin nhan thanh cong!");
            } else {
                JOptionPane.showMessageDialog(this, "Loi xoa tin nhan!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeFriend() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Ban co chac muon xoa " + selectedContact.getDisplayName() + " khoi danh sach ban be?",
            "Xac nhan",
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = DatabaseHelper.removeFriend(
                currentUser.getUserId(),
                selectedContact.getUserId()
            );
            
            if (success) {
                loadContacts();
                selectedContact = null;
                lblChatTitle.setText("Chon mot cuoc tro chuyen");
                lblChatStatus.setText("");
                chatPanel.removeAll();
                chatPanel.revalidate();
                chatPanel.repaint();
                JOptionPane.showMessageDialog(this, "Da xoa ban be thanh cong!");
            } else {
                JOptionPane.showMessageDialog(this, "Loi xoa ban be!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showProfileMenu(Component comp) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(MENU_BG);
        menu.setBorder(new LineBorder(DIVIDER, 1));

        JMenuItem profile = new JMenuItem("Thong tin ca nhan");
        profile.setBackground(MENU_BG);
        profile.setForeground(MENU_TEXT);
        profile.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        profile.addActionListener(e -> showProfile());

        JMenuItem settings = new JMenuItem("Cai dat");
        settings.setBackground(MENU_BG);
        settings.setForeground(MENU_TEXT);
        settings.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem logout = new JMenuItem("Dang xuat");
        logout.setBackground(MENU_BG);
        logout.setForeground(new Color(220, 53, 69));
        logout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logout.addActionListener(e -> logout());

        menu.add(profile);
        menu.add(settings);
        menu.addSeparator();
        menu.add(logout);
        
        menu.show(comp, 0, comp.getHeight());
    }

    private void showProfile() {
        JOptionPane.showMessageDialog(this,
            "Ten: " + currentUser.getFullName() + "\n" +
            "Username: " + currentUser.getUsername() + "\n" +
            "Email: " + currentUser.getEmail(),
            "Thong tin ca nhan",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Ban co chac muon dang xuat?",
            "Xac nhan",
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            DatabaseHelper.updateUserStatus(currentUser.getUserId(), "offline");
            dispose();
            new LoginGUI();
        }
    }

    private void showEmojiPicker() {
        String[] emojis = {"(^_^)", "(-_-)", "(@_@)", "(>_<)", "(O_O)", "(T_T)", 
                          "(^o^)", "(*_*)", "(0_0)", "(^-^)"};
        String selected = (String) JOptionPane.showInputDialog(this,
            "Chon bieu tuong cam xuc:",
            "Bieu tuong",
            JOptionPane.PLAIN_MESSAGE,
            null,
            emojis,
            emojis[0]);
        
        if (selected != null) {
            txtMessage.setText(txtMessage.getText() + " " + selected);
        }
    }

    // ================= CONTACT RENDERER =================
    class ContactRenderer extends JPanel implements ListCellRenderer<Contact> {
        JLabel lblName, lblStatus, lblUnread;
        
        ContactRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(15, 20, 15, 20));
            
            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setOpaque(false);
            
            lblName = new JLabel();
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblName.setForeground(TEXT);
            
            lblStatus = new JLabel();
            lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblStatus.setForeground(TEXT_DIM);
            
            textPanel.add(lblName, BorderLayout.NORTH);
            textPanel.add(lblStatus, BorderLayout.CENTER);
            
            lblUnread = new JLabel();
            lblUnread.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblUnread.setForeground(Color.WHITE);
            lblUnread.setBackground(BLUE);
            lblUnread.setOpaque(true);
            lblUnread.setBorder(new EmptyBorder(3, 8, 3, 8));
            lblUnread.setVisible(false);
            
            add(textPanel, BorderLayout.CENTER);
            add(lblUnread, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Contact> list,
                                                     Contact value,
                                                     int index,
                                                     boolean isSelected,
                                                     boolean cellHasFocus) {
            lblName.setText(value.getDisplayName());
            
            String status = value.getStatus() != null ? value.getStatus() : "offline";
            String statusIcon = status.equals("online") ? "O" : "o";
            String statusText = status.equals("online") ? "Dang hoat dong" : "Khong hoat dong";
            lblStatus.setText(statusIcon + " " + statusText);
            
            if (value.getUnreadCount() > 0) {
                lblUnread.setText(String.valueOf(value.getUnreadCount()));
                lblUnread.setVisible(true);
            } else {
                lblUnread.setVisible(false);
            }
            
            setBackground(isSelected ? BG_INPUT : BG_SIDEBAR);
            return this;
        }
    }
}