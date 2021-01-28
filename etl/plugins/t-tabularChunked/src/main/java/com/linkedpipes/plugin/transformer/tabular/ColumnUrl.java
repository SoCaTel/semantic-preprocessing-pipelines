package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a column with IRI value.
 */
class ColumnUrl extends ColumnAbstract {

    private final UrlTemplate template;

    ColumnUrl(UrlTemplate template, String name, boolean required,
            ResourceTemplate aboutUrl, UrlTemplate predicate) {
        super(name, required, aboutUrl, predicate);
        this.template = template;
    }

    @Override
    public void initialize(String tableUri, List<String> header)
            throws MissingNameInHeader, InvalidTemplate {
        aboutUrl.initialize(tableUri, header);
        predicate.initialize(tableUri, header);
        template.initialize(tableUri, header);
        // Custom initialization method as we do not need index to row to
        // read data.
    }

    @Override
    public List<Resource> emit(RdfOutput outputConsumer,
            List<String> row, int rowNumber) throws LpException {
        final Resource s = aboutUrl.getResource(row, rowNumber);
        final IRI p = predicate.getUrl(row, rowNumber);
        final IRI o = template.getUrl(row, rowNumber);
        if (s == null || p == null || o == null) {
            return Collections.EMPTY_LIST;
        }
        outputConsumer.submit(s, p, o);
        return Arrays.asList(s);
    }

}
