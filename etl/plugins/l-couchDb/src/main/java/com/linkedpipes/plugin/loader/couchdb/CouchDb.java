package com.linkedpipes.plugin.loader.couchdb;

import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Collection;

public class CouchDb {

    private static final Logger LOG = LoggerFactory.getLogger(CouchDb.class);

    private final String server;

    private final ExceptionFactory exceptionFactory;

    private String authorizationHeader = null;

    public CouchDb(String server, ExceptionFactory exceptionFactory) {
        if (server.endsWith("/")) {
            this.server = server;
        } else {
            this.server = server + "/";
        }
        this.exceptionFactory = exceptionFactory;
    }

    public void setCredentials(String userName, String password) {
        String auth = userName + ":" + password;
        byte[] authBytes = auth.getBytes(Charset.forName("ISO-8859-1"));
        byte[] encodedAuth = Base64.encodeBase64(authBytes);
        this.authorizationHeader = "Basic " + new String(encodedAuth);
    }

    public void deleteDatabase(String database) throws LpException {
        String url = this.server + database;
        int responseCode;
        try {
            responseCode = executeRequest(url, "DELETE");
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't delete database: {}",
                    database, ex);
        }
        if (responseCode == 404) {
            // Already deleted.
            return;
        }
        if (responseCode < 200 || responseCode > 299) {
            throw exceptionFactory.failure(
                    "Request failed with status: {}", responseCode);
        }
    }

    public void createDatabase(String database) throws LpException {
        String url = this.server + database;
        int responseCode;
        try {
            responseCode = executeRequest(url, "PUT");
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't create database: {}",
                    database, ex);
        }
        if (responseCode < 200 || responseCode > 299) {
            throw exceptionFactory.failure(
                    "Request failed with status: {}", responseCode);
        }
    }

    private int executeRequest(String url, String method)
            throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = createHttpConnection(url, method);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode > 399) {
                tryLogErrorResponse(connection);
            }
            return responseCode;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void tryLogErrorResponse(HttpURLConnection connection) {
        try (InputStream errorStream = connection.getErrorStream()) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(errorStream, writer, "utf-8");
            LOG.error("Error stream: {}", writer.toString());
        } catch (Exception ex) {
            // Do nothing.
        }
    }

    private HttpURLConnection createHttpConnection(String url, String method)
            throws IOException {
        URLConnection connection = (new URL(url)).openConnection();
        connection.setRequestProperty("Accept", "*/*");
        addAuthorizationHeader(connection);
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestMethod(method);
        return httpConnection;
    }

    private void addAuthorizationHeader(URLConnection connection) {
        if (authorizationHeader == null) {
            return;
        }
        connection.setRequestProperty("Authorization", authorizationHeader);
    }

    public void uploadDocuments(String database, Collection<File> documents)
            throws LpException {
        HttpURLConnection connection = null;
        try {
            connection = createBulkLoadConnection(database);
            connection.connect();
            try (OutputStream stream = connection.getOutputStream()) {
                writeFilesAsBulkDocument(stream, documents);
                stream.flush();
            }
            checkStatus(connection);
        } catch (IOException ex) {
            throw exceptionFactory.failure("Can't open connection.", ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection createBulkLoadConnection(String database)
            throws IOException {
        String url = this.server + database + "/_bulk_docs";
        URLConnection connection = (new URL(url)).openConnection();
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestMethod("POST");
        httpConnection.setDoOutput(true);
        httpConnection.addRequestProperty("Content-Type", "application/json");
        return httpConnection;
    }

    private void writeFilesAsBulkDocument(
            OutputStream stream, Collection<File> files) throws IOException {
        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(stream, "UTF-8"),
                true);
        writer.write("{\"docs\":[");
        writer.flush();
        boolean first = true;
        for (File file : files) {
            if (!first) {
                writer.write(",");
                writer.flush();
            }
            first = false;
            FileUtils.copyFile(file, stream);
        }
        writer.write("]}");
        writer.flush();
    }

    private void checkStatus(HttpURLConnection connection) throws LpException {
        int responseCode;
        try {
            responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return;
            }
        } catch (IOException ex) {
            // This can happen if the query is too big, ie. we split the query.
            throw exceptionFactory.failure("Can't get response code.", ex);
        }
        StringWriter error = new StringWriter();
        try (InputStream stream = connection.getErrorStream()) {
            if (stream != null) {
                IOUtils.copy(stream, error, "UTF-8");
            }
        } catch (IOException ex) {
            // Ignore.
        }
        throw exceptionFactory.failure(
                "Can't execute request: {}\nError: {}",
                responseCode, error);
    }

}


