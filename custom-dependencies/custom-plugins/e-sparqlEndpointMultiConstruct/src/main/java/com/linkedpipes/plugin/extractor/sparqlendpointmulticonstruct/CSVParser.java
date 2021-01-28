package com.linkedpipes.plugin.extractor.sparqlendpointmulticonstruct;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.QuoteMode;
import org.supercsv.util.CsvContext;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;

public class CSVParser {

    private static final Logger LOG = LoggerFactory.getLogger(CSVParser.class);

    private final CsvPreference csvPreference;
    
    private final String encoding;

    private final ExceptionFactory exceptionFactory;
	
	CSVParser(SPARQLEndpointMultiCONSTRUCTConfiguration configuration,
            ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
        // We will use quates only if they are provided
        if (configuration.getQuoteChar() == null ||
        		configuration.getQuoteChar().isEmpty()) {
            // We do not use quates.
            final QuoteMode customQuoteMode = (String csvColumn,
                    CsvContext context, CsvPreference preference) -> false;
            // Quote char is never used.
            csvPreference = new CsvPreference.Builder(' ',
                    getEffectiveDelimiter(configuration.getDelimiter()),
                    "\\n").useQuoteMode(customQuoteMode).build();
            // Line terminators are also part of the configuration!
        } else {
            csvPreference = new CsvPreference.Builder(
                    configuration.getQuoteChar().charAt(0),
                    getEffectiveDelimiter(configuration.getDelimiter()),
                    "\\n").build();
        }
        
        if (configuration.getEncoding() == null ||
        		configuration.getEncoding().isEmpty()) {
        	encoding = "UTF-8";
        } else {
        	encoding = configuration.getEncoding();
        }
    }
    
    public void parse(FilesDataUnit.Entry entry,
    		List<String> mappedVariables, List<List<String>> mappedVariableValues) throws LpException {
        try (final FileInputStream fileInputStream
                = new FileInputStream(entry.toFile());
                final InputStreamReader inputStreamReader
                = getInputStream(fileInputStream);
                final BufferedReader bufferedReader
                = new BufferedReader(inputStreamReader);
                final CsvListReader csvListReader
                = new CsvListReader(bufferedReader, csvPreference)) {
        	
        	// Read file headers
        	List<String> localMappedVars = Arrays.asList(csvListReader.getHeader(true));
            if (localMappedVars == null) {
            	throw exceptionFactory.failure("Input CSV file does not have header: {}", entry.getFileName());
            }
            mappedVariables.addAll(localMappedVars);
            LOG.info("Processed mapped variables: {}", localMappedVars.toString());
        	
        	// Read lines
        	List<String> line;
        	List<List<String>> localMappedVarValues = new ArrayList<List<String>>();
        	while ((line = csvListReader.read()) != null) {
        		localMappedVarValues.add(line);
        	}
        	if (localMappedVarValues.size() == 0) {
            	throw exceptionFactory.failure("Input CSV file does not have any lines: {}", entry.getFileName());
        	}
        	mappedVariableValues.addAll(localMappedVarValues);
            LOG.info("Processed {} mapped variables value(s)", localMappedVarValues.size());

        } catch (IOException e) {
            throw exceptionFactory.failure("Can't process file: {}", entry.getFileName(), e);
		}
    }

    /**
     * Create {@link InputStreamReader}. If "UTF-8" as encoding is given then
     * {@link BOMInputStream} is used as wrap of given fileInputStream
     * and output {@link InputStreamReader} to remove possible
     * BOM mark at the start of "UTF" files.
     *
     * @param fileInputStream
     * @return
     */
    private InputStreamReader getInputStream(FileInputStream fileInputStream)
            throws UnsupportedEncodingException {
        if (encoding.compareToIgnoreCase("UTF-8") == 0) {
            return new InputStreamReader(
                    new BOMInputStream(fileInputStream, false),
                    encoding);
        } else {
            return new InputStreamReader(fileInputStream,
            		encoding);
        }
    }
	

    private char getEffectiveDelimiter(String deliminterStr) {
        if (deliminterStr.equals("\\t")) {
            return '\t';
        } else {
            return deliminterStr.charAt(0);
        }
    }
}
