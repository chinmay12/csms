FROM maven:3.9.0-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy everything to ensure both common and transaction-service are available
COPY . .

ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository"

# Install common module into local Maven repo
RUN mvn -B -f common/pom.xml clean install -DskipTests

# Build only authentication-service and its dependencies (like 'common')
RUN mvn -B -f  authentication-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jdk

# Set work directory
WORKDIR /app

# Copy the fat JAR of the authentication service
#COPY target/authentication-service-1.0-SNAPSHOT.jar authentication-service.jar

COPY --from=builder /workspace/authentication-service/target/authentication-service-1.0-SNAPSHOT.jar authentication-service.jar

EXPOSE 8082

# Run the app
ENTRYPOINT ["java", "-jar", "authentication-service.jar"]