#!/usr/bin/env bash
# Author: Patrick Huang
set -e

SYNC_WAR=$(find sync-config-war/target -type f -name "*.war")
JOBS_WAR=$(find jobs-war/target -type f -name "*.war")

# default to build (skip tests) and deploy everything
BUILD="mvn clean package"
SKIP_TEST=" -DskipTests"

# default jboss home
JBOSS_HOME=/NotBackedUp/tools/jboss-eap

# When you want getopts to expect an argument for an option, just place a :
# (colon) after the proper option flag.
# If the very first character of the option-string is a : (colon), getopts
# switches to "silent error reporting mode".
while getopts ":cxfjw:ts:H" opt; do
  case ${opt} in
    c)
      echo ">> building sync-config-war <<"
      APP=" -pl sync-config-war -am "
      DEPLOY_TARGET="sync.war"
      ;;
    x)
      echo ">> building sync-config-war excluding frontend <<"
      APP=" -pl sync-config-war -am -DexcludeFrontend "
      DEPLOY_TARGET="sync.war"
      ;;
    f)
      echo ">> building frontend <<"
      APP=" -pl frontend "
#      install frontend to local maven repo
      BUILD="mvn clean install"
      DEPLOY_TARGET="NONE"
      ;;
    j)
      echo ">> building jobs-war <<"
      APP=" -pl jobs-war -am "
      DEPLOY_TARGET="jobs.war"
      ;;
    w)
      echo ">> to wipe out the data in your local database (sync_local), run:<<"
      echo "mysql -uroot -p${OPTARG} -e \"drop database sync_local;create database sync_local;\" sync_local"
      exit
      ;;
    s)
      echo ">> override jboss home to ${OPTARG} <<"
      JBOSS_HOME="$OPTARG"
      ;;
    t)
      echo ">> will run test as part of the build <<"
      SKIP_TEST=""
      ;;
    H)
      echo "========   HELP   ========="
      echo "-f                 : will build the frontend module"
      echo "-c                 : will build (including frontend) and deploy the sync-config-war module"
      echo "-x                 : will build (excluding frontend) and deploy the sync-config-war module"
      echo "-j                 : will build and deploy the jobs-war module"
      echo "-t                 : the build will run tests (default is skip all tests)"
      echo "-w <mysqlpassword> : will display command to wipe out the data in your local database (sync_local)"
      echo "-H                 : display help"
      echo "-s <jboss home>    : will override JBOSS_HOME. Default is ${JBOSS_HOME}"
      echo "If no option is given, it will try to build and deploy everything"
      exit
      ;;
    \?)
      echo "Invalid option: -${OPTARG}. Use -H for help" >&2
      exit 1
      ;;
  esac
done

CMD="$BUILD $APP $SKIP_TEST"

set -x

${CMD}

DEPLOYMENTS=${JBOSS_HOME}/standalone/deployments

if [ "${DEPLOY_TARGET}" = "sync.war" ];
then
    cp ${SYNC_WAR} ${DEPLOYMENTS}/${DEPLOY_TARGET}
elif [ "${DEPLOY_TARGET}" = "jobs.war" ];
then
    cp ${JOBS_WAR} ${DEPLOYMENTS}/${DEPLOY_TARGET}
elif [ "${DEPLOY_TARGET}" = "NONE" ];
then
    echo installed frontend to the local maven repo
else
    cp ${SYNC_WAR} ${DEPLOYMENTS}/sync.war && cp ${JOBS_WAR} ${DEPLOYMENTS}/jobs.war
fi

