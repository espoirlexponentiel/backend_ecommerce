# ==========================================
# Multi-stage Dockerfile pour Spring Boot 3 (Java 21)
# ==========================================

# Étape 1 : Build Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copier les fichiers Maven pour mettre en cache les dépendances
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B || true

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution légère
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Créer un dossier pour les uploads
RUN mkdir -p /app/uploads

# Copier le jar compilé depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port par défaut de Render
EXPOSE 8080

# Variables JVM optimales pour conteneur cloud
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
