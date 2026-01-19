-- =====================================================
-- PHAN MEM CHAT ONLINE - SQL SERVER SCRIPT
-- =====================================================

-- Xoa database cu neu ton tai
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'chat_app')
BEGIN
    ALTER DATABASE chat_app SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE chat_app;
END
GO

-- Tao database moi
CREATE DATABASE chat_app;
GO

USE chat_app;
GO

-- =====================================================
-- BANG 1: USERS (Nguoi dung)
-- =====================================================
CREATE TABLE users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    full_name NVARCHAR(100),
    avatar NVARCHAR(255) DEFAULT 'default_avatar.png',
    status NVARCHAR(20) DEFAULT 'offline',
    last_seen DATETIME DEFAULT GETDATE(),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    is_active BIT DEFAULT 1
);
GO

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_status ON users(status);
GO

-- =====================================================
-- BANG 2: MESSAGES (Tin nhan)
-- =====================================================
CREATE TABLE messages (
    message_id INT PRIMARY KEY IDENTITY(1,1),
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    timestamp DATETIME DEFAULT GETDATE(),
    is_read BIT DEFAULT 0,
    message_type NVARCHAR(20) DEFAULT 'text',
    is_deleted BIT DEFAULT 0,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);
GO

CREATE INDEX idx_sender ON messages(sender_id);
CREATE INDEX idx_receiver ON messages(receiver_id);
CREATE INDEX idx_timestamp ON messages(timestamp);
CREATE INDEX idx_conversation ON messages(sender_id, receiver_id, timestamp);
GO

-- =====================================================
-- BANG 3: CONTACTS (Danh ba ban be)
-- =====================================================
CREATE TABLE contacts (
    contact_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    nickname NVARCHAR(100),
    is_blocked BIT DEFAULT 0,
    added_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(user_id),
    CONSTRAINT unique_contact UNIQUE (user_id, friend_id)
);
GO

CREATE INDEX idx_user_contacts ON contacts(user_id);
GO

-- =====================================================
-- BANG 4: GROUPS (Nhom chat - mo rong)
-- =====================================================
CREATE TABLE chat_groups (
    group_id INT PRIMARY KEY IDENTITY(1,1),
    group_name NVARCHAR(100) NOT NULL,
    creator_id INT NOT NULL,
    avatar NVARCHAR(255) DEFAULT 'default_group.png',
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (creator_id) REFERENCES users(user_id) ON DELETE CASCADE
);
GO

-- =====================================================
-- BANG 5: GROUP_MEMBERS (Thanh vien nhom)
-- =====================================================
CREATE TABLE group_members (
    member_id INT PRIMARY KEY IDENTITY(1,1),
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    role NVARCHAR(20) DEFAULT 'member',
    joined_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (group_id) REFERENCES chat_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT unique_member UNIQUE (group_id, user_id)
);
GO

-- =====================================================
-- DU LIEU MAU (Test Data)
-- =====================================================

