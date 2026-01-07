package com.personalaccount.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LogMaskingUtil 테스트")
class LogMaskingUtilTest {

    @Test
    @DisplayName("이메일_마스킹_정상_처리")
    void maskEmail_Normal() {
        String email = "example@test.com";

        String result = LogMaskingUtil.maskEmail(email);

        assertThat(result).isEqualTo("exa***@test.com");
    }

    @Test
    @DisplayName("이메일_마스킹_짧은_로컬파트")
    void maskEmail_ShortLocalPart() {
        String email = "abc@test.com";

        String result = LogMaskingUtil.maskEmail(email);

        assertThat(result).isEqualTo("a***@test.com");
    }

    @Test
    @DisplayName("이메일_마스킹_1글자_로컬파트")
    void maskEmail_SingleCharLocalPart() {
        String email = "a@test.com";

        String result = LogMaskingUtil.maskEmail(email);

        assertThat(result).isEqualTo("a***@test.com");
    }

    @Test
    @DisplayName("이메일_마스킹_null_빈문자열_반환")
    void maskEmail_Null_ReturnsEmpty() {
        assertThat(LogMaskingUtil.maskEmail(null)).isEmpty();
    }

    @Test
    @DisplayName("이메일_마스킹_빈문자열_빈문자열_반환")
    void maskEmail_Empty_ReturnsEmpty() {
        assertThat(LogMaskingUtil.maskEmail("")).isEmpty();
    }

    @Test
    @DisplayName("이메일_마스킹_골뱅이없음_원본반환")
    void maskEmail_NoAtSign_ReturnsOriginal() {
        String invalid = "notanemail";

        String result = LogMaskingUtil.maskEmail(invalid);

        assertThat(result).isEqualTo("notanemail");
    }

    @Test
    @DisplayName("이메일_마스킹_골뱅이로_시작_원본반환")
    void maskEmail_StartsWithAtSign_ReturnsOriginal() {
        String invalid = "@test.com";

        String result = LogMaskingUtil.maskEmail(invalid);

        assertThat(result).isEqualTo("@test.com");
    }

    @Test
    @DisplayName("이름_마스킹_한글_3글자")
    void maskName_Korean3Chars() {
        String name = "홍길동";

        String result = LogMaskingUtil.maskName(name);

        assertThat(result).isEqualTo("홍*동");
    }

    @Test
    @DisplayName("이름_마스킹_영문_4글자")
    void maskName_English4Chars() {
        String name = "John";

        String result = LogMaskingUtil.maskName(name);

        assertThat(result).isEqualTo("J**n");
    }

    @Test
    @DisplayName("이름_마스킹_5글자_이상")
    void maskName_LongName() {
        String name = "Alexander";

        String result = LogMaskingUtil.maskName(name);

        assertThat(result).isEqualTo("A*******r");
    }

    @Test
    @DisplayName("이름_마스킹_2글자")
    void maskName_TwoChars() {
        String name = "홍길";

        String result = LogMaskingUtil.maskName(name);

        assertThat(result).isEqualTo("홍*");
    }

    @Test
    @DisplayName("이름_마스킹_1글자_원본반환")
    void maskName_SingleChar_ReturnsOriginal() {
        String name = "홍";

        String result = LogMaskingUtil.maskName(name);

        assertThat(result).isEqualTo("홍");
    }

    @Test
    @DisplayName("이름_마스킹_null_빈문자열_반환")
    void maskName_Null_ReturnsEmpty() {
        assertThat(LogMaskingUtil.maskName(null)).isEmpty();
    }

    @Test
    @DisplayName("이름_마스킹_빈문자열_빈문자열_반환")
    void maskName_Empty_ReturnsEmpty() {
        assertThat(LogMaskingUtil.maskName("")).isEmpty();
    }

    @Test
    @DisplayName("토큰_마스킹_정상_JWT")
    void maskToken_NormalJWT() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";

        String result = LogMaskingUtil.maskToken(token);

        assertThat(result).isEqualTo("eyJhbGciOi...");
    }

    @Test
    @DisplayName("토큰_마스킹_짧은_토큰")
    void maskToken_ShortToken() {
        String token = "short";

        String result = LogMaskingUtil.maskToken(token);

        assertThat(result).isEqualTo("***");
    }

    @Test
    @DisplayName("토큰_마스킹_정확히_10글자")
    void maskToken_Exactly10Chars() {
        String token = "1234567890";

        String result = LogMaskingUtil.maskToken(token);

        assertThat(result).isEqualTo("***");
    }

    @Test
    @DisplayName("토큰_마스킹_11글자")
    void maskToken_11Chars() {
        String token = "12345678901";

        String result = LogMaskingUtil.maskToken(token);

        assertThat(result).isEqualTo("1234567890...");
    }

    @Test
    @DisplayName("토큰_마스킹_null_빈문자열_반환")
    void maskToken_Null_ReturnsEmpty() {
        assertThat(LogMaskingUtil.maskToken(null)).isEmpty();
    }

    @Test
    @DisplayName("토큰_마스킹_빈문자열_빈문자열_반환")
    void maskToken_Empty_ReturnsEmpty() {
        assertThat(LogMaskingUtil.maskToken("")).isEmpty();
    }

    @Test
    @DisplayName("IP_마스킹_정상_IPv4")
    void maskIp_NormalIPv4() {
        String ip = "192.168.1.100";

        String result = LogMaskingUtil.maskIp(ip);

        assertThat(result).isEqualTo("192.168.***. ***");
    }

    @Test
    @DisplayName("IP_마스킹_공인IP")
    void maskIp_PublicIP() {
        String ip = "8.8.8.8";

        String result = LogMaskingUtil.maskIp(ip);

        assertThat(result).isEqualTo("8.8.***. ***");
    }

    @Test
    @DisplayName("IP_마스킹_잘못된_형식_원본반환")
    void maskIp_InvalidFormat_ReturnsOriginal() {
        String invalid = "192.168.1";

        String result = LogMaskingUtil.maskIp(invalid);

        assertThat(result).isEqualTo("192.168.1");
    }

    @Test
    @DisplayName("IP_마스킹_null_빈문자열_반환")
    void maskIp_Null_ReturnsEmpty() {
        assertThat(LogMaskingUtil.maskIp(null)).isEmpty();
    }

    @Test
    @DisplayName("IP_마스킹_빈문자열_빈문자열_반환")
    void maskIp_Empty_ReturnsEmpty() {
        assertThat(LogMaskingUtil.maskIp("")).isEmpty();
    }
}