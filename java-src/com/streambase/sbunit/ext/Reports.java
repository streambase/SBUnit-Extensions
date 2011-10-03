package com.streambase.sbunit.ext;

/**
 * Factory and utility methods for {@link ErrorReport} and 
 * {@link ErrorReportFactory} classes.
 */
public class Reports {
    
    /**
     * @return A factory that produces {@link BasicErrorReport} instances
     */
    public static ErrorReportFactory getBasicReportFactory() {
        return BasicErrorReport.FACTORY;
    }

}
