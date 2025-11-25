
INSERT INTO user (login_id, password, email, phone_num, address, nickname, name)
VALUES
    ('test01', '1234', 'test01@example.com', '010-1111-2222', '서울시 강남구', '테스트1', '홍길동'),
    ('test02', '1234', 'test02@example.com', '010-2222-3333', '서울시 마포구', '테스트2', '이민수'),
    ('test03', '1234', 'test03@example.com', '010-3333-4444', '서울시 송파구', '테스트3', '박지나');

INSERT INTO store (min_price, store_address, store_name, store_phone)
VALUES
    (5000, '서울 강남구 테헤란로', '희영이네 분식', '02-111-2222'),
    (15000, '서울 마포구 서교동', '민수치킨', '02-222-3333'),
    (4000, '서울 송파구 방이동', '지나카페', '02-333-4444');

INSERT INTO menu (menu_name, price, store_id)
VALUES
    ('떡볶이', 4000, 1),
    ('김밥', 3000, 1),
    ('순대', 4000, 1),
    ('후라이드치킨', 16000, 2),
    ('양념치킨', 17000, 2),
    ('아메리카노', 4000, 3);