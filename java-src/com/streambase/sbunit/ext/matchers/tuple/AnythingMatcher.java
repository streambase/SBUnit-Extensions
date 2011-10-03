package com.streambase.sbunit.ext.matchers.tuple;

import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;
import com.streambase.sbunit.ext.ValueMatcher;

public class AnythingMatcher implements TupleMatcher, ValueMatcher {
    public AnythingMatcher() { }
    
    @Override
    public boolean matches(Object field) throws TupleException {
        return true;
    }

    @Override
    public boolean matches(Tuple a) {
        return true;
    }

    @Override
    public String describe() {
        return "<anything>";
    }
}