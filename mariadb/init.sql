-- 테이블 자동 생성 및 더미 데이터 삽입
-- 예시
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

INSERT INTO users (username, email) VALUES (
'test_user', 'test@test.com'
);
