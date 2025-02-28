package com.linkedpipes.plugin.transformer.tabularuv.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for {@link ParserCsv}.
 */
public class ParserCsvConfig {

    private static final Logger LOG = LoggerFactory.getLogger(
            ParserCsvConfig.class);

    final String quoteChar;

    final char delimiterChar;

    final String encoding;

    final int numberOfStartLinesToIgnore;

    final Integer rowLimit;

    final boolean hasHeader;

    final boolean checkStaticRowCounter;

    public ParserCsvConfig(String quoteChar, char delimiterChar,
            String encoding, Integer numberOfStartLinesToIgnore,
            Integer rowLimit, boolean hasHeader,
            boolean checkStaticRowCounter) {
        if (quoteChar == null) {
            this.quoteChar = "\"";
            LOG.warn("Property quoteChar is not set, '{}' is used as default.",
                    this.quoteChar);
        } else {
            this.quoteChar = quoteChar;
        }
        this.delimiterChar = delimiterChar;
        if (encoding == null) {
            this.encoding = "UTF-8";
            LOG.warn("Property encoding is not set, '{}' is used as default.",
                    this.encoding);
        } else {
            this.encoding = encoding;
        }
        if (numberOfStartLinesToIgnore == null) {
            this.numberOfStartLinesToIgnore = 0;
        } else {
            this.numberOfStartLinesToIgnore = numberOfStartLinesToIgnore;
        }
        this.rowLimit = rowLimit;
        this.hasHeader = hasHeader;
        this.checkStaticRowCounter = checkStaticRowCounter;
    }

}
