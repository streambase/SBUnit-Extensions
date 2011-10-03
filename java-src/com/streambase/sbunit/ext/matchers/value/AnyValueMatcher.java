package com.streambase.sbunit.ext.matchers.value;

import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.ValueMatcher;

public class AnyValueMatcher implements ValueMatcher {
    private final ValueMatcher[] matchers;
    private final ValueMatcher m;

    public AnyValueMatcher(ValueMatcher m, ValueMatcher[] matchers) {
        this.matchers = matchers;
        this.m = m;
    }

    @Override
    public boolean matches(Object a) throws TupleException {
        boolean res = m.matches(a);
        for (ValueMatcher m : matchers) {
            res = res || m.matches(a);
        }
        return res;
    }

    @Override
    public String describe() {
        StringBuilder res = new StringBuilder();
        res.append("any of ");
        res.append(m.describe());
        for (ValueMatcher m : matchers) {
            res.append(", ");
            res.append(m.describe());
        }
        return res.toString();
    }
}