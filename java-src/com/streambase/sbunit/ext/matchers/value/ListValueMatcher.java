package com.streambase.sbunit.ext.matchers.value;

import java.util.List;

import com.streambase.org.json.simple.JSONArray;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} which uses the {@link #equals(Object)} method to
 * determine a match.
 */
public class ListValueMatcher implements ValueMatcher {
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
    
    @SuppressWarnings("unchecked")
    @Override
    public String describe() {
        JSONArray res = new JSONArray();
        for (ValueMatcher v : expected) {
            res.add(v.describe());
        }
        return res.toString();
    }
}
