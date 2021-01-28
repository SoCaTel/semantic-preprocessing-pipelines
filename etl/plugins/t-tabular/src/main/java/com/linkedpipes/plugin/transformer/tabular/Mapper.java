package com.linkedpipes.plugin.transformer.tabular;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.vocabulary.XSD;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * https://www.w3.org/TR/2015/REC-csv2rdf-20151217/#bib-tabular-data-model
 *
 * @author Škoda Petr
 */
class Mapper {

    /**
     * Output data mode.
     */
    public enum Mode {

        STANDARD,
        MINIMAL
    }

    /**
     * Valued factory used to create RDF data.
     */
    private static final ValueFactory VALUE_FACOTORY
            = SimpleValueFactory.getInstance();

    private Mode outputMode = Mode.MINIMAL;

    /**
     * Used output consumer.
     */
    private final StatementConsumer consumer;

    /**
     * Represent a root resource for group of tables.
     */
    private Resource G = null;

    /**
     * Represent a root resource for a table.
     */
    private Resource T = null;

    /**
     * Row number in source file.
     */
    private int rowNumber = 0;

    /**
     * Processed row number.
     */
    private int processedRowNumber = 0;

    private final TabularConfiguration configuration;

    /**
     * Columns as configured by the user.
     */
    private final List<ColumnAbstract> columns;

    /**
     * Column used during parsing.
     */
    private List<ColumnAbstract> usedColumns;

    private String tableResource;

    private final ExceptionFactory exceptionFactory;

    /**
     * Does not call any other method on {@link StatementConsumer}
     * than onRowStart, onRowEnd and submit for new statement.
     *
     * @param consumer
     * @param configuration
     * @param columns
     */
    Mapper(StatementConsumer consumer, TabularConfiguration configuration,
            List<ColumnAbstract> columns, ExceptionFactory exceptionFactory) {
        this.consumer = consumer;
        this.configuration = configuration;
        this.columns = columns;
        if (configuration.isStandardMode()) {
            this.outputMode = Mode.STANDARD;
        } else {
            this.outputMode = Mode.MINIMAL;
        }
        this.exceptionFactory = exceptionFactory;
    }

    public void initialize(String tableGroupUri) throws LpException {
        if (this.outputMode == Mode.STANDARD) {
            // 1
            if (tableGroupUri == null) {
                G = VALUE_FACOTORY.createBNode();
            } else {
                G = VALUE_FACOTORY.createIRI(tableGroupUri);
            }
            // 2
            consumer.submit(G, RDF.TYPE, CSVW.TABLE_GROUP);
            // 3
            // In standard mode only, emit the triples generated by running
            // the algorithm specified in section 6. JSON-LD to RDF over any
            // notes and non-core annotations specified for the group of tables,
            // with node G as an initial subject, the notes or non-core
            // annotation as property, and the value
            // of the notes or non-core annotation as value.
        }
    }

    /**
     * @param tableResource Table resource if null blank node is used.
     * @param tableUri Source table URI.
     * @return
     */
    public boolean onTableStart(String tableResource, String tableUri)
            throws LpException {
        this.tableResource = tableResource;
        // Rest row number.
        rowNumber = 0;
        processedRowNumber = 0;
        // 4 - we never skip table
        if (outputMode == Mode.STANDARD) {
            // 4.1
            if (tableResource == null) {
                T = VALUE_FACOTORY.createBNode();
            } else {
                T = VALUE_FACOTORY.createIRI(tableResource);
            }
            // 4.2
            consumer.submit(G, CSVW.HAS_TABLE, T);
            // 4.3
            consumer.submit(T, RDF.TYPE, CSVW.TABLE);
            // 4.4
            if (tableUri != null) {
                consumer.submit(G, CSVW.HAS_URL,
                        VALUE_FACOTORY.createIRI(tableUri));
            }
            // 4.5
            // In standard mode only, emit the triples generated by running
            // the algorithm specified in section 6. JSON-LD to RDF over any
            // notes and non-core annotations specified for the table, with
            // node T as an initial subject, the notes or non-core
            // annotation as property, and the value
            // of the notes or non-core annotation as value.
        }
        return false;
    }

