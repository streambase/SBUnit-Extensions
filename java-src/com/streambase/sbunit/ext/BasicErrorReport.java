package com.streambase.sbunit.ext;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;

import com.streambase.sb.Tuple;

public class BasicErrorReport implements ErrorReport {
    static final ErrorReportFactory FACTORY = new ErrorReportFactory() {
        @Override
        public ErrorReport newErrorReport() {
            return new BasicErrorReport();
        }
    };
    
    private final List<Tuple> matchedTuples = new ArrayList<Tuple>();
    private final List<TupleMatcher> matchedMatchers = new ArrayList<TupleMatcher>();
    private final List<Tuple> unmatchedTuples = new ArrayList<Tuple>();
    private final List<TupleMatcher> unmatchedMatchers = new ArrayList<TupleMatcher>();

    @Override
    public void addMatchedTuple(TupleMatcher m, Tuple t) {
        matchedMatchers.add(m);
        matchedTuples.add(t);
    }

    @Override
    public void addUnexpectedTuple(Tuple t) {
        unmatchedTuples.add(t);
    }

    @Override
    public void addMissingMatcher(TupleMatcher m) {
        unmatchedMatchers.add(m);
    }

    @Override
    public void throwIfError() throws AssertionError {
        if (unmatchedMatchers.size() > 0 || unmatchedTuples.size() > 0) {
            throw new AssertionError("I suck");
        }

    }

}
