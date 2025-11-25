package com.personalaccount.domain.account.constants;

import com.personalaccount.domain.account.entity.AccountType;
import com.personalaccount.domain.book.entity.BookType;

public class DefaultAccounts {

    public static class AccountTemplate {
        public final String code;
        public final String name;
        public final AccountType accountType;

        public AccountTemplate(String code, String name, AccountType accountType) {
            this.code = code;
            this.name = name;
            this.accountType = accountType;
        }
    }

    public static AccountTemplate[] getDefaultAccounts(BookType bookType) {
        return bookType == BookType.PERSONAL ? PERSONAL_ACCOUNTS : BUSINESS_ACCOUNTS;
    }

    private static final AccountTemplate[] PERSONAL_ACCOUNTS = {
            new AccountTemplate("1100", "현금", AccountType.PAYMENT_METHOD),
            new AccountTemplate("1200", "은행", AccountType.PAYMENT_METHOD),
            new AccountTemplate("1300", "체크카드", AccountType.PAYMENT_METHOD),
            new AccountTemplate("1400", "신용카드", AccountType.PAYMENT_METHOD),
            new AccountTemplate("4100", "급여", AccountType.REVENUE),
            new AccountTemplate("4200", "용돈", AccountType.REVENUE),
            new AccountTemplate("4300", "부업수입", AccountType.REVENUE),
            new AccountTemplate("4400", "이자수입", AccountType.REVENUE),
            new AccountTemplate("4500", "배당수입", AccountType.REVENUE),
            new AccountTemplate("4900", "기타수입", AccountType.REVENUE),
            new AccountTemplate("5100", "식비", AccountType.EXPENSE),
            new AccountTemplate("5200", "교통비", AccountType.EXPENSE),
            new AccountTemplate("5300", "문화생활", AccountType.EXPENSE),
            new AccountTemplate("5400", "쇼핑", AccountType.EXPENSE),
            new AccountTemplate("5500", "의료비", AccountType.EXPENSE),
            new AccountTemplate("5600", "교육비", AccountType.EXPENSE),
            new AccountTemplate("5700", "통신비", AccountType.EXPENSE),
            new AccountTemplate("5800", "월세/관리비", AccountType.EXPENSE),
            new AccountTemplate("5850", "공과금", AccountType.EXPENSE),
            new AccountTemplate("5900", "보험료", AccountType.EXPENSE),
            new AccountTemplate("5950", "경조사비", AccountType.EXPENSE),
            new AccountTemplate("5999", "기타지출", AccountType.EXPENSE)
    };

    private static final AccountTemplate[] BUSINESS_ACCOUNTS = {
            new AccountTemplate("2100", "현금", AccountType.PAYMENT_METHOD),
            new AccountTemplate("2200", "사업자계좌", AccountType.PAYMENT_METHOD),
            new AccountTemplate("2300", "법인카드", AccountType.PAYMENT_METHOD),
            new AccountTemplate("6100", "매출", AccountType.REVENUE),
            new AccountTemplate("6200", "용역수입", AccountType.REVENUE),
            new AccountTemplate("6300", "수수료수입", AccountType.REVENUE),
            new AccountTemplate("6400", "이자수입", AccountType.REVENUE),
            new AccountTemplate("6900", "기타수익", AccountType.REVENUE),
            new AccountTemplate("7100", "외주비", AccountType.EXPENSE),
            new AccountTemplate("7150", "인건비", AccountType.EXPENSE),
            new AccountTemplate("7200", "재료비", AccountType.EXPENSE),
            new AccountTemplate("7250", "수도광열비", AccountType.EXPENSE),
            new AccountTemplate("7300", "임차료", AccountType.EXPENSE),
            new AccountTemplate("7350", "보험료", AccountType.EXPENSE),
            new AccountTemplate("7400", "광고선전비", AccountType.EXPENSE),
            new AccountTemplate("7500", "접대비", AccountType.EXPENSE),
            new AccountTemplate("7600", "통신비", AccountType.EXPENSE),
            new AccountTemplate("7650", "세금과공과", AccountType.EXPENSE),
            new AccountTemplate("7700", "소모품비", AccountType.EXPENSE),
            new AccountTemplate("7750", "차량유지비", AccountType.EXPENSE),
            new AccountTemplate("7800", "운반비", AccountType.EXPENSE),
            new AccountTemplate("7850", "수선비", AccountType.EXPENSE),
            new AccountTemplate("7900", "기타비용", AccountType.EXPENSE)
    };
}