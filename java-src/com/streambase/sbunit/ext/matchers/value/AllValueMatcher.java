package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.ValueMatcher;

public class AllValueMatcher implements ValueMatcher {
    private final ValueMatcher m;
    private final ValueMatcher[] matchers;

    public AllValueMatcher(ValueMatcher m, ValueMatcher[] matchers) {
        this.m = m;
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Object a) throws TupleException {
        boolean res = m.matches(a);
        for (ValueMatcher m : matchers) {
            res = res && m.matches(a);
        }
        return res;
    }

    @Override
    public String describe() {
        StringBuilder res = new StringBuilder();
        res.append("all of ");
        res.append(m.describe());
        for (ValueMatcher m : matchers) {
            res.append(", ");
            res.append(m.describe());
        }
        return res.toString();
    }
}