package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
    public JsonElement describe(Gson gson) {
        return gson.toJsonTree(null);
    }
}
