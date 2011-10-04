package com.streambase.sbunit.ext.matchers.value;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.Matchers;
import com.streambase.sbunit.ext.ValueMatcher;
import com.streambase.sbunit.ext.matchers.IgnoreNullTransform;

/**
 * A {@link ValueMatcher} which uses the {@link #equals(Object)} method to
 * determine a match.
 */
public class ListValueMatcher implements ValueMatcher, IgnoreNullTransform {
    private final List<? extends ValueMatcher> expected;
    
    public ListValueMatcher(List<? extends ValueMatcher> expected) {
        this.expected = expected;
    }
    
    @Override
    public boolean matches(Object actual) throws TupleException {
        if (actual instanceof List) {
            List<?> actualList = (List<?>)actual;
            if (actualList.size() != expected.size()) {
                return false;
            }
            for (int i = 0; i < expected.size(); ++i) {
                boolean matches = expected.get(i).matches(actualList.get(i));
                if (!matches) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public ValueMatcher ignoreNulls() {
        List<ValueMatcher> res = new ArrayList<ValueMatcher>();
        for (ValueMatcher vm : expected) {
            if (vm instanceof IgnoreNullTransform) {
                vm = ((IgnoreNullTransform) vm).ignoreNulls();
                if (vm == null) {
                    vm = Matchers.anything();
                }
            }
            res.add(vm);
        }
        return new ListValueMatcher(res);
    }
    
    @Override
    public JsonElement describe(Gson gson) {
    	JsonArray parts = new JsonArray();
        for (ValueMatcher v : expected) {
    		parts.add(v.describe(gson));
    	}
    	return parts;
    }
}
