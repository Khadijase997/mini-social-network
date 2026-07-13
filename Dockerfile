# Étape 1 : build avec Gradle
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# Étape 2 : image finale légère
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/build/libs/Social-Network-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]