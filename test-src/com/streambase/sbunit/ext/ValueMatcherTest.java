package com.streambase.sbunit.ext;

import org.junit.Assert;
import org.junit.Test;

import com.streambase.sbunit.ext.matchers.value.DifferenceTestDoubleValueMatcher;
import com.streambase.sbunit.ext.matchers.value.EqualsValueMatcher;
import com.streambase.sbunit.ext.matchers.value.NonNullValueMatcher;
import com.streambase.sbunit.ext.matchers.value.NullValueMatcher;
import com.streambase.sbunit.ext.matchers.value.RatioTestDoubleValueMatcher;

public class ValueMatcherTest {
    @Test
    public void testNullMatcher() throws Exception {
        Assert.assertTrue(new NullValueMatcher().matches(null));
        Assert.assertFalse(new NullValueMatcher().matches(1));
    }
    
    @Test
    public void testNonNullMatcher() throws Exception {
        Assert.assertFalse(new NonNullValueMatcher().matches(null));
        Assert.assertTrue(new NonNullValueMatcher().matches(1));
    }
    
    @Test
    public void testEqualsMatcher() throws Exception {
        Object o1 = new Object();
        Object o2 = new Object();
        Assert.assertFalse(new EqualsValueMatcher(o1).matches(o2));
        Assert.assertTrue(new EqualsValueMatcher(o2).matches(o2));
        Assert.assertFalse(new EqualsValueMatcher(new Integer(1024)).matches(o2));
        Assert.assertTrue(new EqualsValueMatcher(new Integer(1024)).matches(new Integer(1024)));
    }
    
    @Test
    public void testDoubleMatchers() throws Exception {
        double big = 123456789;
        double nearBig = 123456789 - 0.00001;
        double farBig = 123556789;
        double small = 0.0000000001234;
        double nearSmall = 0.000000000123400001;
        double farSmall = 0.0000000001235;
        
        ValueMatcher ratioBig = new RatioTestDoubleValueMatcher(big); 
        ValueMatcher ratioSmall = new RatioTestDoubleValueMatcher(small); 
        ValueMatcher diffBig = new DifferenceTestDoubleValueMatcher(big, 0.0003); 
        ValueMatcher diffSmall = new DifferenceTestDoubleValueMatcher(small, 0.0003); 
        
        Assert.assertTrue(ratioBig.matches(big));
        Assert.assertTrue(ratioBig.matches(nearBig));
        Assert.assertFalse(ratioBig.matches(farBig));
        Assert.assertFalse(ratioBig.matches(small));
        Assert.assertFalse(ratioBig.matches(nearSmall));
        Assert.assertFalse(ratioBig.matches(farSmall));
        
        Assert.assertFalse(ratioSmall.matches(big));
        Assert.assertFalse(ratioSmall.matches(nearBig));
        Assert.assertFalse(ratioSmall.matches(farBig));
        Assert.assertTrue(ratioSmall.matches(small));
        Assert.assertTrue(ratioSmall.matches(nearSmall));
        Assert.assertFalse(ratioSmall.matches(farSmall));
        
        
        Assert.assertTrue(diffBig.matches(big));
        Assert.assertTrue(diffBig.matches(nearBig));
        Assert.assertFalse(diffBig.matches(farBig));
        Assert.assertFalse(diffBig.matches(small));
        Assert.assertFalse(diffBig.matches(nearSmall));
        Assert.assertFalse(diffBig.matches(farSmall));
        
        Assert.assertFalse(diffSmall.matches(big));
        Assert.assertFalse(diffSmall.matches(nearBig));
        Assert.assertFalse(diffSmall.matches(farBig));
        Assert.assertTrue(diffSmall.matches(small));
        Assert.assertTrue(diffSmall.matches(nearSmall));
        Assert.assertTrue(diffSmall.matches(farSmall));
    }
    
}
