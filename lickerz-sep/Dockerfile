FROM openjdk:17-alpine

ARG JAR_FILE=/build/libs/lickerz-sep-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} lickerz-sep.jar

EXPOSE 8000

ENTRYPOINT ["java","-jar","-Duser.timezone=Asia/Seoul","lickerz-sep.jar"]