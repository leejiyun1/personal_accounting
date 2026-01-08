package com.personalaccount.common.util;

import java.math.BigDecimal;

public class LogMaskingUtil {

    /**
     * 이메일 마스킹
     * example@test.com → exa***@test.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 3) {
            return localPart.charAt(0) + "***" + domain;
        }

        return localPart.substring(0, 3) + "***" + domain;
    }

    /**
     * 이름 마스킹
     * 홍길동 → 홍*동
     * John → J**n
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        if (name.length() <= 1) {
            return name;
        }

        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }

        String middle = "*".repeat(name.length() - 2);
        return name.charAt(0) + middle + name.charAt(name.length() - 1);
    }

    /**
     * 토큰 마스킹 (앞 10자만 표시)
     * eyJhbGciOiJIUzI1... → eyJhbGciOi...
     */
    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }

        if (token.length() <= 10) {
            return "***";
        }

        return token.substring(0, 10) + "...";
    }

    /**
     * IP 마스킹
     * 192.168.1.100 → 192.168.***.***
     */
    public static String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "";
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }

        return parts[0] + "." + parts[1] + ".***. ***";
    }

    /**
     * 금액 마스킹
     * 500000 → 500***
     * 1000000 → 100***
     */
    public static String maskAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        String str = amount.toString();

        if (str.length() <= 3) {
            return str + "***";
        }

        return str.substring(0, 3) + "***";
    }
}