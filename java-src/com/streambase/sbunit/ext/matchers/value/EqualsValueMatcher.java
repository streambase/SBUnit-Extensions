package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} which uses the {@link #equals(Object)} method to
 * determine a match.
 */
public class EqualsValueMatcher implements ValueMatcher {
    private final Object expected;
    
    public EqualsValueMatcher(Object expected) {
        this.expected = expected;
    }
    
    @Override
    public boolean matches(Object actual) {
        return expected.equals(actual);
    }
    
    @Override
    public JsonElement describe(Gson gson) {
    	return gson.toJsonTree(expected);
    }
}
