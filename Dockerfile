# ── 1단계: Gradle 빌드 ───────────────────────────────────────
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# 의존성 캐시 레이어 (소스 변경 시 재다운로드 방지)
COPY URLModifierBackend/gradlew       ./gradlew
COPY URLModifierBackend/gradle        ./gradle
COPY URLModifierBackend/build.gradle  ./build.gradle
COPY URLModifierBackend/settings.gradle ./settings.gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q || true

COPY URLModifierBackend/src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ── 2단계: 런타임 이미지 ──────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
