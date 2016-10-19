#!/usr/bin/env bash
# Author Patrick Huang <pahuang@redhat.com>

# volume mapping for sync deployment files
SYNC_DEPLOYMENT_DIR=$HOME/docker-volumes/sync-deployments
# create the directory and set permissions (SELinux)
mkdir -p ${SYNC_DEPLOYMENT_DIR} && chcon -Rt svirt_sandbox_file_t "$SYNC_DEPLOYMENT_DIR"

CONFIG_WAR=$(find . -type f -name "*config*.war")
JOBS_WAR=$(find . -type f -name "*jobs*.war")

# default docker network to join
DOCKER_NETWORK=docker-network

set -x

if [ -f ${CONFIG_WAR} ]
then
    echo ">>>> copy existing config war: $(ls -ltr ${CONFIG_WAR})"
    cp ${CONFIG_WAR} ${SYNC_DEPLOYMENT_DIR}/sync.war
else
    mvn clean package -DskipTests -pl sync-config-war -am
    cp $(find . -type f -name "*config*.war") ${SYNC_DEPLOYMENT_DIR}/sync.war
fi

if [ -f ${JOBS_WAR} ]
then
    echo ">>>> copy existing jobs war: $(ls -ltr ${JOBS_WAR})"
    cp ${JOBS_WAR} ${SYNC_DEPLOYMENT_DIR}/jobs.war
else
    mvn clean package -DskipTests -pl jobs-war -am
    cp $(find . -type f -name "*jobs*.war") ${SYNC_DEPLOYMENT_DIR}/jobs.war
fi

JBOSS_DEPLOYMENT_VOLUME=/opt/jboss/wildfly/standalone/deployments

CACERTS_DIR=/etc/pki/ca-trust/extracted/
echo "you need to import all needed CA certs into your host machine's security store. Check jobs-war/README.md"
echo "$CACERTS_DIR will be mounted to the docker container"

while getopts ":n:h" opt; do
  case ${opt} in
    n)
      echo ">> use docker network $OPTARG <<"
      DOCKER_NETWORK=$OPTARG
      ;;
    h)
      echo "========   HELP   ========="
      echo "-n <docker network>: will connect container to given docker network"
      echo "-h                 : display help"
      exit
      ;;
    \?)
      echo "Invalid option: -${OPTARG}. Use -h for help" >&2
      exit 1
      ;;
  esac
done

# check if the docker network is already created
if docker network ls | grep -w ${DOCKER_NETWORK}
then
    echo "will use docker network $DOCKER_NETWORK"
else
    echo "creating docker network $DOCKER_NETWORK"
    docker network create ${DOCKER_NETWORK}
fi

docker run --rm -p 8080:8080 --name sync-dev \
    --net=${DOCKER_NETWORK} \
    -v ${SYNC_DEPLOYMENT_DIR}:${JBOSS_DEPLOYMENT_VOLUME} \
    -v ${CACERTS_DIR}:${CACERTS_DIR} \
    zanata/sync --debug
