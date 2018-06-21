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


<br /><br /><img src="./small_h-trans.png" />


The Imixs-Admin client provides a Docker Image to be used to run the service in a Docker conatiner. 
The docker image is based on the docker image [imixs/wildfly](https://hub.docker.com/r/imixs/wildfly/).

You can start the latest version of the Imixs-Admin Tool in a docker container run:

	$ docker run --name="imixs-admin" -d -p 8080:8080  imixs/imixs-admin

You can start the application from your browser

	http://localhost:8080/

## Build Imixs-Admin from sources

Alternatively you can build the imixs-admin client manually from sources and start from your local docker image:

	$ mvn clean install -Pdocker-build
	$ docker run --name="imixs-admin" -it -p 8080:8080  imixs/imixs-admin



To push the docker image into a registry run

	$ mvn clean install -Pdocker-push -Dorg.imixs.docker.registry=localhost:5000

where 'localhost:5000' need to be replaced with your private registry.



## Development

During development you can use the docker-compose-dev.yml file. This stack maps the src/docker/deployments folder to the wildfly auto deploy directory. 

	$ docker-compose -f docker-compose-dev.yml up
	
you may have to grant the deployment folder first to allow the docker non privileged user to access this location.

	$ sudo chmod 777 src/docker/deployments/


### The Maven 'dev' Profile

Building the applicaiton with the maven profile 'dev' changes the root context of the application from '/' to '/dev/'. This can be used in cases when the admin tool is deployed parallel to other applications. 
