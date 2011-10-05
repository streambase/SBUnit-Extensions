package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} that matches if and only if all of its
 * component {@link ValueMatcher}s match.
 */
public class AllValueMatcher implements ValueMatcher {
    private final ValueMatcher m;
    private final ValueMatcher[] matchers;

    public AllValueMatcher(ValueMatcher m, ValueMatcher[] matchers) {
        this.m = m;
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Object a) throws TupleException {
        boolean res = m.matches(a);
        for (ValueMatcher m : matchers) {
            res = res && m.matches(a);
        }
        return res;
    }

    @Override
    public JsonElement describe(Gson gson) {
    	JsonArray parts = new JsonArray();
    	parts.add(m.describe(gson));
    	for (ValueMatcher m : matchers) {
    		parts.add(m.describe(gson));
    	}
    	
    	String res = "all of " + gson.toJson(parts);
    	return gson.toJsonTree(res);
    }
}