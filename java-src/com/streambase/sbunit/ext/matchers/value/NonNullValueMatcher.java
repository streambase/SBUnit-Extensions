package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
    public JsonElement describe(Gson gson) {
    	return gson.toJsonTree("non-null");
    }
}
