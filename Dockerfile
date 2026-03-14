# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
ARG MAVEN_VERSION
WORKDIR /app
COPY . .
RUN if [ -n "$MAVEN_VERSION" ]; then ./mvnw -B versions:set -DnewVersion=$MAVEN_VERSION -DgenerateBackupPoms=false; fi && \
    ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
