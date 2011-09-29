package com.streambase.sbunit.ext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.streambase.sb.StreamBaseException;
import com.streambase.sb.Tuple;
import com.streambase.sb.unittest.Dequeuer;

public class StreamMatcher {
    public static enum ExtraTuples {
        INGORE,
        ERROR
    }
    
    public static enum Ordering {
        ORDERED,
        UNORDERED
    }
    
    private final long timeout;
    private final TimeUnit timeUnit;
    private final ExtraTuples extras;
    private final Ordering ordering;
    private final Dequeuer dequeuer;
    private final ErrorReportFactory reportFactory;
    
    private StreamMatcher(Dequeuer dequeuer, long timeout, TimeUnit timeUnit, 
            ExtraTuples extras, Ordering ordering, ErrorReportFactory reporter) {
        assert timeout >= 0;
        this.dequeuer = dequeuer;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.extras = extras;
        this.ordering = ordering;
        this.reportFactory = reporter;
    }
    
    /**
     * Create a default {@link StreamMatcher} for the stream provided by {@link Dequeuer}.
     */
    public static StreamMatcher on(Dequeuer dequeuer) {
        return new StreamMatcher(dequeuer, 
                Dequeuer.DEFAULT_TIMEOUT, Dequeuer.DEFAULT_TIMEOUT_UNIT, 
                ExtraTuples.ERROR, Ordering.ORDERED,
                Reports.getBasicReportFactory());
    }
    
    /**
     * Create an identical {@link StreamMatcher} except that will handle unexpected Tuples 
     * within expect*() calls according to the provided {@link ExtraTuples}
     */
    public StreamMatcher onExtra(ExtraTuples extras) {
        return new StreamMatcher(dequeuer, timeout, timeUnit, extras, ordering, reportFactory);
    }
    
    /**
     * Create an identical {@link StreamMatcher} except that will handle order of tuples
     * within expect*() calls according to the provided {@link Ordering}.
     */
    public StreamMatcher ordering(Ordering ordering) {
        return new StreamMatcher(dequeuer, timeout, timeUnit, extras, ordering, reportFactory);
    }
    
    /**
     * Create an identical {@link StreamMatcher} except that will use the provided timeout
     * within expect*() calls.
     */
    public StreamMatcher timeout(long timeout, TimeUnit timeUnit) {
        return new StreamMatcher(dequeuer, timeout, timeUnit, extras, ordering, reportFactory);
    }
    
    /**
     * Create an identical {@link StreamMatcher} except that will use the provided reporter
     * within expect*() calls.
     */
    public StreamMatcher reporting(ErrorReportFactory reporter) {
        return new StreamMatcher(dequeuer, timeout, timeUnit, extras, ordering, reporter);
    }
    
    
    /**
     * Expect tuples that match each of the given {@link TupleMatcher}s
     * @throws StreamBaseException if an internal error occurs
     * @throws AssertionError if the expected tuples do not match
     */
    public void expectTuples(TupleMatcher... matchers) throws StreamBaseException {
        expectTuples(Arrays.asList(matchers));
    }
    
    /**
     * Expect tuples that match each of the given {@link TupleMatcher}s
     * @throws StreamBaseException if an internal error occurs
     * @throws AssertionError if the expected tuples do not match
     */
    public void expectTuples(List<? extends TupleMatcher> matchers) throws StreamBaseException {
        if (ordering == Ordering.ORDERED) {
            expectOrderedImpl(matchers);
        } else {
            expectUnorderedImpl(matchers);
        }
    }
    
    /**
     * Expect a single tuple that matches the provided {@link TupleMatcher}
     * @throws StreamBaseException if an internal error occurs
     * @throws AssertionError if the expected tuples do not match
     */
    public void expectTuple(TupleMatcher m) throws StreamBaseException {
        expectOrderedImpl(Collections.singletonList(m));
    }
    
