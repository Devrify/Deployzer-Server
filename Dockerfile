#
# Build stage
#
FROM maven:3.9.6-amazoncorretto-17-debian AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app

RUN mvn -f /usr/src/app/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=/usr/app/target/*.jar
COPY --from=build $JAR_FILE /app/app.jar
EXPOSE 8080
ENTRYPOINT java -jar /app/app*.jar