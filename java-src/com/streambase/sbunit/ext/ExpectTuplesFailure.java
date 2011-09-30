package com.streambase.sbunit.ext;

import org.junit.ComparisonFailure;

@SuppressWarnings("serial")
public class ExpectTuplesFailure extends ComparisonFailure {
    
    private final ErrorReport report;

    public ExpectTuplesFailure(ErrorReport report) {
        super(report.getMessage(), report.getExpectedMessage(), report.getActualMessage());
        this.report = report;
    }
    
    public String getMessage() {
        return report.getMessage();
    }
    
    public ErrorReport getReport() {
        return report;
    }
}
