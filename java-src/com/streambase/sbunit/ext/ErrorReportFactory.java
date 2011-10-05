package com.streambase.sbunit.ext;

/**
 * A factory used to control the {@link ErrorReport} used by a {@link StreamMatcher}
 */
public interface ErrorReportFactory {
    
    /**
     * create an empty error report
     * @param header  A human readable header to preface the error message
     */
    public ErrorReport newErrorReport(String header);
}
