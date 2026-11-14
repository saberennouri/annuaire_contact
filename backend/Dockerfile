# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---- run ----
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 --no-create-home app
WORKDIR /app
COPY --from=build /workspace/target/annuaire-1.0.0.jar app.jar
USER app
EXPOSE 8080
# Réglé pour les petites instances gratuites (512 Mo de RAM)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
