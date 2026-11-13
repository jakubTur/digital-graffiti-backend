FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and configuration files first to optimize dependency caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Pre-download project dependencies so they are cached unless pom.xml changes
RUN ./mvnw dependency:go-offline -B

# Copy the actual application source code into the build container
COPY src src

# Package the application into an executable fat JAR (skipping unit tests for speed)
RUN ./mvnw package -DskipTests


FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy only the compiled JAR file from the build stage, avoiding heavy JDK tools
COPY --from=build /app/target/*.jar app.jar

# COPY the JSON file from the host project folder into the container's /app/data directory
COPY --from=build /app/src/main/resources/digitalgraffiti-9aec4-firebase-adminsdk-fbsvc-7aafca3741.json /app/data/digitalgraffiti-9aec4-firebase-adminsdk-fbsvc-7aafca3741.json

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