    /**
     * Must be called before {@link #onRow(java.util.List)}.
     *
     * @param header Null if there is no header.
     */
    public void onHeader(List<String> header)
            throws ColumnAbstract.MissingNameInHeader, InvalidTemplate,
            LpException {
        usedColumns = new ArrayList<>(columns.size());
        if (configuration.isFullMapping()) {
            usedColumns.addAll(ColumnFactory.createColumList(
                    configuration, header, exceptionFactory));
        } else {
            // Use user given.
            usedColumns.addAll(columns);
        }
        for (ColumnAbstract column : usedColumns) {
            column.initialize(tableResource, header);
        }
    }

    /**
     * @param row Row from the CSV file.
     * @return True if next line should be processed if it exists.
     */
    public boolean onRow(List<String> row) throws UnsupportedEncodingException,
            LpException, ColumnAbstract.MissingColumnValue {
        consumer.onRowStart();
        rowNumber++;
        if (rowNumber <= configuration.getDialect().getSkipRows()) {
            return true;
        }
        processedRowNumber++;
        // 4.6
        final Resource R;
        if (outputMode == Mode.STANDARD) {
            // 4.6.1
            R = VALUE_FACOTORY.createBNode();
            // 4.6.2
            consumer.submit(T, CSVW.HAS_ROW, R);
            // 4.6.3
            consumer.submit(R, RDF.TYPE, CSVW.ROW);
            // 4.6.4
            consumer.submit(R, CSVW.HAS_ROWNUM,
                    VALUE_FACOTORY.createLiteral(
                            Integer.toString(rowNumber),
                            VALUE_FACOTORY.createIRI(XSD.INTEGER)));
            // 4.6.5
            //if (configuration.rows.size() > rowNumber) {
            //    final TabularDataModel.Rows rows
            //          = configuration.rows.get(rowNumber);
            final String rowIri = T.stringValue() + "#"
                    + Integer.toString(rowNumber);
            consumer.submit(R, CSVW.HAS_URL, VALUE_FACOTORY.createIRI(rowIri));
            // 4.6.6 - Add titles.
            // We do not support titles.

            // 4.6.7
            // In standard mode only, emit the triples generated by running
            // the algorithm specified in section 6. JSON-LD to RDF over any
            // non-core annotations specified for the row, with node R as
            // an initial subject, the non-core annotation as property, and the
            // value of the non-core annotation as value.
        } else {
            R = null;
        }

        // For each specified column.
        for (ColumnAbstract column : usedColumns) {
            // If row is set then add reference to columns.
            if (R == null) {
                column.emit(consumer, row, rowNumber);
            } else {
                // In standard mode add links from table.
                for (Resource resource : column
                        .emit(consumer, row, rowNumber)) {
                    consumer.submit(R, CSVW.HAS_DESCRIBES, resource);
                }
            }
            // 4.6.8 - Create subject or use blank node if about URL is not set.
//            final Resource Sdef = valueFactory.createBNode();
            // 4.6.1
//            final Resource S = column.getSubject();
//            if (column.aboutUrl != null) {
//                S = valueFactory.createIRI(column.aboutUrl);
//            } else {
//                S = Sdef;
//            }
            // 4.6.8.2
//            if (R != null) {
//                consumer.submit(R, CSVW.HAS_DESCRIBES, S);
//            }
            // 4.6.8.3  - Construct predefined predicate or return default one.
//            final IRI P = column.getPredicate();
//            if (column.propertyUrl != null) {
//                P = valueFactory.createIRI(column.propertyUrl);
//            } else {
//                // Construct
//                // TODO Encode for URL!
//                P = valueFactory.createIRI(configuration.url + "#"
//                      + URLEncoder.encode(column.name, "UTF-8"));
//            }
            // 4.6.8.4
            // 4.6.8.5 - else, if value is list and cellOrdering == true
            // 4.6.8.6 - else, if value is list
            // 4.6.8.7 - else if cellValue is not null
//            for (Value value : column.getValues()) {
//                consumer.submit(S, P, value);
//            }
        }
        consumer.onRowEnd();

        // True if we should continue with reading of the next line.
        return !(0 < configuration.getRowLimit()
                && configuration.getRowLimit() <= processedRowNumber);
    }

    public void onTableEnd() {
    }

}
