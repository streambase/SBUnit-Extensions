package com.streambase.sbunit.ext.matchers.tuple;

import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.TupleMatcher;

public class NothingMatcher implements TupleMatcher {
    public NothingMatcher() { }

    @Override
    public boolean matches(Tuple a) {
        return false;
    }

    @Override
    public String describe() {
        return "<nothing>";
    }
}