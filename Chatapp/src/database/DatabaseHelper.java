package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.*;

/**
 * DatabaseHelper - PHIEN BAN HOAN CHINH
 * Fix socket closed + Them chuc nang xoa/sua tin nhan
 */
public class DatabaseHelper {
    
    // THONG TIN KET NOI SQL SERVER - SUA LAI CHO DUNG
    private static final String URL = 
        "jdbc:sqlserver://localhost:1433;" +
        "databaseName=chat_app;" +
        "user=chatapp;" +
        "password=Chatapp@123;" +
        "encrypt=false;" +
        "trustServerCertificate=true";
    
    /**
     * Lay ket noi moi moi lan (Fix loi Socket closed)
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            System.err.println("✗ Loi ket noi SQL Server: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static void closeConnection() {
        // Khong can thiet - try-with-resources tu dong dong
    }
    
    // ==================== USER OPERATIONS ====================
    
    public static boolean registerUser(String username, String password, String email, String fullName) {
        String sql = "INSERT INTO users (username, password, email, full_name) VALUES (?, CONVERT(VARCHAR(32), HashBytes('MD5', ?), 2), ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, fullName);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Loi dang ky: " + e.getMessage());
            return false;
        }
    }
    
    public static User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = CONVERT(VARCHAR(32), HashBytes('MD5', ?), 2) AND is_active = 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setAvatar(rs.getString("avatar"));
                user.setStatus(rs.getString("status"));
                user.setLastSeen(rs.getTimestamp("last_seen"));
                
                updateUserStatus(user.getUserId(), "online");
                return user;
            }
            
        } catch (SQLException e) {
            System.err.println("Loi dang nhap: " + e.getMessage());
        }
        return null;
    }
    
    public static void updateUserStatus(int userId, String status) {
        String sql = "{CALL UpdateUserStatus(?, ?)}";
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, status);
            stmt.execute();
            
        } catch (SQLException e) {
            // Silent fail
        }
    }
    
    public static User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setAvatar(rs.getString("avatar"));
                user.setStatus(rs.getString("status"));
                user.setLastSeen(rs.getTimestamp("last_seen"));
                return user;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static List<User> searchUsers(String searchTerm, int currentUserId) {
        List<User> users = new ArrayList<>();
        String sql = "{CALL SearchUsers(?, ?)}";
        
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setString(1, searchTerm);
            stmt.setInt(2, currentUserId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setAvatar(rs.getString("avatar"));
                user.setStatus(rs.getString("status"));
                users.add(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    public static boolean updateUser(int userId, String email, String fullName) {
        String sql = "UPDATE users SET email = ?, full_name = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, fullName);
            stmt.setInt(3, userId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean deleteUser(int userId) {
        String sql = "UPDATE users SET is_active = 0 WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== MESSAGE OPERATIONS ====================
    
    public static boolean sendMessage(int senderId, int receiverId, String content) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static List<Message> getChatHistory(int user1Id, int user2Id, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "{CALL GetChatHistory(?, ?, ?)}";
        
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, limit);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message();
                msg.setMessageId(rs.getInt("message_id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setReceiverId(rs.getInt("receiver_id"));
                msg.setContent(rs.getString("content"));
                msg.setTimestamp(rs.getTimestamp("timestamp"));
                msg.setRead(rs.getBoolean("is_read"));
                messages.add(msg);
            }
            
        } catch (SQLException e) {
            // Silent fail
        }
        
        java.util.Collections.reverse(messages);
        return messages;
    }
    
    public static void markMessagesAsRead(int receiverId, int senderId) {
        String sql = "{CALL MarkMessagesAsRead(?, ?)}";
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, receiverId);
            stmt.setInt(2, senderId);
            stmt.execute();
            
        } catch (SQLException e) {
            // Silent fail
        }
    }
    
    public static boolean deleteMessage(int messageId) {
        String sql = "UPDATE messages SET is_deleted = 1 WHERE message_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * XOA TOAN BO lich su chat giua 2 nguoi - MOI THEM
     */
    public static boolean deleteAllMessages(int user1Id, int user2Id) {
        String sql = "DELETE FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);
            
            return stmt.executeUpdate() >= 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * SUA tin nhan - MOI THEM
     */
    public static boolean editMessage(int messageId, String newContent) {
        String sql = "UPDATE messages SET content = ? WHERE message_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newContent);
            stmt.setInt(2, messageId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ? AND is_read = 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            // Silent fail
        }
        return 0;
    }
    
    // ==================== CONTACT OPERATIONS ====================
    
    public static List<Contact> getFriendsList(int userId) {
        List<Contact> contacts = new ArrayList<>();
        String sql = "{CALL GetFriendsList(?)}";
        
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Contact contact = new Contact();
                contact.setUserId(rs.getInt("user_id"));
                contact.setUsername(rs.getString("username"));
                contact.setFullName(rs.getString("full_name"));
                contact.setAvatar(rs.getString("avatar"));
                contact.setStatus(rs.getString("status"));
                contact.setLastSeen(rs.getTimestamp("last_seen"));
                contact.setNickname(rs.getString("nickname"));
                contact.setUnreadCount(rs.getInt("unread_count"));
                contacts.add(contact);
            }
            
        } catch (SQLException e) {
            // Silent fail
        }
        return contacts;
    }
    
    public static boolean addFriend(int userId, int friendId, String nickname) {
        String sql = "INSERT INTO contacts (user_id, friend_id, nickname) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setString(3, nickname);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Loi them ban: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM contacts WHERE user_id = ? AND friend_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean updateNickname(int userId, int friendId, String nickname) {
        String sql = "UPDATE contacts SET nickname = ? WHERE user_id = ? AND friend_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nickname);
            stmt.setInt(2, userId);
            stmt.setInt(3, friendId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isFriend(int userId, int friendId) {
        String sql = "SELECT COUNT(*) as count FROM contacts WHERE user_id = ? AND friend_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            // Silent fail
        }
        return false;
    }
}