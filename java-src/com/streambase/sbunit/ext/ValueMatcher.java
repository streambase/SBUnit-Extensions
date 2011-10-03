package com.streambase.sbunit.ext;

import com.streambase.sb.TupleException;

public interface ValueMatcher {
    public boolean matches(Object field) throws TupleException;
    public String describe();
}
