package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sbunit.ext.ValueMatcher;

public class EqualsValueMatcher<T> implements ValueMatcher<T> {
    private final T expected;
    
    public EqualsValueMatcher(T expected) {
        this.expected = expected;
    }
    
    public boolean matches(T actual) {
        return expected.equals(actual);
    }
    
    public String describe() {
        return expected.toString();
    }

}
