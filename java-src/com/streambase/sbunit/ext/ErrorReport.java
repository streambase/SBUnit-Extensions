package com.streambase.sbunit.ext;

import java.util.List;

import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.StreamMatcher.ExtraTuples;

/**
 * An interface used to collect and report errors found by a 
 * {@link StreamMatcher}.
 */
public interface ErrorReport {
    
    /**
     * add a successfully matched tuple to the report.
     * @param m  The matcher that succeeded
     * @param a  The tuple that it succeeded against
     */
    public void addFoundTuple(TupleMatcher m, Tuple a);

    /**
     * add a tuple that failed to match anything to the report.
     */
    public void addUnexpectedTuple(Tuple a);

    /**
     * add a matcher that failed to match anything to the report.
     */
    public void addMissingMatcher(TupleMatcher m);
    
    /**
     * get the tuple matchers that matched successfully
     */
    public List<TupleMatcher> getFoundMatchers();
    
    /**
     * get the tuples that matched successfully
     */
    public List<Tuple> getFoundTuples();
    
    /**
     * get the tuple matchers that failed to match.
     */
    public List<TupleMatcher> getMissingMatchers();
    
    /**
     * get the tuples that arrived unexpectedly
     */
    public List<Tuple> getUnexpectedTuples();
    

    /**
     * throw an error that summarizes the report, if it contains errors.
     * @param extra Should unexpected tuples be treated as errors
     */
    public void throwIfError(ExtraTuples extra) throws ExpectTuplesFailure;

    /**
     * get the message shown in the stack trace, when reporting a failure.
     */
    public String getMessage();

    /**
     * inside Eclipse the JUnit view will present a graphical diff of
     * {@link #getExpectedMessage()} and {@link #getActualMessage()}.
     */
    public String getExpectedMessage();

    /**
     * inside Eclipse the JUnit view will present a graphical diff of
     * {@link #getExpectedMessage()} and {@link #getActualMessage()}.
     */
    public String getActualMessage();
}
