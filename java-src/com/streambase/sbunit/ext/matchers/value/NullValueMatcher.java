package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sbunit.ext.ValueMatcher;
import com.streambase.sbunit.ext.matchers.IgnoreNullTransform;

/**
 * A {@link ValueMatcher} which matches non-<code>null</code> values.
 */
public class NullValueMatcher implements ValueMatcher, IgnoreNullTransform {
    public NullValueMatcher() { }
    
    @Override
    public boolean matches(Object actual) {
        return actual == null;
    }
    
    @Override
    public ValueMatcher ignoreNulls() {
        return null;
    }
    
    @Override
    public String describe() {
        return "null";
    }
}
