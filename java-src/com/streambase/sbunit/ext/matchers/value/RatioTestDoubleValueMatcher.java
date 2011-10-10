package com.streambase.sbunit.ext.matchers.value;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
        this(expected, 1.0e-6);
    }
    
    public RatioTestDoubleValueMatcher(double expected, double tolerance) {
        this.expected = expected;
        this.tolerance = tolerance;
    }
    
    @Override
    public boolean matches(Object actual) {
        if (actual instanceof Double) {        	
            return compareDoubles(tolerance, expected, (Double)actual);
        }
        return false;
    }
    
    @Override
    public JsonElement describe(Gson gson) {
        return gson.toJsonTree(expected);
    }
    
    private static boolean compareDoubles(double tolerance, double d1, double d2) {
        // For a discussion and analysis of this technique, check out:
        // http://adtmag.com/Articles/2000/03/16/Comparing-Floats-How-To-Determine-if-Floating-Quantities-Are-Close-Enough-Once-a-Tolerance-Has-Been.aspx?Page=1
        
        // handle 0.0 specifically, as it will screw with our division 
        if (d1 == 0.0 || d2 == 0.0) { return Math.abs(d1 - d2) < tolerance; }
        
        // catch overflow cases, we will divide by d1 which could increase the value of d2
        // but use absolute value to handle all the different permutations of signs
        double d1abs = Math.abs(d1);
        double d2abs = Math.abs(d2);
        if ((d1abs < 1.0) && (d2abs > d1abs*Double.MAX_VALUE)) { return false; }
        
        // comparing that they have a ratio near 1.0 handles both very large
        // and very small numbers cleanly with a single tolerance
        double ratio = d2 / d1;
        return ((1-tolerance) < ratio && ratio < (1+tolerance));
    }

}
