FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/backend-app-1.0.jar app.jar

EXPOSE 8080

# I written CMD in EXEC form, so two execute two command we need to use sh -c <command1> && <command2>
CMD ["sh", "-c", "sleep 30 && java -jar app.jar"]  


# if we write CMD in shell form no need to use sh
# CMD sleep 30 && java -jar app.jar

# sleep 30 second is used beacuse, mysql database take some time to run 
# and front end run 