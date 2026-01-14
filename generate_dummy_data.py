import requests
import random
from datetime import datetime, timedelta

# === 설정 ===
BASE_URL = "http://localhost:8080/api/v1"
TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QHRlc3QuY29tIiwidHlwZSI6ImFjY2VzcyIsImlhdCI6MTc2ODM1ODIzNCwiZXhwIjoxNzY4MzYwMDM0fQ.djYjAGXXEuQm1zg7LBDt38AWfv2NqqTOzduhJ_MfEcpsubPNc2B9QUvD1tYbPFn7uQ_0YLew0M-eUqoTk80acg"

headers = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json"
}

# === 장부 ID ===
PERSONAL_BOOK_ID = 1
BUSINESS_BOOK_ID = 2

# === 개인 장부 계정과목 ID ===
PERSONAL_REVENUE = {
    "급여": 5,
    "용돈": 6,
    "부업수입": 7,
    "이자수입": 8,
    "배당수입": 9,
    "기타수입": 10,
}

PERSONAL_EXPENSE = {
    "식비": 11,
    "교통비": 12,
    "문화생활": 13,
    "쇼핑": 14,
    "의료비": 15,
    "교육비": 16,
    "통신비": 17,
    "월세/관리비": 18,
    "공과금": 19,
    "보험료": 20,
    "경조사비": 21,
    "기타지출": 22,
}

PERSONAL_PAYMENT = {
    "현금": 1,
    "은행": 2,
    "체크카드": 3,
    "신용카드": 4,
}

# === 사업자 장부 계정과목 ID ===
BUSINESS_REVENUE = {
    "매출": 26,
    "용역수입": 27,
    "수수료수입": 28,
    "이자수입": 29,
    "기타수익": 30,
}

BUSINESS_EXPENSE = {
    "외주비": 31,
    "인건비": 32,
    "재료비": 33,
    "수도광열비": 34,
    "임차료": 35,
    "보험료": 36,
    "광고선전비": 37,
    "접대비": 38,
    "통신비": 39,
    "세금과공과": 40,
    "소모품비": 41,
    "차량유지비": 42,
    "운반비": 43,
    "수선비": 44,
    "기타비용": 45,
}

BUSINESS_PAYMENT = {
    "현금": 23,
    "사업자계좌": 24,
    "법인카드": 25,
}


def create_transaction(book_id, date, trans_type, category_id, payment_id, amount, memo):
    """거래 생성"""
    data = {
        "bookId": book_id,
        "date": date,
        "type": trans_type,
        "categoryId": category_id,
        "paymentMethodId": payment_id,
        "amount": amount,
        "memo": memo
    }

    response = requests.post(f"{BASE_URL}/transactions", json=data, headers=headers)

    if response.status_code == 201:
        print(f"  ✅ {date} | {memo}: {amount:,}원")
        return True
    else:
        print(f"  ❌ 실패: {response.status_code} - {response.text[:100]}")
        return False


