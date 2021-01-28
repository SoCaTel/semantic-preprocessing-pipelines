![SoCaTelLogo](https://www.socatel.eu/wp-content/uploads/2018/02/logo_Socatel_L.png)
# **Linked Pipes Plugins for SoCaTel**

This directory contains the plugins that were implemented to extend [LinkedPipes](https://etl.linkedpipes.com/) in order to fulfill the requirements of the semantic pre-processing layer of SoCaTel. In the following, we explain the plguins that we implemented in detail.

## **l-graphStoreProtocolGraphDB**

Allows the user to store RDF triples into a GraphDB repository using [SPARQL 1.1 Graph Store HTTP Protocol](https://www.w3.org/TR/sparql11-http-rdf-update/). It is an extension of the plugin [Graph Store Protocol](https://etl.linkedpipes.com/components/l-graphstoreprotocol), and was implemented since LinkedPipes does not currently support connections to GraphDB. It has the following configuration parameters:

* **Repository:**  the target repository type (fixed to GraphDB).
* **Graph Store protocol endpoint:** URL of the graph store protocol endpoint.
* **Target graph IRI:** IRI of the RDF graph to which the input data will be loaded.
* **Clear target graph before loading:** when checked, replaces the target graph by the loaded data.
* **Log graph size:** when checked, the target graph size before and after the load is logged.
* **Use authentication:** when checked, prompts the user to enter credentials to authenticate the call to the graph store protocol.
* **User name:** username to be used to log in to the SPARQL endpoint.
* **Password:** password to be used to log in to the SPARQL endpoint

In the semantic pre-processing pipelines, this plugin is used to store the transformed data into the knowledge base's GraphDB semantic repository and its configuration should not be changed unless a change in the GraphDB configuration occurs (e.g. new SPARQL Endpoint URL, user credentials).

## **t-socatelExternalDataTransformer**

Applies a custom transformation on the output of the data handler in order to modify the structure of the JSON files as a pre-step to their conversion to JSON-LD, and later on to RDF. It can be configured using the following four parameters:

* **Data source type:** type of source to be transformed. The plugin currently supports 5 different types of sources (Twitter, Facebook Post, Facebook Page, OpenData-CKAN and SoCaTel Service).
* **Single element / Array of elements:** specifies whether the input JSON contains a single JSON element or a JSON array of elements in one of the supported types. The elements of an array must all be of the same data source type.
* **JSON path to single element:** specifies the JSON path to the root JSON object that contains the single element to be transformed. When used in combination with "JSON path to array element", this parameter specifies the path inside each element of the array. It is only used if the JSON passed to the components comes from ElasticSearch and has unnecessary information at the beginning that should not be transformed,  otherwise, it should be left empty.
* **JSON path to array element:** specifies the JSON path to the root JSON object that contains the array of elements to be transformed. 

This plugin perfoms a task specific to SoCaTel and it is not foreseen that it will be useful in any other project or implementation.

## **t-shaclValidator**

Performs a SHACL validation test on the input RDF data using the input [SHACL shapes](https://www.w3.org/TR/shacl/). Both inputs are specified as files, which are read by the plugin as Jena RDFmodels to comply with the needs of the used SHACL validation library ([TopBraid](https://github.com/TopQuadrant/shacl)). It has one configuration parameter:

* **Fail component on validation error:** when checked, the plugin execution will fail (thus halting the execution of subsequent components in the pipeline) in case the input RDF data does not conform to the input SHACL shapes.

In the semantic pre-processing pipelines, this plugin is used to check that the converted RDF data complies with the predefined QA business rules, before storing it into the knowledge base's GraphDB semantic repository.

## **e-sparqlEndpointMultiConstruct**

Executes multiple SPARQL CONSTRUCT queries to extract RDF triples from a SPARQL endpoint. The plugin maps variables in the query template to columns of the input CSV file with the same name, executing a separate CONSTRCT query for each row in the CSV. The first row of the CSV file **MUST** be a header row that contains column names. The plugin can be configured using the two following groups of parameters:

The first set of parameters is there to specify how the CSV files should be parsed:

* **Delimiter:** character separating columns in a row in non-standard CSV files. For the tab character (used as delimiter in TSV files), enter \t.
* **Quote:** character used as quote in case the column value contains the column separator.
* **Encoding:** encoding of the CSV file.

The second group of parameters deals with the endpoint and query to be executed:

* **Endpoint URL:** URL of the SPARQL endpoint to be queried.
* **Use authentication:** when checked, prompts the user to enter credentials to authenticate the call to the graph store protocol.
* **User name:** username to be used to log in to the SPARQL endpoint.
* **Password:** password to be used to log in to the SPARQL endpoint
* **MIME type:** MIME type to be used in the Accept header of the HTTP request made by RDF4J to the remote endpoint.
* **Encode invalida IRIs:** when checked, tries to fix IRIs by encoding unencoded special characters.
* **Fix missing language tags on rdf:langString literals:** when checked, tries to fix missing language tags in the returned RDF data.
* **Default graph:** specifies one or more default graphs of the target endpoint for the query to be executed.
* **SPARQL CONSTRUCT query template:** query template for extraction of triples from the endpoint. 

In SoCaTel, this plugin was used as part of the Linked Open Data Handler to retrieve additional information related to a set of input values (specified in the CSV file) from external Linked Open Data sources (specified in the Endpoint settings).