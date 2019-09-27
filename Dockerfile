ARG NEXUS_VERSION=3.19.0-SNAPSHOT

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.19.0
ARG NEXUS_BUILD=SNAPSHOT

COPY . /nexus-repository-apk/
RUN cd /nexus-repository-apk/; sed -i "s/3.19.0-02/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn clean package -PbuildKar;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.19.0
ARG NEXUS_BUILD=SNAPSHOT
ARG APK_VERSION=0.0.1
ARG DEPLOY_DIR=/opt/sonatype/nexus/deploy/
USER root
COPY --from=build /nexus-repository-apk/target/nexus-repository-apk-${APK_VERSION}-bundle.kar ${DEPLOY_DIR}
USER nexus
