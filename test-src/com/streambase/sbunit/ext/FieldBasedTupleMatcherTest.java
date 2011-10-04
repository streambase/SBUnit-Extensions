package com.streambase.sbunit.ext;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.streambase.sb.CompleteDataType;
import com.streambase.sb.DataType;
import com.streambase.sb.Schema;
import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;

public class FieldBasedTupleMatcherTest {
    private Schema complex;
    private Schema point;
    
    @Before
    public void createSchema() {
        // (points list((x int, y int)), 
    	//  color string, 
    	//  id (name string, 
    	//      id (unique boolean, 
    	//          prefix string, 
    	//          sequence long)))
        point = new Schema(null,
                Schema.createField(DataType.INT, "x"),
                Schema.createField(DataType.INT, "y"));
        
        Schema sequence = new Schema(null,
                Schema.createField(DataType.BOOL, "unique"),
                Schema.createField(DataType.STRING, "prefix"),
                Schema.createField(DataType.LONG, "num"));
        Schema id = new Schema(null,
                Schema.createField(DataType.STRING, "name"),
                Schema.createTupleField("seq", sequence));
        
        complex = new Schema(null,
                Schema.createListField("points", CompleteDataType.forTuple(point)),
                Schema.createField(DataType.STRING, "color"),
                Schema.createTupleField("id", id)
        );        
    }
    public Tuple point(Integer x, Integer y) throws Exception {
        Tuple p = point.createTuple();
        p.setField("x", x);
        p.setField("y", y);
        return p;
    }
    
    public Tuple complex(List<Tuple> points, String color, String name, String prefix, Long sequence, Boolean unique) throws Exception {
        Tuple t = complex.createTuple();
        t.setField("points", points);
        t.setField("color", color);
        t.setField("id.name", name);
        t.setField("id.seq.prefix", prefix);
        t.setField("id.seq.num", sequence);
        t.setField("id.seq.unique", unique);
        return t;
    }
    
    
    @Test
    public void testAnythingMatcher() throws Exception {
        FieldBasedTupleMatcher m;
        Tuple redJo = complex(
                Arrays.asList(point(1,2), point(3,4)),
                "red", "jo", "pre-", 1L, false);
        
        Tuple blueDave = complex(
                Arrays.asList(point(1,2), point(3,4)),
                "blue", "dave", "pre-", 1L, false);
        
        Tuple someNulls = complex(
                Arrays.asList(point(null,2), point(3,4)),
                null, null, "pre-", 1L, false);
        
        m = Matchers.literal(redJo);
        Assert.assertTrue(m.matches(redJo));
        Assert.assertFalse(m.matches(blueDave));
        
        m = Matchers.literal(redJo)
		        .ignore("id.name")
		        .ignore("color");
        Assert.assertTrue(m.matches(redJo));
        Assert.assertTrue(m.matches(blueDave));
        
        
        m = Matchers.emptyFieldMatcher()
                .require("id.seq.unique", true);
        Assert.assertFalse(m.matches(redJo));
        Assert.assertFalse(m.matches(blueDave));
        
        m = Matchers.literal(someNulls)
                .ignoreNulls();
        Assert.assertTrue(m.matches(redJo));
        Assert.assertTrue(m.matches(blueDave));
        
        m = Matchers.literal(someNulls)
                .ignoreNulls()
                .ignore("points");
        Assert.assertTrue(m.matches(redJo));
        Assert.assertTrue(m.matches(blueDave));
    }
    
}
