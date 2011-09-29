package com.streambase.sbunit.ext.matchers;

import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.TupleMatcher;

public class NotMatcher implements TupleMatcher {
    private final TupleMatcher m;

    public NotMatcher(TupleMatcher m) {
        this.m = m;
    }

    @Override
    public boolean matches(Tuple a) {
        return !m.matches(a);
    }

    @Override
    public String describeExpected() {
        return "does not match " + m.describeExpected();
    }
}