CREATE schema  if not exists roasthub;
USE roasthub;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

INSERT INTO users (username, password) VALUES-- 请用密码的实际哈希代替 'password1hash'
    ('admin', '210cf7aa5e2682c9c9d4511f88fe2789');