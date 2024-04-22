FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV JAVA_OPTS=""
ADD target/QrCodeGenerator.jar App.jar
EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar App.jar