FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/backend-app-1.0.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "sleep 30 && java -jar app.jar"]  



