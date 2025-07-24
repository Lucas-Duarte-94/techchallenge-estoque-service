# Estágio de Build
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean install

# Estágio de Produção
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=target/fiap-estoque-service-0.0.1-SNAPSHOT.jar
COPY --from=build /app/${JAR_FILE} app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]