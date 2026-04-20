FROM eclipse-temurin:17
WORKDIR /app
COPY URLModifierBackend/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
