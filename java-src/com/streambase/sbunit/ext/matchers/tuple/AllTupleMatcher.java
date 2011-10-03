package com.streambase.sbunit.ext.matchers.tuple;

import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.TupleMatcher;

public class AllTupleMatcher implements TupleMatcher {
    private final TupleMatcher m;
    private final TupleMatcher[] matchers;

    public AllTupleMatcher(TupleMatcher m, TupleMatcher[] matchers) {
        this.m = m;
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Tuple a) throws TupleException {
        boolean res = m.matches(a);
        for (TupleMatcher m : matchers) {
            res = res && m.matches(a);
        }
        return res;
    }

    @Override
    public String describe() {
        StringBuilder res = new StringBuilder();
        res.append("all of ");
        res.append(m.describe());
        for (TupleMatcher m : matchers) {
            res.append(", ");
            res.append(m.describe());
        }
        return res.toString();
    }
}