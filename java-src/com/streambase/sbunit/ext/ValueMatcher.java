package com.streambase.sbunit.ext;

import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;

/**
 * {@link ValueMatcher}s are used by {@link FieldBasedTupleMatcher} to
 * determine if particular values match.
 */
public interface ValueMatcher {
    /**
     * @return whether or not a value matches
     */
    public boolean matches(Object val) throws TupleException;
    
    /**
     * @return A human readable description of what is expected for
     * use in error messages.
     */
    public String describe();
}
