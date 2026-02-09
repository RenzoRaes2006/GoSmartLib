FROM eclipse-temurin:25

RUN mkdir /opt/app

COPY target/backend-0.0.1-SNAPSHOT.jar /opt/app

EXPOSE 8080

CMD ["java", "-jar", "/opt/app/backend-0.0.1-SNAPSHOT.jar"]


