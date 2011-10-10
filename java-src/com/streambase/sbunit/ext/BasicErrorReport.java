package com.streambase.sbunit.ext;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleJSONUtil;
import com.streambase.sbunit.ext.StreamMatcher.ExtraTuples;

/**
 * A simple English error report that uses JSON to represent Tuples and
 * {@link ExpectTuplesFailure} for reporting errors.
 */
public class BasicErrorReport implements ErrorReport {
    static final ErrorReportFactory FACTORY = new ErrorReportFactory() {
        @Override
        public ErrorReport newErrorReport(String header) {
            return new BasicErrorReport(header);
        }
    };
    
    private final List<Tuple> foundTuples = new ArrayList<Tuple>();
    private final List<TupleMatcher> foundMatchers = new ArrayList<TupleMatcher>();
    private final List<Tuple> unexpectedTuples = new ArrayList<Tuple>();
    private final List<TupleMatcher> missingMatchers = new ArrayList<TupleMatcher>();
    private final String header;
    
    /**
     * Create an empty error report.
     * @param header  A human readable header to preface the error message
     */
    public BasicErrorReport(String header) {
        this.header = header;
    }

    @Override
    public void addFoundTuple(TupleMatcher m, Tuple t) {
        foundMatchers.add(m);
        foundTuples.add(t);
    }
    
    @Override
    public List<Tuple> getFoundTuples() {
        return foundTuples;
    }

    @Override
    public List<TupleMatcher> getFoundMatchers() {
        return foundMatchers;
    }

    @Override
    public void addUnexpectedTuple(Tuple t) {
        unexpectedTuples.add(t);
    }

    @Override
    public List<Tuple> getUnexpectedTuples() {
        return unexpectedTuples;
    }

    @Override
    public void addMissingMatcher(TupleMatcher m) {
        missingMatchers.add(m);
    }
    
    @Override
    public List<TupleMatcher> getMissingMatchers() {
        return missingMatchers;
    }


    @Override
    public void throwIfError(ExtraTuples extra) throws AssertionError {
        if (missingMatchers.size() > 0) {
            throw new ExpectTuplesFailure(this);
        } 
        if (extra == ExtraTuples.ERROR && unexpectedTuples.size() > 0) {
            throw new ExpectTuplesFailure(this);
        } 
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(this.header);
        
        String missing = makeMissingMessage();
        if (!missing.isEmpty()) {
            sb.append("\nMissing tuples:\n");
            sb.append(missing);
        }
        
        String unexpeted = makeUnexpectedMessage();
        if (!unexpeted.isEmpty()) {
            sb.append("\nUnexpected tuples:\n");
            sb.append(unexpeted);
        }
        
        String found = makeFoundMessage();
        if (!found.isEmpty()) {
            sb.append("\nFound tuples:\n");
            sb.append(found);
        }
        return sb.toString();
    }
    
    @Override
    public String getActualMessage() {
        return makeUnexpectedMessage();
    }
    
    @Override
    public String getExpectedMessage() {
        return makeMissingMessage();
    }
    
    
    /**
     * Subclasses wishing to customize the "Missing tuples" section of the
     * error message should override this method. 
     */
    protected String makeMissingMessage() {
        StringBuilder sb = new StringBuilder();
        if (!missingMatchers.isEmpty()) { 
            Gson gson = new Gson();
            JsonElement e = missingMatchers.get(0).describe(gson);
            sb.append(gson.toJson(e));
            for (int i = 1; i < missingMatchers.size(); ++i) {
                e = missingMatchers.get(i).describe(gson);
                sb.append("\n").append(gson.toJson(e));
            }
        }
        return sb.toString();
    }
    
    /**
     * Subclasses wishing to customize the "Unexpected tuples" section of the
     * error message should override this method. 
     */
    protected String makeUnexpectedMessage() {
        StringBuilder sb = new StringBuilder();
        if (!unexpectedTuples.isEmpty()) { 
            sb.append(formatTupleForMessage(unexpectedTuples.get(0)));
            for (int i = 1; i < unexpectedTuples.size(); ++i) {
                sb.append("\n");
                sb.append(formatTupleForMessage(unexpectedTuples.get(i)));
            }
        }
        return sb.toString();
    }
    
    /**
     * Subclasses wishing to customize the "Found tuples" section of the
     * error message should override this method. 
     */
    protected String makeFoundMessage() {
        StringBuilder sb = new StringBuilder();
        if (!foundTuples.isEmpty()) { 
            sb.append(formatTupleForMessage(foundTuples.get(0)));
            for (int i = 1; i < foundTuples.size(); ++i) {
                sb.append("\n");
                sb.append(formatTupleForMessage(foundTuples.get(i)));
            }
        }
        return sb.toString();
    }
    
    /**
     * Subclasses wishing to customize the display of Tuples in the
     * error message should override this method. 
     */
    protected String formatTupleForMessage(Tuple t) {
        return TupleJSONUtil.toJSONMapString(t);
    }
}
