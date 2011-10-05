package com.streambase.sbunit.ext.matchers.tuple;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;

/**
 * A {@link TupleMatcher} that matches if and only if any of its
 * component {@link TupleMatcher}s match.
 */
public class AnyTupleMatcher implements TupleMatcher {
    private final TupleMatcher[] matchers;
    private final TupleMatcher m;

    public AnyTupleMatcher(TupleMatcher m, TupleMatcher[] matchers) {
        this.matchers = matchers;
        this.m = m;
    }

    @Override
    public boolean matches(Tuple a) throws TupleException {
        boolean res = m.matches(a);
        for (TupleMatcher m : matchers) {
            res = res || m.matches(a);
        }
        return res;
    }

    @Override
    public JsonElement describe(Gson gson) {
    	JsonArray parts = new JsonArray();
    	parts.add(m.describe(gson));
    	for (TupleMatcher m : matchers) {
    		parts.add(m.describe(gson));
    	}
    	
    	String res = "any of " + gson.toJson(parts);
    	return gson.toJsonTree(res);
    }
}