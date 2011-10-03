package com.streambase.sbunit.ext;

import com.streambase.sbunit.ext.matchers.AllMatcher;
import com.streambase.sbunit.ext.matchers.AnyMatcher;
import com.streambase.sbunit.ext.matchers.AnythingMatcher;
import com.streambase.sbunit.ext.matchers.NotMatcher;
import com.streambase.sbunit.ext.matchers.NothingMatcher;

/**
 * Factory and utility methods for {@link TupleMatcher}
 * and {@link ValueMatcher} classes.
 */
public class Matchers {
    /**
     * @return a single {@link TupleMatcher} that will match if and only if at
     * least one of the argument {@link TupleMatcher}s does.
     */
    public static TupleMatcher anyOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AnyMatcher(m, matchers);
    }
    
    /**
     * @return a single {@link TupleMatcher} that will match if and only if all
     * of the argument {@link TupleMatcher}s does.
     */
    public static TupleMatcher allOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AllMatcher(m, matchers);
    }
    
    /**
     * @return a single {@link TupleMatcher} that will match if and only if the
     * argument {@link TupleMatcher}s does not.
     */
    public static TupleMatcher not(TupleMatcher m) {
        return new NotMatcher(m);
    }
    
    /**
     * @return a {@link TupleMatcher} that will match anything.
     */
    public static TupleMatcher anything() {
        return new AnythingMatcher();
    }
    
    /**
     * @return a {@link TupleMatcher} that will never match.
     */
    public static TupleMatcher nothing() {
        return new NothingMatcher();
    }
}
