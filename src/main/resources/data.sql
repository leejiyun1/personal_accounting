-- ============================================
-- 개인 장부 계정과목
-- ============================================

-- [1] 자산 (ASSET) - 결제수단
INSERT INTO accounts (code, name, account_type, book_type, is_active, created_at, updated_at)
VALUES
    ('1100', '현금', 'PAYMENT_METHOD', 'PERSONAL', true, NOW(), NOW()),
    ('1200', '은행', 'PAYMENT_METHOD', 'PERSONAL', true, NOW(), NOW()),
    ('1300', '체크카드', 'PAYMENT_METHOD', 'PERSONAL', true, NOW(), NOW()),
    ('1400', '신용카드', 'PAYMENT_METHOD', 'PERSONAL', true, NOW(), NOW());

-- [2] 수익 (REVENUE)
INSERT INTO accounts (code, name, account_type, book_type, is_active, created_at, updated_at)
VALUES
    ('4100', '급여', 'REVENUE', 'PERSONAL', true, NOW(), NOW()),
    ('4200', '용돈', 'REVENUE', 'PERSONAL', true, NOW(), NOW()),
    ('4300', '부업수입', 'REVENUE', 'PERSONAL', true, NOW(), NOW()),
    ('4900', '기타수입', 'REVENUE', 'PERSONAL', true, NOW(), NOW());

-- [3] 비용 (EXPENSE)
INSERT INTO accounts (code, name, account_type, book_type, is_active, created_at, updated_at)
VALUES
    ('5100', '식비', 'EXPENSE', 'PERSONAL', true, NOW(), NOW()),
    ('5200', '교통비', 'EXPENSE', 'PERSONAL', true, NOW(), NOW()),
    ('5300', '문화생활', 'EXPENSE', 'PERSONAL', true, NOW(), NOW()),
    ('5400', '쇼핑', 'EXPENSE', 'PERSONAL', true, NOW(), NOW()),
    ('5500', '의료비', 'EXPENSE', 'PERSONAL', true, NOW(), NOW()),
    ('5600', '교육비', 'EXPENSE', 'PERSONAL', true, NOW(), NOW()),
    ('5700', '통신비', 'EXPENSE', 'PERSONAL', true, NOW(), NOW()),
    ('5800', '월세/관리비', 'EXPENSE', 'PERSONAL', true, NOW(), NOW()),
    ('5900', '기타지출', 'EXPENSE', 'PERSONAL', true, NOW(), NOW());

-- ============================================
-- 사업 장부 계정과목
-- ============================================

-- [1] 자산 (ASSET) - 결제수단
INSERT INTO accounts (code, name, account_type, book_type, is_active, created_at, updated_at)
VALUES
    ('1100', '현금', 'PAYMENT_METHOD', 'BUSINESS', true, NOW(), NOW()),
    ('1200', '사업자계좌', 'PAYMENT_METHOD', 'BUSINESS', true, NOW(), NOW()),
    ('1300', '법인카드', 'PAYMENT_METHOD', 'BUSINESS', true, NOW(), NOW());

-- [2] 수익 (REVENUE)
INSERT INTO accounts (code, name, account_type, book_type, is_active, created_at, updated_at)
VALUES
    ('4100', '매출', 'REVENUE', 'BUSINESS', true, NOW(), NOW()),
    ('4200', '용역수입', 'REVENUE', 'BUSINESS', true, NOW(), NOW()),
    ('4300', '수수료수입', 'REVENUE', 'BUSINESS', true, NOW(), NOW()),
    ('4900', '기타수익', 'REVENUE', 'BUSINESS', true, NOW(), NOW());

-- [3] 비용 (EXPENSE)
INSERT INTO accounts (code, name, account_type, book_type, is_active, created_at, updated_at)
VALUES
    ('5100', '외주비', 'EXPENSE', 'BUSINESS', true, NOW(), NOW()),
    ('5200', '재료비', 'EXPENSE', 'BUSINESS', true, NOW(), NOW()),
    ('5300', '임차료', 'EXPENSE', 'BUSINESS', true, NOW(), NOW()),
    ('5400', '광고선전비', 'EXPENSE', 'BUSINESS', true, NOW(), NOW()),
    ('5500', '접대비', 'EXPENSE', 'BUSINESS', true, NOW(), NOW()),
    ('5600', '통신비', 'EXPENSE', 'BUSINESS', true, NOW(), NOW()),
    ('5700', '소모품비', 'EXPENSE', 'BUSINESS', true, NOW(), NOW()),
    ('5800', '운반비', 'EXPENSE', 'BUSINESS', true, NOW(), NOW()),
    ('5900', '기타비용', 'EXPENSE', 'BUSINESS', true, NOW(), NOW());