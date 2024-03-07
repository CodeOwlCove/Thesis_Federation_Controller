# Start with a base image containing Java runtime
FROM openjdk:17-jdk

# Add Maintainer Info
LABEL maintainer="work@rommler.de"

# Make port 12080 available to the world outside this container
EXPOSE 12080
EXPOSE 10080

# The application's jar file
ARG JAR_FILE=build/libs/federation_controller-0.0.1-SNAPSHOT.jar

# Add the application's jar to the container
ADD ${JAR_FILE} app.jar

# Run the jar file
ENTRYPOINT ["java","-jar","/app.jar"]