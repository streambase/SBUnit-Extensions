package com.streambase.sbunit.ext.matchers;

import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.TupleMatcher;

public class AnyMatcher implements TupleMatcher {
    private final TupleMatcher[] matchers;
    private final TupleMatcher m;

    public AnyMatcher(TupleMatcher m, TupleMatcher[] matchers) {
        this.matchers = matchers;
        this.m = m;
    }

    @Override
    public boolean matches(Tuple a) {
        boolean res = m.matches(a);
        for (TupleMatcher m : matchers) {
            res = res || m.matches(a);
        }
        return res;
    }

    @Override
    public String describeExpected() {
        StringBuilder res = new StringBuilder();
        res.append("any of ");
        res.append(m.describeExpected());
        for (TupleMatcher m : matchers) {
            res.append(", ");
            res.append(m.describeExpected());
        }
        return res.toString();
    }
}