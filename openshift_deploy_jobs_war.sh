#!/bin/bash

# Author: Patrick Huang
# usage: -a <appname> to override app name on openshift.
#        -b to build the war first or it will try to find the war


# app name on openshift
APP=syncjobs

#
echo "===== WARNING ====="
echo "for binary deployment to work, you need to create your app in such way (replace your cartridge(s), gear and app name to suit)"
echo "Allowed size: int_hosted_small, int_hosted_medium, ext_hosted_small,ext_hosted_medium, and ext_hosted_large"
echo "  rhc create-app -a $APP -t jbosseap-6 -g int_hosted_medium --no-git --scaling"
echo "  rhc configure-app -a $APP --no-auto-deploy"
echo "  rhc configure-app -a $APP --deployment-type binary"
echo "===== make sure you have done above ===="

while getopts ":ba:" opt; do
  case ${opt} in
    b)
      echo "-b will build the war first!" >&2
      mvn clean package -DskipTests
      ;;
    a)
      echo ">> override app name to $OPTARG"
      APP=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG. Accepts -b to build the war first. -a <appname> to override openshift app name" >&2
      ;;
  esac
done


WAR=$(ls ./jobs-war/target/*.war)

echo "[!] war file:$WAR"

if [ ! -f "$WAR" ]; then
    echo "=== war file not exist at $WAR. Give option -b to build it first."
    exit 1
fi


WORKDIR=/tmp/${APP}-openshift
# delete previous one if exists
rm -rf ${WORKDIR}

mkdir ${WORKDIR}

mkdir ${WORKDIR}/build_dependencies
mkdir -p ${WORKDIR}/dependencies/jbosseap/deployments/
mkdir ${WORKDIR}/repo/

# copy .openshift/ folder over
cp -r ./jobs-war/.openshift ${WORKDIR}/repo

cp ${WAR} ${WORKDIR}/dependencies/jbosseap/deployments/ROOT.war

BUNDLE=/tmp/${APP}.tar.gz
cd ${WORKDIR} && tar -czvf ${BUNDLE} ./ && cd -

echo "=== created at $BUNDLE  ===="
echo "=== next, run:"
echo "rhc deploy -a $APP $BUNDLE"

rhc deploy -a $APP ${BUNDLE}
