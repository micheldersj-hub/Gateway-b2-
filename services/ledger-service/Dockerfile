# Build context é a raiz do monorepo (necessário para o parent pom e o módulo common).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY common common
COPY services/ledger-service services/ledger-service
RUN mvn -q -pl services/ledger-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /workspace/services/ledger-service/target/ledger-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
