# Imixs-Admin

The Imixs-Admin project provides a web based tool to administrate the Imixs-Workflow engine.  
Imixs-Admin is a web client based on the web framework ['MVC 1.0'](https://www.mvc-spec.org/). The client interacts with the Imxis-Workflow Engine via the Imixs-Rest API. 

These are the core features of Imixs-Admin:

* Search documents using Lucene search queries
* Update or add properties of documents
* Process a set of workitems based on the deployed workflow model
* Delete a set of documents
* Start AdminP Jobs
 



<br /><br /><img src="./small_h-trans.png" />


The Imixs-Admin client provides a Docker Image to be used to run the service in a Docker container. 
The docker image is based on the docker image [imixs/wildfly](https://hub.docker.com/r/imixs/wildfly/).

You can start the latest version of the Imixs-Admin Tool in a docker container runing the provided docker-compose.yml file:

	$ docker-compose up

You can start the application from your browser

	http://localhost:8080/

## Build Imixs-Admin from sources

Alternatively you can build the imixs-admin client manually from sources and start from your local docker image:

	$ mvn clean install -Pdocker-build
	$ docker-compose up


To push the docker image into a registry run

	$ mvn clean install -Pdocker-push -Dorg.imixs.docker.registry=localhost:5000

where 'localhost:5000' need to be replaced with your private registry.

### Ozark Implementations

The MVC 1.0 Ozark reference implementation is provided in two different versions optimized for RestEasy (Wildfly) and Jersey (Glassfish). Find installation details [here](https://www.mvc-spec.org/ozark/docs/install-javaee.html).

You can build the admin client for glassfish using the maven profile 'jersey'

	$ mvn clean install -Pjersey 
	
**Note:** The default profile uses the RestEasy implementation for Wildfly. 

## Development

Imixs-Admin is provided as a Maven Web Module and can be build by the maven command:

	$ mvn clean install

The .war file can be deployed into any Jakarta EE Application server.

During development you can use the docker-compose-dev.yml file. This stack maps the src/docker/deployments folder to the wildfly auto deploy directory. 

	$ docker-compose -f docker-compose-dev.yml up
	
you may have to grant the deployment folder first to allow the docker non privileged user to access this location.

	$ sudo chmod 777 src/docker/deployments/


### The Maven 'dev' Profile

Building the applicaiton with the maven profile 'dev' changes the root context of the application from '/' to '/dev/'. This can be used in cases when the admin tool is deployed parallel to other applications. 
