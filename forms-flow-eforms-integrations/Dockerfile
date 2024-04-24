# Build Stage
FROM maven
WORKDIR /usr/src/app
#COPY pom.xml .
#RUN mvn -B -e -C -T 1C org.apache.maven.plugins:maven-dependency-plugin:3.1.1:go-offline
COPY . .
RUN mvn -Dmaven.test.skip=true -P prod clean package
#RUN mvn -B -e -o -T 1C verify

# Package stage
FROM openjdk:17

ARG JAR_FILE=target/*.jar
RUN mkdir /cert/
COPY --from=0 /usr/src/app/src/main/resources/cert /cert/
COPY --from=0 /usr/src/app/target/*.jar ./eforms-integrations.jar

EXPOSE 8002

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","./eforms-integrations.jar"]
