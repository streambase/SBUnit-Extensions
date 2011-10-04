package com.streambase.sbunit.ext.matchers.tuple;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;

public class AllTupleMatcher implements TupleMatcher {
    private final TupleMatcher m;
    private final TupleMatcher[] matchers;

    public AllTupleMatcher(TupleMatcher m, TupleMatcher[] matchers) {
        this.m = m;
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Tuple a) throws TupleException {
        boolean res = m.matches(a);
        for (TupleMatcher m : matchers) {
            res = res && m.matches(a);
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
    	
    	String res = "all of " + gson.toJson(parts);
    	return gson.toJsonTree(res);
    }
}