package com.streambase.sbunit.ext.matchers;

import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.TupleMatcher;

public class AllMatcher implements TupleMatcher {
    private final TupleMatcher m;
    private final TupleMatcher[] matchers;

    public AllMatcher(TupleMatcher m, TupleMatcher[] matchers) {
        this.m = m;
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Tuple a) {
        boolean res = m.matches(a);
        for (TupleMatcher m : matchers) {
            res = res && m.matches(a);
        }
        return res;
    }

    @Override
    public String describeExpected() {
        StringBuilder res = new StringBuilder();
        res.append("all of ");
        res.append(m.describeExpected());
        for (TupleMatcher m : matchers) {
            res.append(", ");
            res.append(m.describeExpected());
        }
        return res.toString();
    }
}