package com.streambase.sbunit.ext;

import java.util.Map;

import com.streambase.sb.Tuple;

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
    public String describeExpected() {
        // TODO Auto-generated method stub
        throw new RuntimeException("TODO: implement this");
    }

}
