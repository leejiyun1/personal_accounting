# ===================================
# Stage 1: Build (빌드 단계)
# ===================================
FROM gradle:8-jdk21 AS build

WORKDIR /app

# Gradle 캐싱 최적화: 의존성 먼저 다운로드
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || return 0

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle bootJar --no-daemon

# ===================================
# Stage 2: Runtime (실행 단계)
# ===================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# 보안: non-root 유저 생성
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# 빌드 단계에서 생성된 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 환경변수 기본값 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# 헬스체크 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]