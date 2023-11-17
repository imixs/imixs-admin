FROM quay.io/wildfly/wildfly:27.0.1.Final-jdk11
LABEL description="Imixs-Admin"
LABEL maintainer="ralph.soika@imixs.com"

# Deploy artefact
ADD target/imixs-admin.war $JBOSS_HOME/standalone/deployments/

WORKDIR /opt/jboss
# Run with management interface
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
