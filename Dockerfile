FROM openjdk:11
# FROM wildfly/wildfly-runtime-jdk11


LABEL description="Imixs-Admin"
LABEL maintainer="ralph.soika@imixs.com"



# Set the working directory
WORKDIR /opt

# set environments 
# https://github.com/wildfly/wildfly/releases/download/27.0.0.Alpha4/wildfly-27.0.0.Alpha4.tar.gz
ENV WILDFLY_VERSION 27.0.0.Alpha4
#ENV WILDFLY_VERSION 26.1.0.Final
ENV WILDFLY_HOME=/opt/wildfly
ENV WILDFLY_DEPLOYMENT=$WILDFLY_HOME/standalone/deployments
ENV WILDFLY_CONFIG=$WILDFLY_HOME/standalone/configuration
ENV DEBUG=false


# Add the WildFly distribution to /opt, and make wildfly the owner of the extracted tar content
# Make sure the distribution is available from a well-known place
RUN curl -L https://github.com/wildfly/wildfly/releases/download/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz | tar zx \
 && ln -s $WILDFLY_HOME-$WILDFLY_VERSION/ $WILDFLY_HOME  
	
# Add the Wildfly start script 
ADD wildfly_start.sh $WILDFLY_HOME/
ADD wildfly_add_admin_user.sh $WILDFLY_HOME/       
RUN chmod +x $WILDFLY_HOME/wildfly_add_admin_user.sh $WILDFLY_HOME/wildfly_start.sh

# add eclipsliknk configuration 
#COPY modules/ $WILDFLY_HOME/modules/


# Expose the ports we're interested in
EXPOSE 8080 9990

CMD ["/opt/wildfly/wildfly_start.sh"]
