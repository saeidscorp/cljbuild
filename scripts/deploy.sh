#!/usr/bin/env bash

echo Building WAR file
lein immutant war

mv target/cljbuild.war target/ROOT.war

echo -- Running with Ruby:
ruby -v

echo Installing RHC command-line tools
gem install rhc

echo Uploading deployment
rhc -l saeidscorp@yahoo.com -p $OPENSHIFT_PASSWORD scp -a wildfly upload target/ROOT.war wildfly/standalone/deployments/

# rm the ROOT.war.failed file on server to cause re-deployment
