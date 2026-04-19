FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY build/libs/*.jar recovery-back.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "recovery-back.jar"]