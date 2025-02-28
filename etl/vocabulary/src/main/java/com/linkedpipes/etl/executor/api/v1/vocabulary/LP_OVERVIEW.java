package com.linkedpipes.etl.executor.api.v1.vocabulary;

public final class LP_OVERVIEW {

    private static final String PREFIX =
            "http://etl.linkedpipes.com/ontology/";

    public static final String OVERVIEW = PREFIX + "ExecutionOverview";

    public static final String HAS_PIPELINE = PREFIX + "pipeline";

    public static final String HAS_EXECUTION = PREFIX + "execution";

    public static final String HAS_START = PREFIX + "execution/start";

    public static final String HAS_END = PREFIX + "execution/end";

    public static final String HAS_STATUS = PREFIX + "status";

    public static final String HAS_LAST_CHANGE = PREFIX + "lastChange";

    public static final String HAS_PIPELINE_PROGRESS =
            PREFIX + "pipelineProgress";

    public static final String HAS_PROGRESS_TOTAL =
            PREFIX + "execution/componentToExecute";

    public static final String HAS_PROGRESS_CURRENT =
            PREFIX + "execution/componentFinished";

    public static final String HAS_PROGRESS_TOTAL_MAP =
            PREFIX + "execution/componentToMap";

    public static final String HAS_PROGRESS_MAPPED =
            PREFIX + "execution/componentMapped";

    public static final String HAS_PROGRESS_EXECUTED =
            PREFIX + "execution/componentExecuted";

    public static final String HAS_DIRECTORY_SIZE = PREFIX + "directorySize";

}
