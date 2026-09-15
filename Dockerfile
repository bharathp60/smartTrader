# ── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Copy wrapper first for layer caching
COPY gradle           gradle
COPY gradlew          gradlew
COPY settings.gradle  settings.gradle
COPY build.gradle     build.gradle
COPY gradle.properties gradle.properties
COPY src              src

RUN chmod +x gradlew && ./gradlew --no-daemon -q clean bootJar

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN addgroup --system smarttrader \
 && adduser  --system --ingroup smarttrader smarttrader

COPY --from=build /workspace/build/libs/smart-trader-*.jar /app/smart-trader.jar

USER smarttrader
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/smart-trader.jar"]
