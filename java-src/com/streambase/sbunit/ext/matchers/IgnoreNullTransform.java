package com.streambase.sbunit.ext.matchers;

import com.streambase.sbunit.ext.ValueMatcher;

/**
 * This interface is used to control structural recursion of 
 * {@link ValueMatcher}s when creating a new version of them
 * that ignores nulls values.
 */
public interface IgnoreNullTransform extends ValueMatcher {
	/**
	 * get a {@link ValueMatcher} like <code>this</code> but that will
	 * ignore sub-values that are null.  If the result would be a trivial
	 * {@link ValueMatcher}, return <code>null</code> instead.
	 */
    public ValueMatcher ignoreNulls();
}
