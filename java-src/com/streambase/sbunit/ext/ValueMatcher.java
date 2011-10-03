package com.streambase.sbunit.ext;

public interface ValueMatcher {
    public boolean matches(Object field);
    public String describe();
}
