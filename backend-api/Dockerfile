# =============================================
# Stage 1: Build - Compile Java application
# =============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy POM files first for dependency caching
COPY pom.xml pom.xml
COPY notaire-shared/pom.xml notaire-shared/pom.xml
COPY backend-api/pom.xml backend-api/pom.xml
COPY frontend-swing/pom.xml frontend-swing/pom.xml

# Download dependencies (cached in Docker layer)
RUN mvn dependency:go-offline -B -q || true

# Copy source code
COPY notaire-shared/src notaire-shared/src
COPY backend-api/src backend-api/src
COPY frontend-swing/src frontend-swing/src

# Build the application (skip tests)
RUN mvn package -pl backend-api -am -DskipTests -B -q

# =============================================
# Stage 2: Runtime - Minimal JRE image
# =============================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Install wget for healthcheck
RUN apk add --no-cache wget

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1000 -S notary && \
    adduser -u 1000 -S notary -G notary

# Copy jar from builder
COPY --from=builder /build/backend-api/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R notary:notary /app

# Switch to non-root user
USER notary

# JVM flags optimized for small containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0 -Xms128m -XX:+UseG1GC -XX:G1HeapRegionSize=4m"

# Health check using Spring Boot Actuator (lighter than Swagger)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
