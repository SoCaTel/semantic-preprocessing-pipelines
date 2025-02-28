package com.linkedpipes.etl.dataunit.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.dataunit.ManageableDataUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provide functionality for management of data and debug directories.
 */
public abstract class AbstractDataUnit implements ManageableDataUnit {

    protected final DataUnitConfiguration configuration;

    protected final Collection<String> sources;

    public AbstractDataUnit(
            DataUnitConfiguration configuration,
            Collection<String> sources) {
        this.configuration = configuration;
        this.sources = sources;
    }

    protected void initializeFromSource(
            Map<String, ManageableDataUnit> dataUnits) throws LpException {
        for (String iri : sources) {
            if (!dataUnits.containsKey(iri)) {
                throw new LpException("Missing input: {}", iri);
            }
            merge(dataUnits.get(iri));
        }
    }

    /**
     * Merge content of given data unit to this data unit.
     */
    protected abstract void merge(ManageableDataUnit dataUnit)
            throws LpException;

    @Override
    public String getBinding() {
        return configuration.getBinding();
    }

    @Override
    public String getIri() {
        return configuration.getResource();
    }

    @Override
    public void referenceContent(
            File source, File destination) throws LpException {
        saveDataDirectories(destination, loadDataDirectories(source));
        saveDebugDirectories(destination, loadDebugDirectories(source));
    }

    protected List<File> loadDataDirectories(File directory)
            throws LpException {
        return loadRelativePaths(directory, "data.json");
    }

    protected List<File> loadDebugDirectories(
            File directory) throws LpException {
        return loadRelativePaths(directory, "debug.json");
    }

    protected List<File> loadRelativePaths(
            File directory, String fileName) throws LpException {
        List<String> directoriesAsString = loadCollectionFromJson(
                new File(directory, fileName), String.class);
        return pathsAsFiles(directoriesAsString, directory);
    }

    protected <T> List<T> loadCollectionFromJson(
            File file, Class<T> type) throws LpException {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();
        try {
            return mapper.readValue(file,
                    typeFactory.constructCollectionType(List.class, type));
        } catch (IOException ex) {
            throw new LpException("Can't load directory list.", ex);
        }
    }

    private List<File> pathsAsFiles(Collection<String> paths, File root) {
        return paths.stream()
                .map(path -> new File(root, path))
                .collect(Collectors.toList());
    }

    protected void saveDataDirectories(
            File directory, List<File> directories) throws LpException {
        saveRelativePaths(directory, "data.json", directories);
    }

    protected void saveRelativePaths(
            File directory, String fileName, List<File> directories)
            throws LpException {
        List<String> directoriesAsString =
                relativeAsString(directories, directory);
        saveCollectionAsJson(new File(directory, fileName),
                directoriesAsString);
    }

    protected void saveCollectionAsJson(
            File file, Collection<String> collection) throws LpException {
        ObjectMapper mapper = new ObjectMapper();
        file.getParentFile().mkdirs();
        try {
            mapper.writeValue(file, collection);
        } catch (IOException ex) {
            throw new LpException("Can't save directory list.", ex);
        }
    }

    private List<String> relativeAsString(
            Collection<File> paths, File root) {
        Path rootPath = root.getAbsoluteFile().toPath();
        return paths.stream().map(
                (file) -> asRelativePath(rootPath, file).toString()
        ).collect(Collectors.toList());
    }

    private Path asRelativePath(Path base, File file) {
        return base.relativize(file.getAbsoluteFile().toPath());
    }

    protected void saveDebugDirectories(
            File directory, List<File> directories) throws LpException {
        saveRelativePaths(directory, "debug.json", directories);
    }

}
