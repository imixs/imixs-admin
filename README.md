# Imixs-Admin

The Imixs-Admin project provides a web based tool to administrate the Imixs-Workflow engine.  
Imixs-Admin is a web client based on the JavaScript library [Ben.JS](http://www.benjs.org). The client uses the Imixs-Rest API to access an instance of the Imixs-Workflow Engine. 

These are the core features of Imixs-Admin:

* Search entities using JPQL statements
* Update or add properties of entities
* Process a set of workitems based on the deployed workflow model
* Delete a set of entities
* Manage the Imixs-Entity-Index table
 
## Installation
Imixs-Admin is provided as a Maven Web Module and can be build by the maven command:

    mvn clean install

The .war file can be deployed into any JEE Application server. It is necessary to provide a security realm 'imixsrealm'. See also the security section in the [Deployment Guide](http://www.imixs.org/jee/deployment/overview.html) on [imixs.org](http://www.imixs.org)

###Deployment Info

Because in Wildfly a persistence.xml file is necessary the pom.xml
includes a 'wildfly' profile just to copy this blank persistence.xml into
the classes/META-INF/ folder. For GlassFish is file my not be included!
