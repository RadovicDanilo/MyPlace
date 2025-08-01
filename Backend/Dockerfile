FROM gradle:8.5-jdk17 AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

RUN gradle build --no-daemon || return 0

COPY . .

RUN gradle bootJar --no-daemon


FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

EXPOSE 8081

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/app.jar"]
