# --------- Stage 1: Build ---------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn dependency:go-offline -B

RUN mvn clean package -DskipTests

# --------- Stage 2: Run ---------
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]