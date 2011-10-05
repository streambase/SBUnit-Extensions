package com.streambase.sbunit.ext.matchers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link TupleMatcher} and a {@link ValueMatcher} that always matches.
 */
public class AnythingMatcher implements TupleMatcher, ValueMatcher {
    public AnythingMatcher() { }
    
    @Override
    public boolean matches(Object field) throws TupleException {
        return true;
    }

    @Override
    public boolean matches(Tuple a) {
        return true;
    }
    
    @Override
    public JsonElement describe(Gson gson) {
    	return gson.toJsonTree("<anything>");
    }
}