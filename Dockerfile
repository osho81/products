## Set the base image to use
#FROM openjdk:17
#
## Add a volume pointing to /tmp (optional)
#VOLUME /tmp
#
## Set the working directory (optional)
#WORKDIR /app
#
## Copy the JAR file into the container at /app:
#
## Local approach:
#COPY target/mongoflux-0.0.1-SNAPSHOT.jar app.jar
#
## Url approach (e.g. docker build gitlab-url):
##COPY . app/mongoflux-0.0.1-SNAPSHOT.jar
#
## Expose the port that the application will run on
#EXPOSE 8080
#
## Run the application
#CMD ["java", "-jar", "app.jar"]


##########################################
####### Built on Joachims example ########
# Build first
FROM maven:3.8.1-openjdk-17-slim AS build
# Copy from project src & pom to "virtual" folder:
COPY src /home/app/src
COPY pom.xml /home/app
# Run mvn command, to create jar file
RUN mvn -f /home/app/pom.xml --batch-mode --errors --fail-at-end -DskipTests clean package

### After build, use produced jar to run container##
FROM openjdk:17-jdk-slim
# Use wildcard to use the produced jar; copy to determined path
COPY --from=build /home/app/target/*.jar /home/app/mongoflux.jar
ENTRYPOINT ["java", "-jar", "/home/app/mongoflux.jar"]
