package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} which matches non-<code>null</code> values.
 */
public class NullValueMatcher<T> implements ValueMatcher<T> {
    public NullValueMatcher() { }
    
    @Override
    public boolean matches(T actual) {
        return actual != null;
    }
    
    @Override
    public String describe() {
        return "non-null";
    }
}
