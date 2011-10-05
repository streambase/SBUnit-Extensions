package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} that matches if and only if its
 * component {@link ValueMatcher} does not match.
 */
public class NotValueMatcher implements ValueMatcher {
    private final ValueMatcher m;

    public NotValueMatcher(ValueMatcher m) {
        this.m = m;
    }

    @Override
    public boolean matches(Object a) throws TupleException {
        return !m.matches(a);
    }

    @Override
    public JsonElement describe(Gson gson) {
    	String res = "not " + gson.toJson(m.describe(gson));
    	return gson.toJsonTree(res);
    }
}