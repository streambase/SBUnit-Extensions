package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} which uses the {@link #equals(Object)} method to
 * determine a match.
 */
public class EqualsValueMatcher<T> implements ValueMatcher<T> {
    private final T expected;
    
    public EqualsValueMatcher(T expected) {
        this.expected = expected;
    }
    
    @Override
    public boolean matches(T actual) {
        return expected.equals(actual);
    }
    
    @Override
    public String describe() {
        return expected.toString();
    }
}
