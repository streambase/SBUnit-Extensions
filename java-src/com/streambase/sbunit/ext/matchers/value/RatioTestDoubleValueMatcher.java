package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.streambase.sb.util.Util;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} which compares doubles using a ratio test
 * with a tolerance.  Ratio tests are applicable when the range of 
 * values are not known <i>a priori</i>.  The basic idea is that the
 * ratio <code>expect/actual</code> will be within <code>1 +/- tolerance</code>
 * regardless of the magnitude of expected or actual, allowing a default
 * tolerance to be more generally applicable. 
 */
public class RatioTestDoubleValueMatcher implements ValueMatcher {
    private final double expected;
    private final double tolerance;
    
    public RatioTestDoubleValueMatcher(double expected) {
        this(expected, Util.DEFAULT_DOUBLE_COMPARE_TOLERANCE);
    }
    
    public RatioTestDoubleValueMatcher(double expected, double tolerance) {
        this.expected = expected;
        this.tolerance = tolerance;
    }
    
    @Override
    public boolean matches(Object actual) {
        if (actual instanceof Double) {
            return Util.compareDoubles(tolerance, expected, (Double)actual);
        }
        return false;
    }
    
    @Override
    public JsonElement describe(Gson gson) {
    	return gson.toJsonTree(expected);
    }
}
