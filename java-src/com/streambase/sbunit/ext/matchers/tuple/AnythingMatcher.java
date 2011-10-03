package com.streambase.sbunit.ext.matchers.tuple;

import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.TupleMatcher;

public class AnythingMatcher implements TupleMatcher {
    public AnythingMatcher() { }

    @Override
    public boolean matches(Tuple a) {
        return true;
    }

    @Override
    public String describe() {
        return "<anything>";
    }
}