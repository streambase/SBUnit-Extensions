package com.streambase.sbunit.ext.matchers.tuple;

import java.util.LinkedHashMap;
import java.util.Map;

import com.streambase.org.json.simple.JSONObject;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;
import com.streambase.sbunit.ext.ValueMatcher;
import com.streambase.sbunit.ext.matchers.value.NullValueMatcher;

public class FieldBasedTupleMatcher implements TupleMatcher, ValueMatcher {
    private final LinkedHashMap<String, ValueMatcher> matchers;
    
    public FieldBasedTupleMatcher(LinkedHashMap<String, ValueMatcher> matchers) {
        this.matchers = matchers;
    }
    
    public FieldBasedTupleMatcher ignoreNulls() {
        LinkedHashMap<String, ValueMatcher> newMatchers = new LinkedHashMap<String, ValueMatcher>();
        for (Map.Entry<String, ValueMatcher> e : matchers.entrySet()) {
            if (!(e.getValue() instanceof NullValueMatcher)) {
                newMatchers.put(e.getKey(), e.getValue());
            }
        }
        return new FieldBasedTupleMatcher(newMatchers);
    }
    
    public FieldBasedTupleMatcher ignore(String field) {
        LinkedHashMap<String, ValueMatcher> newMatchers = new LinkedHashMap<String, ValueMatcher>(matchers);
        newMatchers.remove(field);
        return new FieldBasedTupleMatcher(newMatchers);
    }
    
    public FieldBasedTupleMatcher require(String field, ValueMatcher m) {
        LinkedHashMap<String, ValueMatcher> newMatchers = new LinkedHashMap<String, ValueMatcher>(matchers);
        newMatchers.put(field, m);
        return new FieldBasedTupleMatcher(newMatchers);
    }
    
    
    @Override
    public boolean matches(Object field) throws TupleException {
        if (field instanceof Tuple) {
            return matches((Tuple)field);
        }
        return false;
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

    @SuppressWarnings("unchecked")
    @Override
    public String describe() {
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, ValueMatcher> e : matchers.entrySet()) {
            obj.put(e.getKey(), e.getValue().describe());
        }
        return obj.toString();
    }

}
