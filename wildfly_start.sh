#!/bin/bash
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#                                                                                       #
# = = = = = = = = =  Starting Wildfly container = = = = = = = = = = = = = = = = = = = = #
#                                                                                       #
# Usage to launch in debug mode                                                         #
#  ... -e DEBUG=true                                                                    #
#                                                                                       #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# disable admin user
#if [ ! -f /opt/wildfly/.wildfly_admin_created ]; then
#  /opt/wildfly/wildfly_add_admin_user.sh
#fi

# test for starting in debug mode...
if [ "$DEBUG" == "true" ]; then    
  echo "starting wildfly in debug mode..."
  exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 --debug *:8787
else
  echo "starting wildfly..."
  exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0
fi;
