FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --create-home --uid 10001 appuser
COPY target/task-manager-api.jar app.jar
RUN chown -R appuser:appuser /app
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
