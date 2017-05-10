ARG MAVEN_TAG=latest
ARG TOMCAT_TAG=latest
ARG SKIP_TEST=false

# Building stage
FROM maven:${MAVEN_TAG} AS builder
COPY . /src
WORKDIR /src
RUN mvn package -Dmaven.test.skip=${SKIP_TEST}
RUN tar xvzf dist/target/openwayback.tar.gz -C dist/target \
    && mkdir dist/target/openwayback/ROOT \
    && cd dist/target/openwayback/ROOT \
    && jar -xvf ../*.war

# Image creation stage
FROM tomcat:${TOMCAT_TAG}
LABEL maintainer="Sawood Alam <@ibnesayeed>"

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=builder /src/dist/target/openwayback/ROOT /usr/local/tomcat/webapps/ROOT
COPY --from=builder /src/dist/target/openwayback/bin /usr/local/bin/

VOLUME /data

ENV WAYBACK_HOME=/usr/local/tomcat/webapps/ROOT/WEB-INF \
    WAYBACK_BASEDIR=/data \
    WAYBACK_URL_SCHEME=http \
    WAYBACK_URL_HOST=localhost \
    WAYBACK_URL_PORT=8080 \
    WAYBACK_URL_PREFIX=http://localhost:8080