def generate_personal_data():
    """개인 장부 더미데이터 생성 - 직장인 시나리오"""
    print("\n" + "=" * 70)
    print("👤 개인 장부 더미데이터 생성 (직장인 시나리오)")
    print("=" * 70)
    
    success, fail = 0, 0
    
    for month in range(1, 13):
        print(f"\n📅 2025년 {month}월")
        
        # === 수입 ===
        
        # 1. 급여 (매월 25일, 세후 350만원 기준)
        date = f"2025-{month:02d}-25"
        base_salary = 3500000
        if create_transaction(PERSONAL_BOOK_ID, date, "INCOME", 
                            PERSONAL_REVENUE["급여"], PERSONAL_PAYMENT["은행"],
                            base_salary, "월급"):
            success += 1
        else:
            fail += 1
        
        # 2. 용돈 (명절: 1월, 9월)
        if month in [1, 9]:
            date = f"2025-{month:02d}-{random.randint(1, 5):02d}"
            amount = random.choice([100000, 200000, 300000])
            if create_transaction(PERSONAL_BOOK_ID, date, "INCOME",
                                PERSONAL_REVENUE["용돈"], PERSONAL_PAYMENT["현금"],
                                amount, "명절 용돈"):
                success += 1
            else:
                fail += 1
        
        # 3. 이자수입 (분기별: 3, 6, 9, 12월)
        if month in [3, 6, 9, 12]:
            date = f"2025-{month:02d}-{random.randint(1, 5):02d}"
            amount = random.randint(5000, 15000)
            if create_transaction(PERSONAL_BOOK_ID, date, "INCOME",
                                PERSONAL_REVENUE["이자수입"], PERSONAL_PAYMENT["은행"],
                                amount, "예금 이자"):
                success += 1
            else:
                fail += 1
        
        # 4. 부업수입 (가끔, 40% 확률)
        if random.random() > 0.6:
            date = f"2025-{month:02d}-{random.randint(10, 28):02d}"
            amount = random.choice([200000, 300000, 500000])
            memo = random.choice(["블로그 원고료", "번역 아르바이트", "온라인 강의 수입"])
            if create_transaction(PERSONAL_BOOK_ID, date, "INCOME",
                                PERSONAL_REVENUE["부업수입"], PERSONAL_PAYMENT["은행"],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # === 지출 ===
        
        # 5. 월세/관리비 (매월 1일)
        date = f"2025-{month:02d}-01"
        if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                            PERSONAL_EXPENSE["월세/관리비"], PERSONAL_PAYMENT["은행"],
                            750000, "월세"):
            success += 1
        else:
            fail += 1
        
        date = f"2025-{month:02d}-05"
        amount = random.randint(80000, 120000)
        if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                            PERSONAL_EXPENSE["월세/관리비"], PERSONAL_PAYMENT["은행"],
                            amount, "관리비"):
            success += 1
        else:
            fail += 1
        
        # 6. 공과금 (매월)
        date = f"2025-{month:02d}-{random.randint(15, 20):02d}"
        elec = random.randint(30000, 80000) if month in [7, 8, 1, 2] else random.randint(20000, 40000)
        if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                            PERSONAL_EXPENSE["공과금"], PERSONAL_PAYMENT["은행"],
                            elec, "전기요금"):
            success += 1
        else:
            fail += 1
        
        date = f"2025-{month:02d}-{random.randint(15, 20):02d}"
        gas = random.randint(30000, 80000) if month in [11, 12, 1, 2] else random.randint(5000, 15000)
        if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                            PERSONAL_EXPENSE["공과금"], PERSONAL_PAYMENT["은행"],
                            gas, "가스요금"):
            success += 1
        else:
            fail += 1
        
        # 7. 통신비 (매월)
        date = f"2025-{month:02d}-{random.randint(10, 15):02d}"
        if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                            PERSONAL_EXPENSE["통신비"], PERSONAL_PAYMENT["체크카드"],
                            69000, "휴대폰 요금"):
            success += 1
        else:
            fail += 1
        
        date = f"2025-{month:02d}-{random.randint(10, 15):02d}"
        if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                            PERSONAL_EXPENSE["통신비"], PERSONAL_PAYMENT["체크카드"],
                            25000, "인터넷 요금"):
            success += 1
        else:
            fail += 1
        
        # 8. 보험료 (매월)
        date = f"2025-{month:02d}-{random.randint(20, 25):02d}"
        if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                            PERSONAL_EXPENSE["보험료"], PERSONAL_PAYMENT["은행"],
                            150000, "실비보험"):
            success += 1
        else:
            fail += 1
        
        # 9. 식비 (주 평균 4~5회 외식/배달)
        num_meals = random.randint(15, 25)
        for _ in range(num_meals):
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            
            meal_type = random.choice(["점심", "저녁", "배달"])
            if meal_type == "점심":
                amount = random.choice([8000, 9000, 10000, 11000, 12000])
                memo = random.choice(["점심 식사", "회사 근처 식당", "점심 백반"])
            elif meal_type == "저녁":
                amount = random.choice([15000, 20000, 25000, 30000])
                memo = random.choice(["저녁 식사", "회식", "친구 만남"])
            else:
                amount = random.choice([15000, 18000, 22000, 25000])
                memo = random.choice(["배달음식", "야식", "주말 배달"])
            
            payment = random.choice(["체크카드", "신용카드"])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["식비"], PERSONAL_PAYMENT[payment],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # 10. 마트/편의점 (월 4~8회)
        num_grocery = random.randint(4, 8)
        for _ in range(num_grocery):
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.randint(20000, 80000)
            memo = random.choice(["마트 장보기", "편의점", "생필품 구매"])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["식비"], PERSONAL_PAYMENT["체크카드"],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # 11. 카페 (월 8~15회)
        num_cafe = random.randint(8, 15)
        for _ in range(num_cafe):
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.choice([4500, 5000, 5500, 6000, 6500])
            memo = random.choice(["커피", "스타벅스", "카페", "아메리카노"])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["식비"], PERSONAL_PAYMENT["체크카드"],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # 12. 교통비 (출퇴근 + 가끔 택시)
        # 대중교통 (월 40~50회)
        num_transit = random.randint(40, 50)
        transit_total = num_transit * random.randint(1400, 1600)
        date = f"2025-{month:02d}-28"
        if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                            PERSONAL_EXPENSE["교통비"], PERSONAL_PAYMENT["체크카드"],
                            transit_total, "교통카드 충전"):
            success += 1
        else:
            fail += 1
        
        # 택시 (월 2~4회)
        num_taxi = random.randint(2, 4)
        for _ in range(num_taxi):
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.choice([8000, 12000, 15000, 20000])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["교통비"], PERSONAL_PAYMENT["신용카드"],
                                amount, "택시"):
                success += 1
            else:
                fail += 1
        
        # 13. 문화생활 (월 2~4회)
        num_culture = random.randint(2, 4)
        for _ in range(num_culture):
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            activity = random.choice([
                ("영화 관람", random.choice([14000, 15000, 16000])),
                ("넷플릭스", 17000),
                ("유튜브 프리미엄", 14900),
                ("공연 관람", random.randint(50000, 100000)),
                ("전시회", random.randint(15000, 25000)),
                ("독서 (책 구매)", random.randint(15000, 25000)),
            ])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["문화생활"], PERSONAL_PAYMENT["신용카드"],
                                activity[1], activity[0]):
                success += 1
            else:
                fail += 1
        
        # 14. 쇼핑 (월 1~3회)
        num_shopping = random.randint(1, 3)
        for _ in range(num_shopping):
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            item = random.choice([
                ("옷 구매", random.randint(30000, 150000)),
                ("신발", random.randint(50000, 120000)),
                ("전자기기", random.randint(30000, 200000)),
                ("생활용품", random.randint(20000, 50000)),
                ("온라인 쇼핑", random.randint(20000, 80000)),
            ])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["쇼핑"], PERSONAL_PAYMENT["신용카드"],
                                item[1], item[0]):
                success += 1
            else:
                fail += 1
        
        # 15. 의료비 (가끔)
        if random.random() > 0.7:
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            medical = random.choice([
                ("병원 진료", random.randint(10000, 30000)),
                ("약국", random.randint(5000, 20000)),
                ("치과", random.randint(30000, 100000)),
                ("안과", random.randint(20000, 50000)),
            ])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["의료비"], PERSONAL_PAYMENT["체크카드"],
                                medical[1], medical[0]):
                success += 1
            else:
                fail += 1
        
        # 16. 교육비 (가끔)
        if random.random() > 0.7:
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            edu = random.choice([
                ("온라인 강의", random.randint(30000, 100000)),
                ("자격증 시험", random.randint(30000, 80000)),
                ("세미나 참가", random.randint(20000, 50000)),
            ])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["교육비"], PERSONAL_PAYMENT["신용카드"],
                                edu[1], edu[0]):
                success += 1
            else:
                fail += 1
        
        # 17. 경조사비 (가끔)
        if random.random() > 0.7:
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            event = random.choice([
                ("결혼식 축의금", random.choice([50000, 100000])),
                ("장례식 조의금", random.choice([50000, 100000])),
                ("돌잔치", random.choice([30000, 50000])),
                ("생일 선물", random.randint(30000, 80000)),
            ])
            if create_transaction(PERSONAL_BOOK_ID, date, "EXPENSE",
                                PERSONAL_EXPENSE["경조사비"], PERSONAL_PAYMENT["현금"],
                                event[1], event[0]):
                success += 1
            else:
                fail += 1
    
    return success, fail


