FROM maven:3.5-jdk-8-alpine As build-env
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:resolve-plugins dependency:resolve -e
COPY . .
RUN mvn -B clean package -DskipTests -e

FROM openjdk:11-jre
COPY --from=build-env /build/target/app.jar /app.jar
RUN mkdir /logs
VOLUME /logs
ENV SERVER_SERVLET_CONTEXT_PATH="/"
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=""
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]
