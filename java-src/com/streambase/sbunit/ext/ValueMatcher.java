package com.streambase.sbunit.ext;

public interface ValueMatcher<T> {
    public boolean matches(T a);
    public String describeExpected();
}
