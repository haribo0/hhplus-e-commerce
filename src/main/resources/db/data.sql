
-- 유저
INSERT INTO users (id, username, email, password, points, created_at) VALUES
    (1, 'test_user', 'test_user@example.com', 'password123', 100000, NOW())
;

-- 유저 포인트
INSERT INTO user_points (id, user_id, points, updated_at) VALUES
    (1, 1, 100000, NOW())
;

-- 포인트 내역
INSERT INTO point_recharge_history (id, user_id, amount, recharge_date) VALUES
    (1, 1, 50000, NOW()),
    (2, 1, 50000, NOW())
;

-- 카테고리
INSERT INTO categories (id, name, created_at) VALUES
    (1, '전자제품', NOW()),
    (2, '가구', NOW()),
    (3, '패션', NOW())
;

-- 상품
INSERT INTO products (id, name, description, price, stock, category_id, created_at) VALUES
    -- 전자제품
    (1, '스마트폰', '최신형 스마트폰. 뛰어난 성능과 카메라 품질.', 1200000, 30, 1, NOW()),
    (2, '노트북', '고사양 게임 및 작업용 노트북.', 1500000, 20, 1, NOW()),
    (3, '4K TV', '초고화질 4K 스마트 TV.', 800000, 15, 1, NOW()),
    -- 가구
    (4, '책상', '깔끔한 디자인의 컴퓨터 책상.', 150000, 50, 2, NOW()),
    (5, '의자', '편안한 인체공학 의자.', 200000, 40, 2, NOW()),
    (6, '소파', '고급스러운 디자인의 소파.', 500000, 10, 2, NOW()),
    -- 패션
    (7, '셔츠', '심플하고 세련된 셔츠.', 50000, 100, 3, NOW()),
    (8, '청바지', '트렌디한 스타일의 청바지.', 70000, 80, 3, NOW()),
    (9, '재킷', '스타일리시한 겨울 재킷.', 120000, 30, 3, NOW())
;
