package com.streambase.sbunit.ext.matchers;

import com.streambase.sbunit.ext.ValueMatcher;

/**
 * This interface is used to control structural recursion of 
 * {@link ValueMatcher}s when creating a new version of them
 * that ignores particular fields.
 */
public interface IgnoreFieldTransform extends ValueMatcher {
    /**
     * get a {@link ValueMatcher} like <code>this</code> but that will
     * ignore sub-fields with the name <code>field</code>.  If the result 
     * would be a trivial {@link ValueMatcher}, return <code>null</code> 
     * instead.
     * @param field  The field to ignore
     */
    public ValueMatcher ignoreField(String field);
}
