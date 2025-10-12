package com.personalaccount.user.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티
 *
 * BaseEntity 상속:
 * - createdAt, updatedAt 자동 관리
 *
 * 테이블명: users (user는 PostgreSQL 예약어라 복수형 사용)
 */

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;
}
