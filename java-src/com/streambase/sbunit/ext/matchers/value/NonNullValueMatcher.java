package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} which matches <code>null</code> values.
 */
public class NonNullValueMatcher<T> implements ValueMatcher<T> {
    public NonNullValueMatcher() { }
    
    @Override
    public boolean matches(T actual) {
        return actual == null;
    }
    
    @Override
    public String describe() {
        return "null";
    }
}
