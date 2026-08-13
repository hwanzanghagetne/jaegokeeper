FROM maven:3.9-eclipse-temurin-17@sha256:4015718012bbf1113ec6cfae2b950be328d90265ceb60f92b26c3ea7c4d14ee8 AS builder

WORKDIR /app

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B clean package

FROM tomcat:10.1-jdk17-temurin@sha256:9eb082a17c024e694b9b7392a630b7298b39b63764d6551a7b284cea755ab3b2

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=builder /app/target/JaegoKeeper.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
    CMD curl -fsS http://127.0.0.1:8080/ || exit 1

CMD ["catalina.sh", "run"]
