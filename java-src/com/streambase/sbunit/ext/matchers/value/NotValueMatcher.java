package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.ValueMatcher;

public class NotValueMatcher implements ValueMatcher {
    private final ValueMatcher m;

    public NotValueMatcher(ValueMatcher m) {
        this.m = m;
    }

    @Override
    public boolean matches(Object a) throws TupleException {
        return !m.matches(a);
    }

    @Override
    public String describe() {
        return "does not match " + m.describe();
    }
}