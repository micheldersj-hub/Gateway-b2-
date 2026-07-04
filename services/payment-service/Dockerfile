# Build context é a raiz do monorepo (necessário para o parent pom e o módulo common).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY common common
COPY services/payment-service services/payment-service
RUN mvn -q -pl services/payment-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=build /workspace/services/payment-service/target/payment-service.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