    private void expectUnorderedImpl(List<? extends TupleMatcher> expected) throws StreamBaseException {
        // We use a simple n^2 algorithm to find the differences between the two sets
        //
        // for each expected tuple, scan the actual list
        //  - when a match is found, remove the tuples from both lists by marking them as 'null'
        // afterwards 
        //  - non-null elements in the expect lists were expected but missing
        //  - non-null elements in the actual lists were unexpected extras
        
        long now = System.currentTimeMillis();
        long finish = now + timeUnit.toMillis(timeout);
        
        ErrorReport report = reportFactory.newErrorReport();
        
        TupleMatcher[] exp = expected.toArray(new TupleMatcher[expected.size()]);
        int remaining = exp.length;
        
        do {
            List<Tuple> actual = dequeuer.dequeue(remaining, finish - now, TimeUnit.MILLISECONDS);
            
            NEXT_TUPLE: for (Tuple a : actual) {
                for (int i = 0; i < exp.length; ++i) {
                    TupleMatcher m = exp[i];
                    if (m != null && m.matches(a)) {
                        report.addMatchedTuple(m, a);
                        exp[i] = null;
                        --remaining;
                        continue NEXT_TUPLE;
                    }
                }
                report.addUnexpectedTuple(a);
            }
            now = System.currentTimeMillis();
        } while (remaining > 0 && extras == ExtraTuples.INGORE && now <= finish);
        
        for (TupleMatcher m : exp) {
            if (m != null) {
                report.addMissingMatcher(m);
            }
        }
        report.throwIfError();
    }
    
    private void expectOrderedImpl(List<? extends TupleMatcher> matchers) throws StreamBaseException {
        long now = System.currentTimeMillis();
        long finish = now + timeUnit.toMillis(timeout);
        
        ErrorReport report = reportFactory.newErrorReport();
        int index = 0;
        do {
            List<Tuple> actual = dequeuer.dequeue(matchers.size() - index, finish - now, TimeUnit.MILLISECONDS);
            for (Tuple a : actual) {
                TupleMatcher m = matchers.get(index);
                
                boolean isExpected = m.matches(a);
                if (isExpected) {
                    report.addMatchedTuple(m, a);
                    ++index;
                } else if (ExtraTuples.ERROR == extras) {
                    report.addUnexpectedTuple(a);
                    report.addMissingMatcher(m);
                    ++index;
                } else {
                    report.addUnexpectedTuple(a);
                }
            }
            now = System.currentTimeMillis();
        } while (index < matchers.size() && extras == ExtraTuples.INGORE && now <= finish);
        
        // anything we didn't get from earlier is missing
        for (; index < matchers.size(); ++index) {
            report.addMissingMatcher(matchers.get(index));
        }
        
        report.throwIfError();
    }
    
    /**
     * Expect num tuples to become available on the stream before the timeout.
     * @throws StreamBaseException if an internal error occurs
     * @throws AssertionError if the expected tuples do not match
     */
    public void expectTuples(int num) throws StreamBaseException {
        List<Tuple> tuples = dequeuer.dequeue(num, timeout, timeUnit);
        
        ErrorReport report = reportFactory.newErrorReport();
        for (; num > tuples.size(); --num) {
            report.addMissingMatcher(null); //TupleMatcher.any());
        }
        report.throwIfError();
    }
    
    /**
     * Expect no tuples to become available on the stream for the entire timeout, ensuring 
     * that all tuples current in flight will have time to finish.
     * @throws StreamBaseException if an internal error occurs
     * @throws AssertionError if the expected tuples do not match
     */
    public void expectNothing() throws StreamBaseException {
        dequeuer.drain();
        List<Tuple> tuples = dequeuer.dequeue(1, timeout, timeUnit);
        if (tuples.size() > 0) {
            // if we got 1, then we should grab the rest of them too
            tuples.addAll(dequeuer.dequeue(-1, 0, TimeUnit.MILLISECONDS));
        }
        
        ErrorReport report = reportFactory.newErrorReport();
        for (Tuple t : tuples) {
            report.addUnexpectedTuple(t);
        }
        report.throwIfError();
    }

}
