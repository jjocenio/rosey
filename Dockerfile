FROM openjdk:11.0.4-jre-slim

COPY build/libs/rosey-*.jar /usr/local/javaapps/rosey/rosey.jar
VOLUME /usr/local/javaapps/rosey/run
WORKDIR /usr/local/javaapps/rosey/run

ENTRYPOINT ["java", "-jar", "/usr/local/javaapps/rosey/rosey.jar"]