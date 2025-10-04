# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
ENV JAVA_OPTS=""
ENV PORT=5000
WORKDIR /opt/app
COPY --from=build /app/target/pokedex-1.0.0.jar app.jar
EXPOSE 5000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
