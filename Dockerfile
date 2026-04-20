FROM openjdk:17-slim
WORKDIR /app
COPY URLModifierBackend/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
