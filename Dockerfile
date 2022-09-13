# Use latest jboss/base-jdk:11 image as the base
# See details: https://github.com/jboss-dockerfiles/wildfly/blob/master/Dockerfile
FROM jboss/base-jdk:11

LABEL description="Imixs-Admin"
LABEL maintainer="ralph.soika@imixs.com"

USER root

# set environments 
# https://github.com/wildfly/wildfly/releases/download/27.0.0.Alpha4/wildfly-27.0.0.Alpha4.tar.gz
ENV WILDFLY_VERSION 27.0.0.Alpha5
ENV JBOSS_HOME /opt/jboss/wildfly
ENV WILDFLY_DEPLOYMENT=JBOSS_HOME/standalone/deployments
ENV WILDFLY_CONFIG=JBOSS_HOME/standalone/configuration
ENV DEBUG=false

# Add the WildFly distribution to /opt, and make wildfly the owner of the extracted tar content
# Make sure the distribution is available from a well-known place
RUN cd $HOME \
    && curl -L -O https://github.com/wildfly/wildfly/releases/download/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
    && tar xf wildfly-$WILDFLY_VERSION.tar.gz \
    && mv $HOME/wildfly-$WILDFLY_VERSION $JBOSS_HOME \
    && rm wildfly-$WILDFLY_VERSION.tar.gz \
    && chown -R jboss:0 ${JBOSS_HOME} \
    && chmod -R g+rw ${JBOSS_HOME}

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND true

# Add the Wildfly start script 
ADD wildfly_start.sh $JBOSS_HOME/
ADD wildfly_add_admin_user.sh $JBOSS_HOME/       
RUN chmod +x $JBOSS_HOME/wildfly_add_admin_user.sh $JBOSS_HOME/wildfly_start.sh

USER jboss

# add imixs-admin.war
COPY target/imixs-admin.war $JBOSS_HOME/standalone/deployments/

# Expose the ports we're interested in
EXPOSE 8080 8787 9990

CMD ["/opt/jboss/wildfly/wildfly_start.sh"]
# Set the default command to run on boot
# This will boot WildFly in standalone mode and bind to all interfaces
#CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
