package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} that matches if and only if any of its
 * component {@link ValueMatcher}s match.
 */
public class AnyValueMatcher implements ValueMatcher {
    private final ValueMatcher[] matchers;
    private final ValueMatcher m;

    public AnyValueMatcher(ValueMatcher m, ValueMatcher[] matchers) {
        this.matchers = matchers;
        this.m = m;
    }

    @Override
    public boolean matches(Object a) throws TupleException {
        boolean res = m.matches(a);
        for (ValueMatcher m : matchers) {
            res = res || m.matches(a);
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
        
        String res = "any of " + gson.toJson(parts);
        return gson.toJsonTree(res);
    }
}