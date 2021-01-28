<img src="https://platform.socatel.eu/images/socatel-logo.png" alt="SoCaTelLogo" width="250" />

# **LinkedPipes in SoCaTel**

This README file provides a go-to reference for the semantic pre-processing layer. It includes details on how to install, deploy and run LinkedPipes, the tool used for the semantic transformation of data. Moreover, it also details how the transformation pipelines can be executed once the tool is up and running.

[LinkedPipes](https://etl.linkedpipes.com/) is an open-source tool that permits the construction of ETL pipelines to convert data into semantic RDF format. It was chosen as the main tool for the development of the semantic pre-processing layer, and it is the only tool necessary for the correct functioning of the layer.

There are two different ways of deploying the semantic pre-processing layer:

1. Building the docker image locally: this should only be tried if downloading the docker image from the Nexus repository fails. Go to [this section](#dockerLocal) for details on how to do this.
2. Building the tool and docker image locally: this should be done when we want to deploy the tool locally without using dockers. Go to [this section](#localBuild) for details on how to do this.

If you encounter any problems during this process, contact [Rizkallah Touma](mailto:rizk.allah.touma@everis.com) from Everis, or in his absence [Emmanuel Jamin](mailto:emmanuel.jean.jacques.jamin@everis.com).

<a name="dockerLocal"></a>
## **Build docker image locally**

1. Clone this entire repository into your local development environment:
    
```
git clone https://github.com/SoCaTel/semantic-preprocessing-pipelines.git
```

2. Set the configuration properties in the file `./docker/pipeline.config.properties` on the machine where LinkedPipes is going to be deployed. Make sure to read the comments in the file and use the correct values for the properties.

3. From this directory root, execute the following commands to build the docker image and run LinkedPipes as a docker container:

```
cd ./docker
docker-compose up --build
```
This will build the LinkedPipes image that was created specifically for the SoCaTel project and start a docker container with the tool running. The frontend of the tool should be accessible at the URL: http://${HOST_IP}:32800, where `${HOST_IP}` is the address that was set in the configuration file in step 2.

### **Deployment URL**

In case we want to change the deployment URI of the tool, it is important to make the necessary changes to the configuration file before building the docker image.

<a name="localBuild"></a>
## **Build and deploy LinkedPipes locally**

1. Clone this entire repository into your local development environment:
    
```
git clone https://github.com/SoCaTel/semantic-preprocessing-pipelines.git
```
2. Set the configuration properties in the file `./docker/pipeline.config.properties` on the machine where LinkedPipes is going to be deployed. Make sure to read the comments in the file and use the correct values for the properties, and to remove the `.sample` from the end of the file name.

3. From this directory root, navigate to the `./etl` directory and build the core LinkedPipes tool:
    
```
cd ./etl/
mvn clean install -DskipTests
```
Note that this step might take around 10 minutes to complete, and it is highly recommended to skip the tests while building the tool. If you need any more help with this step, check out LinkedPipe's official [GitHub repository](https://github.com/linkedpipes/etl).

4. From this directory root, navigate to the directory `./custom-dependencies` and build the custom implemented plugins:

```
cd ./custom-dependencies/
mvn clean install -DskipTests
```

4. Execute the following script to apply the configuration properties set in step 2 to the files:

```
cp ./docker/updatePipelinesConfig.sh ./etl/deploy/
cd ./etl/deploy
./updatePipelinesConfig.sh
```

5. To run the semantic pre-processing on your local machine, navigate to the `./etl/deploy` directory and execute the following commands:

```
cd ./etl/deploy/
./executor.sh
./executor-monitor.sh
./storage.sh
./frontend.sh
```

The frontend of the tool should then be accessible at the default URL: http://${HOST_IP}:32800 , where `${HOST_IP}` is the address that was set in the configuration file in step 2.

If you need any more help with this step, check out LinkedPipe's official [GitHub repository](https://github.com/linkedpipes/etl).

### **Deployment URL**

In case we want to change the deployment URI of the tool, it is important to make the necessary changes to the configuration file before deploying the tool, then run the script `./docker/updateConfigIP.sh`. This script changes the properties in the configuration file, as well as the definitions of the pipelines and templates. These changes are detailed in this [LinkedPipes Wiki page](https://github.com/linkedpipes/etl/wiki/Migrating-existing-LinkedPipes-ETL-instance-to-a-new-IRI-scheme).

<a name="pipelineExecution"></a>
## **Executing Pipelines**

Once the tool is running, the different pipelines that make up the semantic pre-processing layer can be executed to transform data into RDF and store it into the knowledge base's GraphDB.

The pipelines are executed through HTTP POST requests. An example of such a request is the following:

```
curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@{INPUT_FILE}" http://{HOST_IP}:32800/resources/executions?pipeline={PIPELINE_URL}
```

There are three parameters that need changing in this example request:

* **{PIPELINE_URL}** indicates the predefined URL of the pipeline to be executed.
* **{INPUT_FILE}** indicates the JSON file to be passed to the pipeline and transformed into RDF.
* **{HOST_IP}** this is the IP address of the machine where LinkedPipes is deployed

### **{PIPELINE_URL}**

Currently, the five following pipelines have been developed and imported into LinkedPipes. Each of them is responsible for the transformation of one of the types of data sources available in SoCaTel:

| Pipeline Name           | Pipeline URL                                             |
|-------------------------|----------------------------------------------------------|
| SoCaTel - Twitter       | http://{HOST_IP}:32800/resources/pipelines/1552388831995 |
| SoCaTel - Facebook Post | http://{HOST_IP}:32800/resources/pipelines/1556787244055 |
| SoCaTel - Facebook Page | http://{HOST_IP}:32800/resources/pipelines/1556786854002 |
| SoCaTel - OpenData      | http://{HOST_IP}:32800/resources/pipelines/1556619766524 |
| SoCaTel - Service       | http://{HOST_IP}:32800/resources/pipelines/1565080262463 |
| SoCaTel - Organisation  | http://{HOST_IP}:32800/resources/pipelines/1575531753483 |
| SoCaTel - Group         | http://{HOST_IP}:32800/resources/pipelines/1578586045942 |
| SoCaTel - User          | http://{HOST_IP}:32800/resources/pipelines/1578586195746 |

### **{INPUT_FILE}**

The directory `./data/inputs` contains samples of input JSON files that can be used along with each of the pipelines specified above. These are just samples that can be substituted with any other JSON files. However, **note that to guarantee the correct functioning of the semantic pre-processing, any other input JSON files must follow the structures of these sample files.**

### **{HOST_IP}**

This represents the IP address of the machine where LinkedPipes is hosted, as defined by the `HOST_IP` property set before deploying LinkedPipes. It must be used both in the address as well as the pipeline URIs above.

### **Sample Executions**

Using the defined pipelines and the sample data available in `./data/inputs`, we can launch the following sample requests to test the semantic transformation of the different types of data in SoCaTel. Note that these examples assume that the `HOST_IP` is set to `localhost`, meaning that the CURL commands are launched from the same machine where LinkedPipes is deployed. 

| Data Type                             | Request                                                                                                                                                                                                           |
|---------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Twitter                               | curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@./data/inputs/twitter_sample.json" http://localhost:32800/resources/executions?pipeline=http://localhost:32800/resources/pipelines/1552388831995       |
| Facebook Post / Comment               | curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@./data/inputs/facebook_post_sample.json" http://localhost:32800/resources/executions?pipeline=http://localhost:32800/resources/pipelines/1556787244055 |
| Facebook Page                         | curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@./data/inputs/facebook_page_sample.json" http://localhost:32800/resources/executions?pipeline=http://localhost:32800/resources/pipelines/1556786854002 |
| Open Data (CKAN)                      | curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@./data/inputs/opendata_sample.json" http://localhost:32800/resources/executions?pipeline=http://localhost:32800/resources/pipelines/1556619766524      |
| Service                               | curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@./data/inputs/service_sample.json" http://localhost:32800/resources/executions?pipeline=http://localhost:32800/resources/pipelines/1565080262463       |
| Organisation                          | curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@./data/inputs/organisation_sample.json" http://localhost:32800/resources/executions?pipeline=http://localhost:32800/resources/pipelines/1575531753483       |
| Group                                 | curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@./data/inputs/group_sample.json" http://localhost:32800/resources/executions?pipeline=http://localhost:32800/resources/pipelines/1578586045942       |
| User                                  | curl -i -X POST -H "Content-Type: multipart/form-data" -F "input=@./data/inputs/user_sample.json" http://localhost:32800/resources/executions?pipeline=http://localhost:32800/resources/pipelines/1578586195746       |

If the POST request is successful, we should receive a response similar to the following:

```
HTTP/1.1 100 Continue

HTTP/1.1 200 OK
X-Powered-By: Express
Date: Mon, 15 Jul 2019 12:49:29 GMT
Connection: keep-alive
Transfer-Encoding: chunked

{"iri":"http://localhost:32800/resources/executions/1563194969190-28-afb349f0-e771-4ddc-9f90-5f87a6b02665"}`
```

The `"iri"` appearing at the end of the response represents the ID of the "pipeline execution". It can be used to check the pipeline's execution status and logs through LinkedPipe's frontend at the URL: http://localhost:32800/#/executions.

## **License**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Semantic Preprocessing Pipelines is under [Apache-2.0](LICENSE).

## **Contact**
[Rizkallah Touma](mailto:rizk.allah.touma@everis.com)

[Emmanuel Jamin](mailto:emmanuel.jean.jacques.jamin@everis.com)