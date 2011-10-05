package com.streambase.sbunit.ext;

import org.junit.ComparisonFailure;

/**
 * This class extends JUnit's {@link ComparisonFailure} to allow better customization
 * of its results.
 * <p/>
 * <li> {@link ErrorReport#getMessage()} is used for {@link Exception#getMessage()} </li>
 * <li> {@link ErrorReport#getExpectedMessage()} is used for the 
 *          <code>expected</code> string in JUnit </li>
 * <li> {@link ErrorReport#getActualMessage()} is used for the 
 *          <code>actual</code> string in JUnit </li>
 * <p/>
 * {@link ComparisonFailure} is not used directly, as it does not allow a 
 * custom message, but will instead build a message itself.  We override that
 * behavior here.
 */
@SuppressWarnings("serial")
public class ExpectTuplesFailure extends ComparisonFailure {
    
    private final ErrorReport report;

    public ExpectTuplesFailure(ErrorReport report) {
        super(report.getMessage(), report.getExpectedMessage(), report.getActualMessage());
        this.report = report;
    }
    
    @Override
    public String getMessage() {
        return report.getMessage();
    }
    
    /**
     * get the report that caused this failure
     */
    public ErrorReport getReport() {
        return report;
    }
}
