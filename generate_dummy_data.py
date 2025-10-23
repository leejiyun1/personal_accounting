import requests
import random
from datetime import datetime

# 설정
BASE_URL = "http://localhost:8080/api/v1"
TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QHRlc3QuY29tIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTc2MTE3MzI4MywiZXhwIjoxNzYxMTc1MDgzfQ.sKCgGheBfjr-dfwMCYwETl9uReKiYaeTkNl_SsITgv2phg2wZ6K9QBJGHR1PeWdE"  # 로그인 후 받은 토큰

headers = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json",
    "X-User-Id": "1"  # 로그인한 사용자 ID
}

# 장부 ID (생성한 사업 장부 ID)
BOOK_ID = 2  # 실제 장부 ID로 변경

# 사업자 계정과목 ID (DB에서 확인한 실제 ID)
REVENUE_ACCOUNTS = {
    "매출": 21,
    "용역수입": 22,
}

EXPENSE_ACCOUNTS = {
    "외주비": 25,
    "임차료": 27,
    "광고선전비": 28,
    "접대비": 29,
    "통신비": 30,
    "소모품비": 31,
    "운반비": 32,
}

PAYMENT_METHODS = {
    "사업자계좌": 19,
    "법인카드": 20,
    "현금": 18,
}

def create_transaction(date, trans_type, category_id, payment_id, amount, memo):
    """거래 생성"""
    data = {
        "bookId": BOOK_ID,
        "date": date,
        "type": trans_type,
        "categoryId": category_id,
        "paymentMethodId": payment_id,
        "amount": amount,
        "memo": memo
    }

    response = requests.post(f"{BASE_URL}/transactions", json=data, headers=headers)

    if response.status_code == 201:
        print(f"✅ {date} - {memo}: {amount:,}원")
        return True
    else:
        print(f"❌ 실패: {response.status_code} - {response.text}")
        return False

# 2025년 1월~10월 실제 사업자 같은 데이터
print("=" * 60)
print("개인사업자 프리랜서 개발자 더미 데이터 생성 시작")
print("=" * 60)

success_count = 0
fail_count = 0

for month in range(1, 11):  # 1월~10월
    print(f"\n📅 {month}월 데이터 생성 중...")

    # === 수입 ===
    # 1. 주요 매출 (월 2~4건, 프로젝트 단위)
    num_projects = random.randint(2, 4)
    for i in range(num_projects):
        day = random.randint(5, 28)
        date = f"2025-{month:02d}-{day:02d}"
        amount = random.choice([3000000, 5000000, 8000000, 10000000])

        if create_transaction(date, "INCOME", REVENUE_ACCOUNTS["매출"],
                             PAYMENT_METHODS["사업자계좌"], amount,
                             f"웹개발 프로젝트 {i+1}차 대금"):
            success_count += 1
        else:
            fail_count += 1

    # 2. 용역수입 (컨설팅, 유지보수)
    if random.random() > 0.3:
        day = random.randint(10, 25)
        date = f"2025-{month:02d}-{day:02d}"
        amount = random.randint(500000, 2000000)

        if create_transaction(date, "INCOME", REVENUE_ACCOUNTS["용역수입"],
                             PAYMENT_METHODS["사업자계좌"], amount,
                             "기술 컨설팅 수입"):
            success_count += 1
        else:
            fail_count += 1

    # === 지출 ===
    # 3. 외주비 (월 1~2회)
    if random.random() > 0.4:
        day = random.randint(5, 20)
        date = f"2025-{month:02d}-{day:02d}"
        amount = random.randint(1000000, 3000000)

        if create_transaction(date, "EXPENSE", EXPENSE_ACCOUNTS["외주비"],
                             PAYMENT_METHODS["사업자계좌"], amount,
                             "디자이너 외주 비용"):
            success_count += 1
        else:
            fail_count += 1

    # 4. 임차료 (매월 고정)
    date = f"2025-{month:02d}-01"
    if create_transaction(date, "EXPENSE", EXPENSE_ACCOUNTS["임차료"],
                         PAYMENT_METHODS["사업자계좌"], 800000,
                         "사무실 월세"):
        success_count += 1
    else:
        fail_count += 1

    # 5. 광고선전비 (월 1~2회)
    num_ads = random.randint(1, 2)
    for _ in range(num_ads):
        day = random.randint(5, 25)
        date = f"2025-{month:02d}-{day:02d}"
        amount = random.randint(300000, 1000000)
        platform = random.choice(["구글 광고", "네이버 광고", "인스타그램 광고"])

        if create_transaction(date, "EXPENSE", EXPENSE_ACCOUNTS["광고선전비"],
                             PAYMENT_METHODS["법인카드"], amount,
                             f"{platform} 집행"):
            success_count += 1
        else:
            fail_count += 1

    # 6. 접대비 (월 2~4회)
    num_entertainment = random.randint(2, 4)
    for _ in range(num_entertainment):
        day = random.randint(1, 28)
        date = f"2025-{month:02d}-{day:02d}"
        amount = random.randint(50000, 300000)

        if create_transaction(date, "EXPENSE", EXPENSE_ACCOUNTS["접대비"],
                             PAYMENT_METHODS["법인카드"], amount,
                             "클라이언트 미팅 식대"):
            success_count += 1
        else:
            fail_count += 1

    # 7. 통신비 (매월 고정)
    date = f"2025-{month:02d}-05"
    if create_transaction(date, "EXPENSE", EXPENSE_ACCOUNTS["통신비"],
                         PAYMENT_METHODS["법인카드"], 150000,
                         "인터넷/전화 요금"):
        success_count += 1
    else:
        fail_count += 1

    # 8. 소모품비 (월 3~5회)
    num_supplies = random.randint(3, 5)
    for _ in range(num_supplies):
        day = random.randint(1, 28)
        date = f"2025-{month:02d}-{day:02d}"
        amount = random.randint(20000, 150000)
        item = random.choice(["사무용품", "노트북 액세서리", "책/교재", "소프트웨어 구독"])

        if create_transaction(date, "EXPENSE", EXPENSE_ACCOUNTS["소모품비"],
                             PAYMENT_METHODS["법인카드"], amount,
                             f"{item} 구매"):
            success_count += 1
        else:
            fail_count += 1

    # 9. 운반비 (월 1~2회)
    if random.random() > 0.3:
        day = random.randint(1, 28)
        date = f"2025-{month:02d}-{day:02d}"
        amount = random.randint(30000, 100000)

        if create_transaction(date, "EXPENSE", EXPENSE_ACCOUNTS["운반비"],
                             PAYMENT_METHODS["법인카드"], amount,
                             "택배/배송 비용"):
            success_count += 1
        else:
            fail_count += 1

print("\n" + "=" * 60)
print(f"✅ 생성 완료: {success_count}건")
print(f"❌ 실패: {fail_count}건")
print("=" * 60)