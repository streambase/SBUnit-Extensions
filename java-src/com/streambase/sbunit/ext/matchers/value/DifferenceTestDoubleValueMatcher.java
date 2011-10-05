package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * A {@link ValueMatcher} which compares doubles using a difference test
 * with a tolerance.  Difference tests are applicable when the range of 
 * values <b>are</b> known <i>a priori</i>.  The basic idea is that the
 * difference <code>expect - actual</code> will be within <code>0 +/- tolerance</code>
 * when the values are close enough.
 * <p>
 * Use {@link RatioTestDoubleValueMatcher}, for a more general double comparison technique. 
 */
public class DifferenceTestDoubleValueMatcher implements ValueMatcher {
    private final double expected;
    private final double tolerance;
    
    public DifferenceTestDoubleValueMatcher(double expected, double tolerance) {
        this.expected = expected;
        this.tolerance = tolerance;
    }
    
    @Override
    public boolean matches(Object actual) {
        if (actual instanceof Double) {
            return Math.abs(expected - (Double)actual) < tolerance;
        }
        return false;
    }
    
    @Override
    public JsonElement describe(Gson gson) {
        return gson.toJsonTree(expected);
    }
}
