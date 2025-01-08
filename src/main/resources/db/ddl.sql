-- 사용자 테이블
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 사용자 포인트 테이블
CREATE TABLE user_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,        -- 유저id
    point_amount INT NOT NULL,      -- 포인트 잔액
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 사용자 포인트 이력 테이블
CREATE TABLE user_point_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,                        -- 유저id
    type ENUM('CHARGE', 'USE', 'EXPIRE') NOT NULL,  -- 구분
    amount INT NOT NULL,                            -- 금액
    description VARCHAR(255) NOT NULL,              -- 설명
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 상품 테이블
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,     -- 상품 이름
    price INT NOT NULL,             -- 가격
    description TEXT,               -- 설명
    category_id BIGINT,             -- 카테고리 id
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 재고 테이블
CREATE TABLE stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,         -- 상품 id
    quantity INT NOT NULL,              -- 수량
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
);

-- 카테고리 테이블
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,          -- 카테고리 이름
    description TEXT NULL,                      -- 카테고리 설명
    is_active BOOLEAN DEFAULT TRUE NOT NULL,    -- 사용 여부 (TRUE: 활성, FALSE: 비활성)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 장바구니 테이블
CREATE TABLE cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,    -- 유저 id
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 장바구니 아이템 테이블
CREATE TABLE cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,        -- 장바구니 id
    product_id BIGINT NOT NULL,     -- 상품 id
    quantity INT NOT NULL,          -- 수량
    price INT NOT NULL,             -- 가격
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 주문 테이블
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,        -- 유저 id
    total_price INT NOT NULL,       -- 금액 합계
    status ENUM('ORDERED', 'PAID', 'FAILED', 'CANCELLED') NOT NULL, -- 주문 상태
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 주문 상세 테이블
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,           -- 주문 id
    product_id BIGINT NOT NULL,         -- 상품 id
    quantity INT NOT NULL,              -- 수량
    price INT NOT NULL,                 -- 가격
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 결제 테이블
CREATE TABLE payment (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     order_id BIGINT NOT NULL,                                  -- 주문 id
     payment_method ENUM('CASH', 'CARD', 'POINTS') NOT NULL,    -- 결제 방식
     amount INT NOT NULL,                                       -- 결제 금액
     status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL,  -- 결제 상태
     transaction_id VARCHAR(255) NULL,                          -- 결제 아이디 (외부 결제 시)
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 쿠폰 테이블
CREATE TABLE coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,                      -- 쿠폰 이름
    type ENUM('FLAT', 'PERCENT') NOT NULL,           -- 쿠폰 타입: FLAT (정액), PERCENT (% 할인)
    discount_value DECIMAL(10, 2) NOT NULL,          -- 할인 값 (정액 할인 금액 또는 퍼센트 할인 비율)
    min_order_amount INT DEFAULT 0,                  -- 최소 주문 금액
    max_discount_amount INT NULL,                    -- 최대 할인 금액 (% 할인 쿠폰의 경우)
    status ENUM('ACTIVE', 'INACTIVE', 'EXHAUSTED') NOT NULL, -- 쿠폰 상태
    total_count INT NOT NULL,                        -- 발급 가능 수량
    issued_count INT NOT NULL DEFAULT 0,             -- 발급된 수량
    start_date TIMESTAMP NOT NULL,                   -- 쿠폰 유효 시작일
    expiration_date TIMESTAMP NULL,                  -- 쿠폰 유효 만료일
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 유저 쿠폰 테이블(유저에게 발급된 쿠폰)
CREATE TABLE user_coupon (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     user_id BIGINT NOT NULL,                           -- 유저 id
     coupon_id BIGINT NOT NULL,                         -- 쿠폰 id
     status ENUM('ISSUED', 'USED', 'EXPIRED', 'CANCELLED') NOT NULL,    -- 쿠폰 상태
     issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     -- 발급 날짜
     used_at TIMESTAMP NULL,                            -- 사용 날짜
     expired_at TIMESTAMP NULL,                         -- 만료 날짜
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

         CONSTRAINT uq_user_coupon UNIQUE (user_id, coupon_id)      -- 동일 유저의 동일 쿠폰 재발급 제한
);

