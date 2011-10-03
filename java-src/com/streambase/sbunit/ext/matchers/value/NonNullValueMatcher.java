package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} which matches <code>null</code> values.
 */
public class NonNullValueMatcher implements ValueMatcher {
    public NonNullValueMatcher() { }
    
    @Override
    public boolean matches(Object actual) {
        return actual != null;
    }
    
    @Override
    public String describe() {
        return "non-null";
    }
}
