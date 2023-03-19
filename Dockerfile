FROM gradle:7.1.0-jdk11 as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon 

FROM openjdk:11
EXPOSE 8051
RUN mkdir /app
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-jar", "/app/spring-boot-application.jar"]