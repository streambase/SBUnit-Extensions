package com.streambase.sbunit.ext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.streambase.sb.DataType;
import com.streambase.sb.NullValueException;
import com.streambase.sb.Schema;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;

public class TupleMatcherTest {
    private static class ModulusMatcher implements TupleMatcher {
        private final int base;
        private final String field;
        public ModulusMatcher(int base, String field) {
            this.base = base;
            this.field = field;
        }
        @Override
        public boolean matches(Tuple v) throws TupleException {
            return v.getInt(field) % base == 0;
        }
        
        @Override
        public String describe() {
            return null;
        }
    };
    
    private Schema point;
    
    @Before
    public void createSchema() {
        point = new Schema(null,
                Schema.createField(DataType.INT, "x"),
                Schema.createField(DataType.INT, "y"));
    }
    
    public Tuple make(int x) throws NullValueException, TupleException {
        Tuple t = point.createTuple();
        t.setInt("x", x);
        return t;
    }
    
    @Test
    public void testAnyAndAllMatchers() throws Exception {
        TupleMatcher mod2 = new ModulusMatcher(2, "x");
        TupleMatcher mod3 = new ModulusMatcher(3, "x");
        TupleMatcher mod5 = new ModulusMatcher(5, "x");
        
        Assert.assertTrue(Matchers.allOf(mod2, mod3, mod5).matches(make(60)));
        Assert.assertTrue(Matchers.allOf(mod2, mod3, mod5).matches(make(30)));
        Assert.assertFalse(Matchers.allOf(mod2, mod3, mod5).matches(make(15)));
        Assert.assertFalse(Matchers.allOf(mod2, mod3, mod5).matches(make(2)));
        Assert.assertFalse(Matchers.allOf(mod2, mod3, mod5).matches(make(3)));
        Assert.assertFalse(Matchers.allOf(mod2, mod3, mod5).matches(make(5)));
        Assert.assertFalse(Matchers.allOf(mod2, mod3, mod5).matches(make(8)));
        Assert.assertFalse(Matchers.allOf(mod2, mod3, mod5).matches(make(17)));
        
        Assert.assertTrue(Matchers.anyOf(mod2, mod3, mod5).matches(make(60)));
        Assert.assertTrue(Matchers.anyOf(mod2, mod3, mod5).matches(make(30)));
        Assert.assertTrue(Matchers.anyOf(mod2, mod3, mod5).matches(make(15)));
        Assert.assertTrue(Matchers.anyOf(mod2, mod3, mod5).matches(make(2)));
        Assert.assertTrue(Matchers.anyOf(mod2, mod3, mod5).matches(make(3)));
        Assert.assertTrue(Matchers.anyOf(mod2, mod3, mod5).matches(make(5)));
        Assert.assertTrue(Matchers.anyOf(mod2, mod3, mod5).matches(make(8)));
        Assert.assertFalse(Matchers.anyOf(mod2, mod3, mod5).matches(make(17)));
    }
    
    @Test
    public void testAnythingMatcher() throws Exception {
        Assert.assertTrue(Matchers.anything().matches(make(1)));
        Assert.assertTrue(Matchers.anything().matches(make(2)));
    }
    
    @Test
    public void testNothingMatcher() throws Exception {
        Assert.assertFalse(Matchers.nothing().matches(make(1)));
        Assert.assertFalse(Matchers.nothing().matches(make(2)));
    }
    
    @Test
    public void testNotMatcher() throws Exception {
        TupleMatcher mod2 = new ModulusMatcher(2, "x");
        
        Assert.assertTrue(Matchers.not(mod2).matches(make(5)));
        Assert.assertFalse(Matchers.not(mod2).matches(make(4)));
    }
    
    
}
