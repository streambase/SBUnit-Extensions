package com.streambase.sbunit.ext.matchers.tuple;

import java.util.LinkedHashMap;
import java.util.Map;

import com.streambase.sb.Schema;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.Matchers;
import com.streambase.sbunit.ext.TupleMatcher;
import com.streambase.sbunit.ext.ValueMatcher;

public class FieldBasedTupleMatcher implements TupleMatcher, ValueMatcher {
    private final LinkedHashMap<String, ValueMatcher> matchers;
    
    private FieldBasedTupleMatcher(LinkedHashMap<String, ValueMatcher> matchers) {
        this.matchers = matchers;
    }
    
    public static FieldBasedTupleMatcher forTuple(Tuple t) {
        LinkedHashMap<String, ValueMatcher> matchers = new LinkedHashMap<String, ValueMatcher>();
        for (Schema.Field f : t.getSchema().getFields()) {
            Object o = t.getField(f);
            matchers.put(f.getName(), Matchers.forType(f.getCompleteDataType(), o));
        }
        return new FieldBasedTupleMatcher(matchers);
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

    @Override
    public String describe() {
        // TODO Auto-generated method stub
        throw new RuntimeException("TODO: implement this");
    }

}
