package com.streambase.sbunit.ext.matchers.value;

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
public class DifferenceTestDoubleValueMatcher implements ValueMatcher<Double> {
    private final double expected;
    private final double tolerance;
    
    public DifferenceTestDoubleValueMatcher(double expected, double tolerance) {
        this.expected = expected;
        this.tolerance = tolerance;
    }
    
    @Override
    public boolean matches(Double actual) {
        if (actual == null) {
            return false;
        }
        return Math.abs(expected - actual) < tolerance;
    }
    
    @Override
    public String describe() {
        return Double.toString(expected);
    }
}
