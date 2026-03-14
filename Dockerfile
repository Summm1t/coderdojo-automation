# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
ARG MAVEN_VERSION
WORKDIR /app
COPY . .
# Using shell-independent Maven execution if possible, but the original logic is Shell-dependent
# For multi-platform support including Windows, we'd need separate Dockerfiles or a more complex one.
# For now, let's keep it simple and see if the user's specific request for Windows can be met with standard buildx.
RUN if [ -n "$MAVEN_VERSION" ]; then ./mvnw -B versions:set -DnewVersion=$MAVEN_VERSION -DgenerateBackupPoms=false; fi && \
    ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
VOLUME /tmp
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
