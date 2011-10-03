package com.streambase.sbunit.ext.matchers.tuple;

import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;

public class AnyTupleMatcher implements TupleMatcher {
    private final TupleMatcher[] matchers;
    private final TupleMatcher m;

    public AnyTupleMatcher(TupleMatcher m, TupleMatcher[] matchers) {
        this.matchers = matchers;
        this.m = m;
    }

    @Override
    public boolean matches(Tuple a) throws TupleException {
        boolean res = m.matches(a);
        for (TupleMatcher m : matchers) {
            res = res || m.matches(a);
        }
        return res;
    }

    @Override
    public String describe() {
        StringBuilder res = new StringBuilder();
        res.append("any of ");
        res.append(m.describe());
        for (TupleMatcher m : matchers) {
            res.append(", ");
            res.append(m.describe());
        }
        return res.toString();
    }
}