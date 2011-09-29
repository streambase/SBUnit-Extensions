package com.streambase.sbunit.ext;

import com.streambase.sbunit.ext.matchers.AllMatcher;
import com.streambase.sbunit.ext.matchers.AnyMatcher;
import com.streambase.sbunit.ext.matchers.NotMatcher;

public class Matchers {
    public static TupleMatcher anyOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AnyMatcher(m, matchers);
    }
    
    public static TupleMatcher allOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AllMatcher(m, matchers);
    }
    
    public static TupleMatcher not(TupleMatcher m) {
        return new NotMatcher(m);
    }
}
