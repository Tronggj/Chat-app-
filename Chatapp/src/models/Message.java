package models;

import java.sql.Timestamp;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Class Message - Dai dien cho tin nhan
 */
public class Message implements Serializable {
    private int messageId;
    private int senderId;
    private int receiverId;
    private String content;
    private Timestamp timestamp;
    private boolean isRead;
    private String messageType;
    
    public Message() {}
    
    public Message(int senderId, int receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }
    
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }
    
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    /**
     * Dinh dang thoi gian hien thi
     */
    public String getFormattedTime() {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(timestamp);
    }
    
    @Override
    public String toString() {
        return content;
    }
}