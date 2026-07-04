# Build context é a raiz do monorepo (necessário para o parent pom e o módulo common).
# Ver docker-compose.yml: build.context=. , build.dockerfile=services/kyc-service/Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY common common
COPY services/kyc-service services/kyc-service
RUN mvn -q -pl services/kyc-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /workspace/services/kyc-service/target/kyc-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
