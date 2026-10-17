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
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]