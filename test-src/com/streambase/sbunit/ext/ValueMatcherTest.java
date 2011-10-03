package com.streambase.sbunit.ext;

import org.junit.Assert;
import org.junit.Test;

import com.streambase.sbunit.ext.matchers.value.NonNullValueMatcher;
import com.streambase.sbunit.ext.matchers.value.NullValueMatcher;

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
    
}
