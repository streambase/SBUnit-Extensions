package com.streambase.sbunit.ext;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
     * @return a description of the matcher suitable
     * for use in error messages.
     */
    public JsonElement describe(Gson gson);
}
