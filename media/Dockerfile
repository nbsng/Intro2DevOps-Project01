FROM eclipse-temurin:25-jre-alpine

COPY media/target/media*.jar /app.jar

RUN mkdir -p /images
COPY sampledata/images/ /images/

ENTRYPOINT ["java","-jar","/app.jar"]