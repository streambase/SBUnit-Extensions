package com.streambase.sbunit.ext.matchers;

import com.streambase.sbunit.ext.ValueMatcher;

/**
 * Does this {@link ValueMatcher} require structural recursion 
 * when instructed to ignore nulls values.
 * 
 * @return an {@link ValueMatcher} like <code>this</code> but that will
 * ignore sub-values that are null.  If the result would be a trivial
 * {@link ValueMatcher}, return <code>null</code> instead.
 */
public interface IgnoreNullTransform extends ValueMatcher {
    public ValueMatcher ignoreNulls();
}
