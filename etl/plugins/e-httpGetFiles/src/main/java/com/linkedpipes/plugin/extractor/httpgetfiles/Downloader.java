package com.linkedpipes.plugin.extractor.httpgetfiles;

import com.linkedpipes.etl.executor.api.v1.LpException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

class Downloader {

    public static class Task {

        private final String getSourceUrl;

        private final File targetFile;

        private final Map<String, String> headers;

        private final Integer timeOut;

        public Task(
                String getSourceUrl, File targetFile,
                Map<String, String> headers, Integer timeOut) {
            this.getSourceUrl = getSourceUrl;
            this.targetFile = targetFile;
            this.headers = headers;
            this.timeOut = timeOut;
        }

        public String getSourceUrl() {
            return getSourceUrl;
        }

        public File getTargetFile() {
            return targetFile;
        }

        public Map<String, String> getHeader() {
            return headers;
        }

        public Integer getTimeOut() {
            return timeOut;
        }

    }

    public static class Configuration {

        private final boolean manualFollowRedirect;

        private final boolean logDetail;

        private final boolean encodeUrl;

        private final boolean useUtf8ForRedirect;

        public Configuration(
                boolean manualFollowRedirect,
                boolean logDetail,
                boolean encodeUrl,
                boolean useUtf8ForRedirect) {
            this.manualFollowRedirect = manualFollowRedirect;
            this.logDetail = logDetail;
            this.encodeUrl = encodeUrl;
            this.useUtf8ForRedirect = useUtf8ForRedirect;
        }

    }

    public static final int HTTP_TEMPORARY_REDIRECT = 307;

    private static final Logger LOG = LoggerFactory.getLogger(Downloader.class);

    private final Task toDownload;

    private final Configuration configuration;

    private final HttpRequestReport requestReport;

    public Downloader(Task toDownload, Configuration configuration, HttpRequestReport requestReport) {
        this.toDownload = toDownload;
        this.configuration = configuration;
        this.requestReport = requestReport;
    }

    public void download() throws IOException, LpException {
        LOG.info("Downloading: {} -> {}", toDownload.getSourceUrl(),
                toDownload.getTargetFile().toString());
        //
        URL url = createUrl(toDownload.getSourceUrl());
        HttpURLConnection connection = null;
        Date startTime = new Date();
        try {
            connection = connect(url);
            if (configuration.logDetail) {
                requestReport.reportHeaderResponse(connection);
            }
            checkResponseCode(connection);
            saveContentToFile(connection, toDownload.getTargetFile());
        } catch (RuntimeException ex) {
            throw new IOException("Can't download file.", ex);
        } finally {
            long downloadTime = (new Date()).getTime() - startTime.getTime();
            LOG.debug("Processing of: {} takes: {} ms",
                    toDownload.getSourceUrl(), downloadTime);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private URL createUrl(String stringAsUrl) throws IOException {
        if (configuration.encodeUrl) {
            try {
                stringAsUrl = (new URL(stringAsUrl)).toURI().toASCIIString();
            } catch (URISyntaxException ex) {
                throw new IOException("Can't convert to URI:" + stringAsUrl, ex);
            }
        }
        return new URL(stringAsUrl);
    }

    private HttpURLConnection connect(URL target) throws IOException {
        HttpURLConnection connection = createConnection(target);
        if (configuration.manualFollowRedirect) {
            connection.setInstanceFollowRedirects(false);
            return resolveRedirects(connection);
        } else {
            connection.setInstanceFollowRedirects(true);
            return connection;
        }
    }

    private HttpURLConnection createConnection(URL target) throws IOException {
        HttpURLConnection connection =
                (HttpURLConnection) target.openConnection();
        setHeaders(connection);
        setTimeOut(connection);
        return connection;
    }

    private void setHeaders(HttpURLConnection connection) {
        Map<String, String> headers = toDownload.getHeader();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private void setTimeOut(HttpURLConnection connection) {
        Integer timeOut = toDownload.getTimeOut();
        if (timeOut != null) {
            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);
        }
    }

    private HttpURLConnection resolveRedirects(
            HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        while (isResponseRedirect(responseCode)) {
            String location = connection.getHeaderField("Location");
            if (configuration.useUtf8ForRedirect) {
                location = new String(location.getBytes("ISO-8859-1"), "UTF-8");
            }
            connection.disconnect();
            LOG.debug("Resolved redirect to: {}", location);
            connection = createConnection(createUrl(location));
            responseCode = connection.getResponseCode();
        }
        return connection;
    }

    private boolean isResponseRedirect(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                responseCode == HTTP_TEMPORARY_REDIRECT;
    }

    private void checkResponseCode(HttpURLConnection connection)
            throws IOException {
        int responseCode = connection.getResponseCode();

        if (responseCode < 200 || responseCode > 299) {
            IOException ex = new IOException(
                    responseCode + " : " + connection.getResponseMessage());
            LOG.error("Can't download file: {}", toDownload.getSourceUrl(), ex);
            throw ex;
        }
    }

    private void saveContentToFile(HttpURLConnection connection, File file)
            throws IOException {
        try (InputStream inputStream = connection.getInputStream()) {
            FileUtils.copyInputStreamToFile(inputStream, file);
        }
    }

}
