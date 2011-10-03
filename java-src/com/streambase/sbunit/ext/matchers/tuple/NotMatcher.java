package com.streambase.sbunit.ext.matchers.tuple;

import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;

public class NotMatcher implements TupleMatcher {
    private final TupleMatcher m;

    public NotMatcher(TupleMatcher m) {
        this.m = m;
    }

    @Override
    public boolean matches(Tuple a) throws TupleException {
        return !m.matches(a);
    }

    @Override
    public String describe() {
        return "does not match " + m.describe();
    }
}