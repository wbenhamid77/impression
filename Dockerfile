FROM maven:3.9.6
WORKDIR /app
COPY Impression/ .
RUN mvn clean package -DskipTests
EXPOSE 8083
CMD ["java", "-jar", "target/Impression-0.0.1-SNAPSHOT.jar"] 