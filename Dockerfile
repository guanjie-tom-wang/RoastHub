FROM openjdk:8-jdk-alpine
COPY target/foodlover-0.0.1-SNAPSHOT.jar app.jar
RUN ls -la /
EXPOSE 8888
ENTRYPOINT ["java","-jar","/app.jar", "--server.port=8888"]
