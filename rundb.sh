#!/usr/bin/env bash
# Author: Patrick Huang <pahuang@redhat.com>

set -x
set -e

DB_IMAGE=postgres:9.2
DB_NAME=sync_local
# postgres super user and password
DB_ROOT_USER=posgres
DB_ROOT_PASSWORD=posgres

CONTAINER_NAME=syncdb

# default docker network to join
DOCKER_NETWORK=docker-network

DB_DOCKER_ENV="-e PGPASSWORD=$DB_ROOT_PASSWORD -e POSTGRES_USER=$DB_ROOT_USER"

VOLUME_DIR=$HOME/docker-volumes/sync-postgres
mkdir -p ${VOLUME_DIR}
sudo chcon -Rt svirt_sandbox_file_t ${VOLUME_DIR}

# remove previous container if exists
if [ ! -z $(docker ps -aq -f name=${CONTAINER_NAME}) ]
then
    docker rm -f ${CONTAINER_NAME}
fi

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


PGDATA_VOLUME=/var/lib/postgresql/data/pgdata
# start the postgres db in the background with password
docker run -d --name ${CONTAINER_NAME} --net=${DOCKER_NETWORK} ${DB_DOCKER_ENV} \
    -e PGDATA=${PGDATA_VOLUME} -e DB_NAME=${DB_NAME} \
    -v ${VOLUME_DIR}:${PGDATA_VOLUME} -p 5432:5432 ${DB_IMAGE}

# to make sure the container is up and running
sleep 5

# create database. It will fail if it already exists but that's ok.
# it's possible to use http://stackoverflow.com/a/16783253/345718 as a workaround to check database existence but it's a bit too tricky
if docker run -it --net=${DOCKER_NETWORK} --rm ${DB_DOCKER_ENV} ${DB_IMAGE} createdb -h ${CONTAINER_NAME} --username ${DB_ROOT_USER} $DB_NAME 2>/dev/null
then
    echo "$DB_NAME created"
else
    echo "$DB_NAME already exists"
fi
