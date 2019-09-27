ARG NEXUS_VERSION=3.16.1

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.16.1
ARG NEXUS_BUILD=02

COPY . /nexus-repository-conda/
RUN cd /nexus-repository-conda/; sed -i "s/3.16.1-02/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn clean package -PbuildKar;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.16.1
ARG NEXUS_BUILD=02
ARG CONDA_VERSION=0.0.3
ARG DEPLOY_DIR=/opt/sonatype/nexus/deploy/
USER root
COPY --from=build /nexus-repository-conda/target/nexus-repository-conda-${CONDA_VERSION}-bundle.kar ${DEPLOY_DIR}
USER nexus
