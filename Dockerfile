FROM openjdk:11-ire

COPY build/libs/*.jar app.jar

ENTRYPOINT ["jave", "-jar", "-Dspring.profiles.active=prod", "app.jar"]