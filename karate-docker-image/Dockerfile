FROM bellsoft/liberica-openjdk-alpine:17

ENV KARATE_VERSION=1.4.0 \
    KARATE_HOME=/opt/karate

# Download karate runtime
RUN mkdir ${KARATE_HOME} && \
    cd ${KARATE_HOME} && \
    wget https://github.com/karatelabs/karate/releases/download/v${KARATE_VERSION}/karate-${KARATE_VERSION}.jar

# Add self-signed server certificates
ADD target/dependency/certs/server.crt ${KARATE_HOME}/certs/
ADD target/dependency/certs/server.key ${KARATE_HOME}/certs/
ADD target/dependency/certs.jar ${KARATE_HOME}/

# Define working directorys
WORKDIR ${KARATE_HOME}

CMD exec java -cp certs.jar:karate-${KARATE_VERSION}.jar com.intuit.karate.Main $KARATE_OPTS