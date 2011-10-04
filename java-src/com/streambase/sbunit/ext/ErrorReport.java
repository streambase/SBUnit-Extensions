package com.streambase.sbunit.ext;

import java.rmi.UnexpectedException;
import java.util.List;

import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.StreamMatcher.ExtraTuples;

/**
 * An interface used to collect and report errors found by a 
 * {@link StreamMatcher}.
 */
public interface ErrorReport {
    
    /**
     * Add a successfully matched tuple to the report.
     * @param m  The matcher that succeeded
     * @param a  The tuple that it succeeded against
     */
    public void addFoundTuple(TupleMatcher m, Tuple a);

    /**
     * Add a tuple that failed to match anything to the report.
     */
    public void addUnexpectedTuple(Tuple a);

    /**
     * Add a matcher that failed to match anything to the report.
     */
    public void addMissingMatcher(TupleMatcher m);
    
    
    /**
     * @return the tuple matchers that matched successfully
     */
    public List<TupleMatcher> getFoundMatchers();
    
    /**
     * @return the tuples that matched successfully
     */
    public List<Tuple> getFoundTuples();
    
    /**
     * @return the tuple matchers that failed to match.
     */
    public List<TupleMatcher> getMissingMatchers();
    
    /**
     * @return the tuples that arrived unexpectedly
     */
    public List<Tuple> getUnexpectedTuples();
    

    /**
     * If this report contains errors, throw an error that summarizes
     * the report.
     * @param extra specified handling of unexpected tuples.
     */
    public void throwIfError(ExtraTuples extra) throws ExpectTuplesFailure;

    /**
     * Used in error reporting this is the message that will be shown in the 
     * stack trace.
     */
    public String getMessage();

    /**
     * Inside Eclipse the JUnit view will present a graphical diff of
     * {@link #getExpectedMessage()} and {@link #getActualMessage()}.
     */
    public String getExpectedMessage();

    /**
     * Inside Eclipse the JUnit view will present a graphical diff of
     * {@link #getExpectedMessage()} and {@link #getActualMessage()}.
     */
    public String getActualMessage();
}
