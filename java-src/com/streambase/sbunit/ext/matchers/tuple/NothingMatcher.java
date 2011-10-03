package com.streambase.sbunit.ext.matchers.tuple;

import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;
import com.streambase.sbunit.ext.ValueMatcher;

public class NothingMatcher implements TupleMatcher, ValueMatcher {
    public NothingMatcher() { }
    
    @Override
    public boolean matches(Object field) throws TupleException {
        return false;
    }

    @Override
    public boolean matches(Tuple a) {
        return false;
    }

    @Override
    public String describe() {
        return "<nothing>";
    }
}