#!/usr/bin/env bash
# Author: Patrick Huang <pahuang@redhat.com>

set -x
set -e

DB_IMAGE=postgres:9.2
DB_NAME=sync_local
# postgres super user and password
DB_ROOT_USER=posgres
DB_ROOT_PASSWORD=posgres

CONTAINER_NAME=postgres-db

DB_DOCKER_ENV="-e PGPASSWORD=$DB_ROOT_PASSWORD -e POSTGRES_USER=$DB_ROOT_USER"

VOLUME_DIR=$HOME/docker-volumes/sync-postgres
mkdir -p ${VOLUME_DIR}
sudo chcon -Rt svirt_sandbox_file_t ${VOLUME_DIR}

# remove previous container
docker rm -f ${CONTAINER_NAME}

# create a database
CREATE_DB="docker run -it --rm --link $CONTAINER_NAME:postgres $DB_DOCKER_ENV postgres:9.2 createdb -h postgres -U postgres $DB_NAME"

# start the postgres db in the background with password
PGDATA_VOLUME=/var/lib/postgresql/data/pgdata
CMD="docker run -d --name $CONTAINER_NAME $DB_DOCKER_ENV -e PGDATA=$PGDATA_VOLUME -e DB_NAME=$DB_NAME -v $VOLUME_DIR:$PGDATA_VOLUME -p 5432:5432 $DB_IMAGE"

while getopts ":cpH" opt; do
  case ${opt} in
    c)
      echo ">> create new database <<"
      CMD="$CMD && sleep 5 && $CREATE_DB"
      ;;
    p)
      echo ">> pull docker image <<"
      docker pull ${DB_IMAGE}
      ;;
    H)
      echo "========   HELP   ========="
      echo "-b                 : will build the app"
      echo "-c                 : will create database: $DB_NAME"
      echo "-p                 : will pull docker image: $DB_IMAGE"
      echo "-H                 : display help"
      exit
      ;;
    \?)
      echo "Invalid option: -${OPTARG}. Use -H for help" >&2
      exit 1
      ;;
  esac
done

bash -c "$CMD"
