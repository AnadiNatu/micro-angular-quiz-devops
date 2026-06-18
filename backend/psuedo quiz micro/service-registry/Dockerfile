# ==========================
# Build Stage
# ==========================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -q

COPY src/ src/

RUN ./mvnw clean package -DskipTests -q


# ==========================
# Runtime Stage
# ==========================
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /build/target/service-registry.jar app.jar

EXPOSE 8761

USER spring

ENTRYPOINT ["java","-jar","app.jar"]