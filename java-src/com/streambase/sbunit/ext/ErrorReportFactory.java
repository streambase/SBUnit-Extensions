package com.streambase.sbunit.ext;

/**
 * A factory used to allow tests to control the errors they see.
 */
public interface ErrorReportFactory {
    
    /**
     * Create an empty error report.
     */
    public ErrorReport newErrorReport();
}
