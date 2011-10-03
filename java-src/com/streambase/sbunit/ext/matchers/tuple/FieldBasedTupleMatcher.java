package com.streambase.sbunit.ext.matchers.tuple;

import java.util.Map;

import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.TupleMatcher;

public class FieldBasedTupleMatcher implements TupleMatcher {
    private final Map<String, Object> matchers;
    
    private FieldBasedTupleMatcher() {
        matchers = null;
    }

    @Override
    public boolean matches(Tuple a) {
        // TODO Auto-generated method stub
        throw new RuntimeException("TODO: implement this");
    }

    @Override
    public String describe() {
        // TODO Auto-generated method stub
        throw new RuntimeException("TODO: implement this");
    }

}
