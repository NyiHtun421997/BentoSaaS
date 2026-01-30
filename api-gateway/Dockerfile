FROM maven:3.9.9-eclipse-temurin-23 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY plan-management-service/pom.xml plan-management-service/
COPY subscription-service/pom.xml subscription-service/
COPY invoice-service/pom.xml invoice-service/
COPY api-gateway/pom.xml api-gateway/

RUN mvn dependency:go-offline -B

COPY api-gateway/src ./api-gateway/src

RUN mvn clean package -pl api-gateway -am -DskipTests

FROM eclipse-temurin:23-jre AS runner

WORKDIR /app

COPY --from=builder /app/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar app.jar

EXPOSE 4003

ENTRYPOINT ["java", "-jar", "app.jar"]