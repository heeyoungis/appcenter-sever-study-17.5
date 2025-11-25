FROM eclipse-temurin:17-jdk

LABEL version=0.1

WORKDIR /app

COPY build/libs/server-1.0-SNAPSHOT.jar /app/app.jar

CMD ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]
