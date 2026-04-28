FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine
ENV SPRING_PROFILES_ACTIVE=prod

WORKDIR /app
RUN addgroup -S spring && adduser -S -G spring spring
COPY --from=build /workspace/target/*.jar /app/app.jar

USER spring
EXPOSE 8282
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar /app/app.jar"]
