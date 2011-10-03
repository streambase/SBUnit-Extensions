package com.streambase.sbunit.ext.matchers.value;

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
public class RatioTestDoubleValueMatcher implements ValueMatcher<Double> {
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
    public boolean matches(Double actual) {
        if (actual == null) {
            return false;
        }
        return Util.compareDoubles(tolerance, expected, actual);
    }
    
    @Override
    public String describe() {
        return Double.toString(expected);
    }
}
