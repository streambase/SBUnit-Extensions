package com.streambase.sbunit.ext;

import com.streambase.sb.Tuple;

/**
 * {@link TupleMatcher}s are used by {@link StreamMatcher} to determine if
 * a tuple matches as an expected tuple or is an unexpected tuple.
 */
public interface TupleMatcher {
    
    /**
     * @return whether t matches.
     */
    public boolean matches(Tuple t);
    
    /**
     * @return a description of the matcher suitable
     * for use in error messages.
     */
    public String describe();
}