def generate_business_data():
    """사업자 장부 더미데이터 생성 - 프리랜서 개발자 시나리오"""
    print("\n" + "=" * 70)
    print("💼 사업자 장부 더미데이터 생성 (프리랜서 개발자 시나리오)")
    print("=" * 70)
    
    success, fail = 0, 0
    
    # 프로젝트 목록 (연간 시나리오)
    projects = [
        {"name": "A사 웹사이트 리뉴얼", "months": [1, 2, 3], "monthly_amount": 5000000},
        {"name": "B사 관리자 시스템", "months": [2, 3, 4, 5], "monthly_amount": 4000000},
        {"name": "C사 모바일 앱", "months": [4, 5, 6], "monthly_amount": 6000000},
        {"name": "D사 API 개발", "months": [6, 7], "monthly_amount": 4500000},
        {"name": "E사 쇼핑몰", "months": [7, 8, 9, 10], "monthly_amount": 5500000},
        {"name": "F사 대시보드", "months": [9, 10, 11], "monthly_amount": 5000000},
        {"name": "G사 유지보수", "months": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], "monthly_amount": 500000},
    ]
    
    for month in range(1, 13):
        print(f"\n📅 2025년 {month}월")
        
        # === 수입 ===
        
        # 1. 프로젝트 매출
        for project in projects:
            if month in project["months"]:
                day = random.randint(10, 25)
                date = f"2025-{month:02d}-{day:02d}"
                if create_transaction(BUSINESS_BOOK_ID, date, "INCOME",
                                    BUSINESS_REVENUE["매출"], BUSINESS_PAYMENT["사업자계좌"],
                                    project["monthly_amount"], f"{project['name']} {month}월분"):
                    success += 1
                else:
                    fail += 1
        
        # 2. 용역수입 (기술 컨설팅, 50% 확률)
        if random.random() > 0.5:
            day = random.randint(5, 25)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.choice([500000, 800000, 1000000, 1500000])
            memo = random.choice(["기술 컨설팅", "코드 리뷰", "아키텍처 자문", "기술 세미나"])
            if create_transaction(BUSINESS_BOOK_ID, date, "INCOME",
                                BUSINESS_REVENUE["용역수입"], BUSINESS_PAYMENT["사업자계좌"],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # 3. 이자수입 (분기별)
        if month in [3, 6, 9, 12]:
            date = f"2025-{month:02d}-{random.randint(1, 5):02d}"
            amount = random.randint(10000, 30000)
            if create_transaction(BUSINESS_BOOK_ID, date, "INCOME",
                                BUSINESS_REVENUE["이자수입"], BUSINESS_PAYMENT["사업자계좌"],
                                amount, "사업자 통장 이자"):
                success += 1
            else:
                fail += 1
        
        # === 지출 ===
        
        # 4. 임차료 (매월 1일, 코워킹 스페이스)
        date = f"2025-{month:02d}-01"
        if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                            BUSINESS_EXPENSE["임차료"], BUSINESS_PAYMENT["사업자계좌"],
                            550000, "코워킹스페이스 월세"):
            success += 1
        else:
            fail += 1
        
        # 5. 통신비 (매월)
        date = f"2025-{month:02d}-05"
        if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                            BUSINESS_EXPENSE["통신비"], BUSINESS_PAYMENT["법인카드"],
                            89000, "업무용 휴대폰"):
            success += 1
        else:
            fail += 1
        
        date = f"2025-{month:02d}-10"
        if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                            BUSINESS_EXPENSE["통신비"], BUSINESS_PAYMENT["법인카드"],
                            55000, "인터넷/서버 비용"):
            success += 1
        else:
            fail += 1
        
        # 6. 외주비 (프로젝트에 따라)
        if random.random() > 0.4:
            day = random.randint(5, 25)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.choice([500000, 800000, 1000000, 1500000, 2000000])
            memo = random.choice(["디자이너 외주", "퍼블리셔 외주", "백엔드 외주", "QA 외주"])
            if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                                BUSINESS_EXPENSE["외주비"], BUSINESS_PAYMENT["사업자계좌"],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # 7. 소모품비 (월 3~6회)
        supplies = [
            ("AWS 비용", random.randint(50000, 150000)),
            ("개발툴 구독 (JetBrains)", 25000),
            ("GitHub Pro", 4000),
            ("Notion 구독", 10000),
            ("Figma 구독", 15000),
            ("도메인 갱신", random.randint(10000, 30000)),
            ("기술 서적", random.randint(25000, 45000)),
            ("사무용품", random.randint(10000, 30000)),
            ("노트북 액세서리", random.randint(20000, 80000)),
        ]
        
        num_supplies = random.randint(3, 6)
        selected = random.sample(supplies, num_supplies)
        for item_name, amount in selected:
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                                BUSINESS_EXPENSE["소모품비"], BUSINESS_PAYMENT["법인카드"],
                                amount, item_name):
                success += 1
            else:
                fail += 1
        
        # 8. 접대비 (클라이언트 미팅, 월 3~6회)
        num_meetings = random.randint(3, 6)
        for _ in range(num_meetings):
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.randint(30000, 150000)
            memo = random.choice([
                "클라이언트 미팅 식대",
                "프로젝트 킥오프 미팅",
                "중간보고 미팅",
                "네트워킹 모임",
                "개발자 커뮤니티 모임",
            ])
            if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                                BUSINESS_EXPENSE["접대비"], BUSINESS_PAYMENT["법인카드"],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # 9. 광고선전비 (가끔)
        if random.random() > 0.6:
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.randint(100000, 500000)
            memo = random.choice(["포트폴리오 사이트 광고", "프리랜서 플랫폼 광고", "LinkedIn 프리미엄"])
            if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                                BUSINESS_EXPENSE["광고선전비"], BUSINESS_PAYMENT["법인카드"],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # 10. 교통비/운반비 (월 2~4회)
        num_transport = random.randint(2, 4)
        for _ in range(num_transport):
            day = random.randint(1, 28)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.randint(10000, 40000)
            memo = random.choice(["클라이언트 방문 택시비", "미팅 교통비", "출장 교통비"])
            if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                                BUSINESS_EXPENSE["운반비"], BUSINESS_PAYMENT["법인카드"],
                                amount, memo):
                success += 1
            else:
                fail += 1
        
        # 11. 세금과공과 (분기별 부가세 등)
        if month in [1, 4, 7, 10]:
            day = random.randint(20, 25)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.randint(500000, 1500000)
            if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                                BUSINESS_EXPENSE["세금과공과"], BUSINESS_PAYMENT["사업자계좌"],
                                amount, "부가가치세 납부"):
                success += 1
            else:
                fail += 1
        
        # 12. 보험료 (매월, 사업자 보험)
        date = f"2025-{month:02d}-15"
        if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                            BUSINESS_EXPENSE["보험료"], BUSINESS_PAYMENT["사업자계좌"],
                            120000, "사업자 상해보험"):
            success += 1
        else:
            fail += 1
        
        # 13. 수도광열비 (코워킹스페이스라 별도 없지만, 여름/겨울 추가)
        if month in [7, 8, 1, 2]:
            day = random.randint(10, 20)
            date = f"2025-{month:02d}-{day:02d}"
            amount = random.randint(30000, 80000)
            memo = "에어컨/난방 추가요금" if month in [7, 8] else "난방 추가요금"
            if create_transaction(BUSINESS_BOOK_ID, date, "EXPENSE",
                                BUSINESS_EXPENSE["수도광열비"], BUSINESS_PAYMENT["법인카드"],
                                amount, memo):
                success += 1
            else:
                fail += 1
    
    return success, fail


if __name__ == "__main__":
    print("=" * 70)
    print("🚀 더미데이터 생성 시작 (2025년 1월 ~ 12월)")
    print("=" * 70)
    
    # 개인 장부
    personal_success, personal_fail = generate_personal_data()
    
    # 사업자 장부
    business_success, business_fail = generate_business_data()
    
    # 결과 출력
    print("\n" + "=" * 70)
    print("📊 최종 결과")
    print("=" * 70)
    print(f"👤 개인 장부: 성공 {personal_success}건 / 실패 {personal_fail}건")
    print(f"💼 사업자 장부: 성공 {business_success}건 / 실패 {business_fail}건")
    print(f"📈 총합: 성공 {personal_success + business_success}건 / 실패 {personal_fail + business_fail}건")
    print("=" * 70)
