FROM eclipse-temurin:17-jdk-slim
WORKDIR /app
COPY URLModifierBackend/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]