-- Them nguoi dung mau (mat khau: 123456 - da ma hoa MD5)
INSERT INTO users (username, password, email, full_name, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin@chat.com', N'Quản Trị Viên', 'online'),
('nguyenvana', 'e10adc3949ba59abbe56e057f20f883e', 'vana@email.com', N'Nguyễn Văn A', 'online'),
('tranthib', 'e10adc3949ba59abbe56e057f20f883e', 'thib@email.com', N'Trần Thị B', 'offline'),
('levanc', 'e10adc3949ba59abbe56e057f20f883e', 'vanc@email.com', N'Lê Văn C', 'online'),
('phamthid', 'e10adc3949ba59abbe56e057f20f883e', 'thid@email.com', N'Phạm Thị D', 'away');
GO

-- Them danh ba
INSERT INTO contacts (user_id, friend_id, nickname) VALUES
(1, 2, N'Bạn A'),
(1, 3, N'Bạn B'),
(1, 4, N'Bạn C'),
(2, 1, N'Admin'),
(2, 3, N'Trần B'),
(3, 1, N'Admin'),
(3, 2, N'Nguyễn A');
GO

-- Them tin nhan mau
INSERT INTO messages (sender_id, receiver_id, content, is_read) VALUES
(1, 2, N'Chào bạn! Bạn khỏe không?', 1),
(2, 1, N'Mình khỏe, cảm ơn bạn!', 1),
(1, 2, N'Hôm nay bạn có rảnh không?', 0),
(3, 1, N'Admin ơi, cho em hỏi chút!', 0),
(1, 3, N'Có gì bạn cứ hỏi nhé', 1);
GO

-- =====================================================
-- STORED PROCEDURES
-- =====================================================

-- Procedure: Lay lich su chat giua 2 nguoi
CREATE PROCEDURE GetChatHistory
    @user1_id INT,
    @user2_id INT,
    @limit INT
AS
BEGIN
    SELECT TOP (@limit)
        m.message_id,
        m.sender_id,
        m.receiver_id,
        m.content,
        m.timestamp,
        m.is_read,
        u1.username AS sender_name,
        u2.username AS receiver_name
    FROM messages m
    JOIN users u1 ON m.sender_id = u1.user_id
    JOIN users u2 ON m.receiver_id = u2.user_id
    WHERE 
        (m.sender_id = @user1_id AND m.receiver_id = @user2_id)
        OR
        (m.sender_id = @user2_id AND m.receiver_id = @user1_id)
    ORDER BY m.timestamp DESC;
END
GO

-- Procedure: Lay danh sach ban be
CREATE PROCEDURE GetFriendsList
    @user_id INT
AS
BEGIN
    SELECT 
        u.user_id,
        u.username,
        u.full_name,
        u.avatar,
        u.status,
        u.last_seen,
        c.nickname,
        (SELECT COUNT(*) 
         FROM messages 
         WHERE sender_id = u.user_id 
         AND receiver_id = @user_id 
         AND is_read = 0) AS unread_count
    FROM contacts c
    JOIN users u ON c.friend_id = u.user_id
    WHERE c.user_id = @user_id 
    AND c.is_blocked = 0
    AND u.is_active = 1
    ORDER BY u.status DESC, u.full_name ASC;
END
GO

-- Procedure: Tim kiem nguoi dung
CREATE PROCEDURE SearchUsers
    @search_term NVARCHAR(100),
    @current_user_id INT
AS
BEGIN
    SELECT TOP 20
        u.user_id,
        u.username,
        u.full_name,
        u.email,
        u.avatar,
        u.status,
        CASE WHEN EXISTS(
            SELECT 1 FROM contacts 
            WHERE user_id = @current_user_id 
            AND friend_id = u.user_id
        ) THEN 1 ELSE 0 END AS is_friend
    FROM users u
    WHERE u.user_id != @current_user_id
    AND u.is_active = 1
    AND (
        u.username LIKE '%' + @search_term + '%'
        OR u.full_name LIKE '%' + @search_term + '%'
        OR u.email LIKE '%' + @search_term + '%'
    )
    ORDER BY u.full_name ASC;
END
GO

-- Procedure: Cap nhat trang thai online
CREATE PROCEDURE UpdateUserStatus
    @user_id INT,
    @status NVARCHAR(20)
AS
BEGIN
    UPDATE users 
    SET status = @status,
        last_seen = GETDATE()
    WHERE user_id = @user_id;
END
GO

-- Procedure: Danh dau tin nhan da doc
CREATE PROCEDURE MarkMessagesAsRead
    @receiver_id INT,
    @sender_id INT
AS
BEGIN
    UPDATE messages 
    SET is_read = 1
    WHERE receiver_id = @receiver_id 
    AND sender_id = @sender_id
    AND is_read = 0;
END
GO

-- =====================================================
-- VIEWS (Cac view huu ich)
-- =====================================================

-- View: Thong ke nguoi dung
CREATE VIEW user_statistics AS
SELECT 
    u.user_id,
    u.username,
    u.full_name,
    u.status,
    (SELECT COUNT(*) FROM contacts WHERE user_id = u.user_id) AS total_friends,
    (SELECT COUNT(*) FROM messages WHERE sender_id = u.user_id) AS messages_sent,
    (SELECT COUNT(*) FROM messages WHERE receiver_id = u.user_id) AS messages_received,
    u.created_at,
    u.last_seen
FROM users u;
GO

-- View: Tin nhan chua doc
CREATE VIEW unread_messages_summary AS
SELECT 
    m.receiver_id,
    m.sender_id,
    u.username AS sender_name,
    u.full_name AS sender_fullname,
    COUNT(*) AS unread_count,
    MAX(m.timestamp) AS last_message_time
FROM messages m
JOIN users u ON m.sender_id = u.user_id
WHERE m.is_read = 0
GROUP BY m.receiver_id, m.sender_id, u.username, u.full_name;
GO

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Trigger: Tu dong cap nhat last_seen khi gui tin nhan
CREATE TRIGGER update_last_seen_on_message
ON messages
AFTER INSERT
AS
BEGIN
    UPDATE users 
    SET last_seen = GETDATE()
    WHERE user_id IN (SELECT sender_id FROM inserted);
END
GO

-- =====================================================
-- INDEXES BO SUNG DE TOI UU HIEU NANG
-- =====================================================
CREATE INDEX idx_unread ON messages(receiver_id, is_read, timestamp);
CREATE INDEX idx_active_users ON users(is_active, status);
GO

-- =====================================================
-- TEST QUERIES (Cac cau truy van test)
-- =====================================================

-- Test 1: Lay lich su chat
EXEC GetChatHistory @user1_id = 1, @user2_id = 2, @limit = 50;

-- Test 2: Lay danh sach ban be
EXEC GetFriendsList @user_id = 1;

-- Test 3: Tim kiem nguoi dung
EXEC SearchUsers @search_term = N'nguyen', @current_user_id = 1;

-- Test 4: Xem thong ke
SELECT * FROM user_statistics;

-- Test 5: Xem tin nhan chua doc
SELECT * FROM unread_messages_summary WHERE receiver_id = 1;

-- =====================================================
-- HOAN THANH!
-- =====================================================
PRINT 'Database chat_app da duoc tao thanh cong!';
PRINT 'Co 5 user mau: admin, nguyenvana, tranthib, levanc, phamthid';
PRINT 'Mat khau tat ca: 123456';
GO
ALTER PROCEDURE GetChatHistory
    @user1_id INT,
    @user2_id INT,
    @limit INT = 50
AS
BEGIN
    SELECT TOP (@limit) *
    FROM messages
    WHERE ((sender_id = @user1_id AND receiver_id = @user2_id)
        OR (sender_id = @user2_id AND receiver_id = @user1_id))
        AND is_deleted = 0  -- THÊM DÒNG NÀY
    ORDER BY timestamp DESC
END
