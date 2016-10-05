#!/usr/bin/env bash
# Author Patrick Huang <pahuang@redhat.com>

# volume mapping for sync deployment files
SYNC_DEPLOYMENT_DIR=$HOME/docker-volumes/sync-deployments
# create the directory and set permissions (SELinux)
mkdir -p ${SYNC_DEPLOYMENT_DIR} && chcon -Rt svirt_sandbox_file_t "$SYNC_DEPLOYMENT_DIR"

CONFIG_WAR=$(find . -type f -name "*config*.war")
JOBS_WAR=$(find . -type f -name "*jobs*.war")

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

LINK_ZANATA=""
while getopts ":zH" opt; do
  case ${opt} in
    z)
      echo ">> link zanata docker container <<"
      LINK_ZANATA=" --link zanata:zanata "
      ;;
    H)
      echo "========   HELP   ========="
      echo "-z                 : will link zanata docker container"
      echo "-H                 : display help"
      exit
      ;;
    \?)
      echo "Invalid option: -${OPTARG}. Use -H for help" >&2
      exit 1
      ;;
  esac
done

docker run --rm -p 8080:8080 --name sync-dev \
    --link postgres-db:db $LINK_ZANATA \
    -v ${SYNC_DEPLOYMENT_DIR}:${JBOSS_DEPLOYMENT_VOLUME} \
    -v ${CACERTS_DIR}:${CACERTS_DIR} \
    zanata/sync
