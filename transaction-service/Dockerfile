FROM maven:3.9.0-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy everything — assuming this includes root pom, common/, and transaction-service/
COPY . .

ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository"

RUN mvn -B -f common/pom.xml clean install -DskipTests \
 && ls -l /root/.m2/repository/com/example/common/1.0-SNAPSHOT/

# Build transaction-service, which now can use the locally installed common
RUN mvn -B -f transaction-service/pom.xml clean package -DskipTests


#############################################
# Stage 2: Runtime
#############################################
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /workspace/transaction-service/target/transaction-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]