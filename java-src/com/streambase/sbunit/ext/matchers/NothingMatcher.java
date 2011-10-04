package com.streambase.sbunit.ext.matchers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;
import com.streambase.sbunit.ext.ValueMatcher;

public class NothingMatcher implements TupleMatcher, ValueMatcher {
    public NothingMatcher() { }
    
    @Override
    public boolean matches(Object field) throws TupleException {
        return false;
    }

    @Override
    public boolean matches(Tuple a) {
        return false;
    }

    @Override
    public JsonElement describe(Gson gson) {
    	return gson.toJsonTree("<nothing>");
    }
}