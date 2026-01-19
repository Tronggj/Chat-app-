package models;

import java.sql.Timestamp;
import java.io.Serializable;

/**
 * Class Contact - Dai dien cho lien he/ban be
 */
public class Contact implements Serializable {
    private int contactId;
    private int userId;
    private String username;
    private String fullName;
    private String nickname;
    private String avatar;
    private String status;
    private Timestamp lastSeen;
    private int unreadCount;
    private boolean isBlocked;
    
    public Contact() {}
    
    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId = contactId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getLastSeen() { return lastSeen; }
    public void setLastSeen(Timestamp lastSeen) { this.lastSeen = lastSeen; }
    
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    
    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
    
    /**
     * Lay ten hien thi (uu tien nickname)
     */
    public String getDisplayName() {
        if (nickname != null && !nickname.isEmpty()) {
            return nickname;
        }
        return fullName != null ? fullName : username;
    }
    
    /**
     * Lay trang thai voi icon
     */
    public String getStatusIcon() {
        if (status == null) return "⚫";
        switch (status) {
            case "online": return "🟢";
            case "away": return "🟡";
            case "offline": return "⚫";
            default: return "⚫";
        }
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}