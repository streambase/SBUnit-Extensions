package com.streambase.sbunit.ext;

import com.streambase.sb.Tuple;

public interface ErrorReport {
    /**
     * Add a successfully matched tuple to the report.
     * @param m  The matcher that succeeded
     * @param a  The tuple that it succeeded against
     */
    public void addMatchedTuple(TupleMatcher m, Tuple a);

    /**
     * Add a tuple that failed to match anything to the report.
     */
    public void addUnexpectedTuple(Tuple a);

    /**
     * Add a matcher that failed to match anything to the report.
     */
    public void addMissingMatcher(TupleMatcher m);

    /**
     * if this report contains errors, throw an error that summarizes
     * the report.
     */
    public void throwIfError() throws AssertionError;
}
