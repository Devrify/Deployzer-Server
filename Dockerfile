#
# Build stage
#
FROM maven:3.9.6-amazoncorretto-17-debian AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f $HOME/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=/usr/app/target/*.jar
COPY --from=build $JAR_FILE /app/deployzer-server.jar
EXPOSE 8080
ENTRYPOINT java -jar /app/deployzer-server.jar