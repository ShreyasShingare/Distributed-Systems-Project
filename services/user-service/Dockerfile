FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY services/amenity-service/pom.xml ./services/amenity-service/pom.xml
COPY services/booking-service/pom.xml ./services/booking-service/pom.xml
COPY services/user-service/pom.xml ./services/user-service/pom.xml
COPY services/notification-service/pom.xml ./services/notification-service/pom.xml
COPY services/ui-service/pom.xml ./services/ui-service/pom.xml
COPY services/user-service/src ./services/user-service/src
RUN mvn clean package -pl services/user-service -am -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/services/user-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

