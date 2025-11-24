FROM eclipse-temurin:17-jdk

LABEL version=0.1

ARG JAR_NAME=server-1.0-SNAPSHOT.jar
ARG JAR_PATH=./build/libs/${JAR_NAME}

RUN mkdir -p /app
WORKDIR /app

COPY ${JAR_PATH} /app/app.jar

CMD ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]
