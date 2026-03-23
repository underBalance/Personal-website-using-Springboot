FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Imagen oficial de Playwright con dependencias
FROM mcr.microsoft.com/playwright/java:v1.41.0-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# 👇 CLAVE: instalar navegadores
RUN playwright install --with-deps

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]