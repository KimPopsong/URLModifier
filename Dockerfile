FROM eclipse-temurin:17-jdk-slim
WORKDIR /app
COPY URLModifierBackend/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
