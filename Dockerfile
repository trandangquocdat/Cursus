 FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY .env .env
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]