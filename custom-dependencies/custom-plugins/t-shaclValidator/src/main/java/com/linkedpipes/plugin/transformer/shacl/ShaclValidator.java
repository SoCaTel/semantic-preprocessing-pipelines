package com.linkedpipes.plugin.transformer.shacl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationUtil;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;

/**
 * Takes two RDF files as input: a data file and a SHACL shapes files and
 * performs SHACL validation using the TopBraid library. Produces as output 
 * the SHACL validation RDF and writes it to a file in the working directory.
 */
public final class ShaclValidator implements Component, SequentialExecution {

    private static final Logger LOG
            = LoggerFactory.getLogger(ShaclValidator.class);
	
    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "DataFile")
    public FilesDataUnit dataFiles;

    @Component.InputPort(iri = "ShapesFile")
    public FilesDataUnit shapesFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Component.Configuration
    public ShaclValidatorConfiguration configuration;

	@Override
	public void execute() throws LpException {
        if (shapesFiles.size() > 1) {
            throw exceptionFactory.failure("Only one SHACL shapes file can be uploaded.");
        }

        Model shapesModel = null;
        for (final FilesDataUnit.Entry entry : shapesFiles) {
            shapesModel = getJenaModelFromEntry(entry);
        }

        progressReport.start(dataFiles.size());
        for (final FilesDataUnit.Entry entry : dataFiles) {
            Model dataModel = getJenaModelFromEntry(entry);
            
            LOG.info("Starting SHACL validation...");
            Model reportModel = validate(dataModel, shapesModel);
            
            LOG.info("Exporting SHACL validation report...");
            writeJenaModelToFile(reportModel, entry.getFileName());
            checkValidationReportConformity(reportModel, entry);
            progressReport.entryProcessed();
        }
        progressReport.done();
	}
	
	private Model validate(Model dataModel, Model shapesModel) throws LpException {
		Resource resource = ValidationUtil.validateModel(dataModel, shapesModel, false);
		Model reportModel = resource.getModel();
		// Makes report looks nicer and easier to read
		reportModel.setNsPrefix("sh", "http://www.w3.org/ns/shacl#");
		
		return reportModel;
	}
	
	private Model getJenaModelFromEntry(FilesDataUnit.Entry entry) throws LpException {
		String format = getJenaCompatibleFormat(entry);
		Model model = JenaUtil.createMemoryModel();

		try {
			model.read(new FileInputStream(entry.toFile()), null, format);
		} catch (FileNotFoundException e) {
			throw exceptionFactory.failure(
					"Failed to read file {}", entry.toFile().getAbsolutePath(), e);
		}
		return model;
	}
	
	private void writeJenaModelToFile(Model model, String entryFileName) throws LpException {
		final File outputFile =  outputFiles.createFile(entryFileName + "_report.ttl");
		try {
			// Default output language is "TTL" for now
			model.write(new FileOutputStream(outputFile), "TTL");
		} catch (FileNotFoundException e) {
			throw exceptionFactory.failure(
					"Failed to write file {}", outputFile.getAbsolutePath(), e);
		}
	}
	
	/**
	 * Checks the value of "sh:conforms" property in validation report to determine if the component
	 * execution should fail or not.
	 */
	private void checkValidationReportConformity(Model reportModel, FilesDataUnit.Entry entry) 
							throws LpException {
		Property conformsProp = reportModel.getProperty("http://www.w3.org/ns/shacl#", "conforms");
		StmtIterator stmtsItr = reportModel.listStatements(null, conformsProp, (RDFNode) null);
		boolean conforms = stmtsItr.next().getBoolean();
		LOG.info("SHACL validation returned: " + conforms + ". Check component output in"
				+ " working dir for full validation report.");

		if (conforms) {
			return;
		}
		if (configuration.isFailOnError()) {
			throw exceptionFactory.failure(
					"SHACL validation failed for entry {}", entry);
		}
	}

	private String getJenaCompatibleFormat(FilesDataUnit.Entry entry) throws LpException {
		RDFFormat rdfFormat = getEntryFormat(entry);
		return convertRdfFormatToJenaFormat(rdfFormat);
	}
	
	private RDFFormat getEntryFormat(FilesDataUnit.Entry entry) throws LpException {
		final Optional<RDFFormat> rdfFormat 
					= Rio.getParserFormatForFileName(entry.getFileName());
		if (!rdfFormat.isPresent()) {
		    throw exceptionFactory.failure(
		            "Can't determine format for file: {}", entry);
		}
		if (rdfFormat.get().supportsContexts()) {
		    throw exceptionFactory.failure(
		            "Quad-based formats are not supported.");
		}
		
		return rdfFormat.get();
	}

	/**
	 *  For SHACL validation with Jena, only RDF/XML, TURTLE (TTL), N-TRIPLES and N3 formats 
	 *  are accepted.
	 */
	private String convertRdfFormatToJenaFormat(RDFFormat rdfFormat) throws LpException {
		if (rdfFormat != RDFFormat.RDFXML
				&& rdfFormat != RDFFormat.TURTLE
				&& rdfFormat != RDFFormat.NTRIPLES
				&& rdfFormat != RDFFormat.N3) {
		    throw exceptionFactory.failure(
		            "Unsupported format: {}. Only RDF/XML, Turtle (ttl), "
		            + "N-Triples and N3 are accepted.", 
		            rdfFormat.getName());
		}
		
		return rdfFormat.getName().toUpperCase();
	}

}
