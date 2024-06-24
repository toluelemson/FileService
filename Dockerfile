FROM maven:3.9.8-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

RUN ls -l /app/target

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/*.jar /app/target/

RUN ls -l /app/target

EXPOSE 8080

CMD ["java", "-jar", "/app/target/FileService-0.0.1-SNAPSHOT.jar"]
