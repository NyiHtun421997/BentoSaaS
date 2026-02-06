FROM maven:3.9.9-eclipse-temurin-23 AS builder

WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY plan-management-service/pom.xml plan-management-service/
COPY subscription-service/pom.xml subscription-service/
COPY invoice-service/pom.xml invoice-service/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/

RUN mvn dependency:go-offline -B

COPY common/src ./common/src
COPY plan-management-service/src ./plan-management-service/src

RUN mvn clean install -pl common -DskipTests
RUN mvn clean package -pl plan-management-service -am -DskipTests

FROM eclipse-temurin:23-jre AS runner

WORKDIR /app

COPY --from=builder /app/plan-management-service/target/plan-management-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 4000
EXPOSE 9000

ENTRYPOINT ["java", "-jar", "app.jar"]