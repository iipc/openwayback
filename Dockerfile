ARG MAVEN_TAG=latest
ARG TOMCAT_TAG=latest

# Building stage
FROM maven:${MAVEN_TAG} AS builder
WORKDIR /src

# Trick to utilize cache for dependencies for faster successive builds
# setting up a custom local repo path due to https://github.com/carlossg/docker-maven/issues/11
ARG MAVEN_OPTS=-Dmaven.repo.local=/mvnrepo/.m2/
ENV MAVEN_OPTS=$MAVEN_OPTS
COPY pom.xml ./
COPY dist/pom.xml dist/
COPY wayback-cdx-server-core/pom.xml wayback-cdx-server-core/
COPY wayback-cdx-server-webapp/pom.xml wayback-cdx-server-webapp/
COPY wayback-core/pom.xml wayback-core/
COPY wayback-webapp/pom.xml wayback-webapp/
RUN mvn dependency:go-offline -fn

# Actual packaging
COPY . .
RUN mvn package
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
