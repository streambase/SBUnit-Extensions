package com.streambase.sbunit.ext.matchers.tuple;

import java.util.HashMap;
import java.util.Map;

import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;
import com.streambase.sbunit.ext.ValueMatcher;

public class FieldBasedTupleMatcher implements TupleMatcher {
    private final Map<String, ValueMatcher> matchers;
    
    private FieldBasedTupleMatcher(Map<String, ValueMatcher> old, 
            String field, ValueMatcher matcher) {
        matchers = new HashMap<String, ValueMatcher>(old);
        matchers.put(field, matcher);
    }

    @Override
    public boolean matches(Tuple t) throws TupleException {
        for (Map.Entry<String, ValueMatcher> e : matchers.entrySet()) {
            Object field = t.getField(e.getKey());
            boolean matches = e.getValue().matches(field);
            if (!matches) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String describe() {
        // TODO Auto-generated method stub
        throw new RuntimeException("TODO: implement this");
    }

}
