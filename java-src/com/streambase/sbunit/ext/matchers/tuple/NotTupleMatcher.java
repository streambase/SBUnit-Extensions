package com.streambase.sbunit.ext.matchers.tuple;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;

public class NotTupleMatcher implements TupleMatcher {
    private final TupleMatcher m;

    public NotTupleMatcher(TupleMatcher m) {
        this.m = m;
    }

    @Override
    public boolean matches(Tuple a) throws TupleException {
        return !m.matches(a);
    }

    @Override
    public JsonElement describe(Gson gson) {
    	String res = "not " + gson.toJson(m.describe(gson));
    	return gson.toJsonTree(res);
    }
}