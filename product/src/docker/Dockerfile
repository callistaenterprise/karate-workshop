FROM openjdk:17-jdk as builder
WORKDIR /workspace/app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM bellsoft/liberica-openjdk-alpine:17
VOLUME /tmp

RUN addgroup --system --gid 1001 app && adduser --system --uid 1001 -G app app
USER app

COPY --from=builder /workspace/app/dependencies/ ./
COPY --from=builder /workspace/app/spring-boot-loader/ ./
COPY --from=builder /workspace/app/snapshot-dependencies/ ./
# Workaround for https://github.com/moby/moby/issues/37965
RUN true
COPY --from=builder /workspace/app/application/ ./
ENV JAVA_TOOL_OPTIONS "-XX:MaxRAMPercentage=90 -Dspring.profiles.active=docker -Duser.timezone=UTC"
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
