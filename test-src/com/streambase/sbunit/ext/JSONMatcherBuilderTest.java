package com.streambase.sbunit.ext;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.streambase.sb.CompleteDataType;
import com.streambase.sb.DataType;
import com.streambase.sb.Schema;
import com.streambase.sb.StreamBaseException;
import com.streambase.sb.Timestamp;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.matcher.builder.JSONMatcherBuilder;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;


public class JSONMatcherBuilderTest {
	private Schema flatSchema;
	private Tuple tupleFromFlatSchema;

    private Schema complex;
    private Schema complexNoLists;
    private Schema point;
    private Tuple redJo;
    private Tuple blueDave;
    private Tuple greenNoPointLists;
    private String flatTupleJSONstring = "{'s':'string1','s2':'string2','i':42,'i2':43,'d':2.5,'d2':3.5,'b':true,'b2':false}";
    private String greenInFullJSONstring = "{'point':{'x':1,'y':2},'color':'red','dateTime':'2012-11-22 14:50:12.123-0500','id':{'name':'jo','seq':{'unique':false,'prefix':'pre-','num':1}}}";
    private String redJoInFullJSONstring = "{'points':[{'x':1,'y':2},{'x':3,'y':4}], 'dateTime':'2012-11-22 14:50:12.123-0500', 'color':'red','id':{'name':'jo','seq':{'unique':false,'prefix':'pre-','num':1}}}";
    private String blueDaveInFullJSONstring = "{'points':[{'x':1,'y':2},{'x':3,'y':4}], 'dateTime':'2012-11-22 14:50:12.123-0500', 'color':'blue','id':{'name':'dave','seq':{'unique':false,'prefix':'pre-','num':1}}}";
    private String redJoinFullWrongPointsWrongDatetimeJSONstring = "{'points':[{'x':99,'y':2},{'x':3,'y':4}], 'dateTime':'2010-01-02 00:00:00.000-0500', 'color':'red','id':{'name':'jo','seq':{'unique':false,'prefix':'pre-','num':1}}}";
    private String redJoMinusSomeFieldsJSONstring = "{'points':[{'x':1,'y':2},{'x':3,'y':4}], 'id':{'name':'jo','seq':{'unique':false,'num':1}}}";
	
	@Before
	public void createTestSchemas() throws Exception {
		flatSchema = new Schema(null,
				Schema.createField(DataType.STRING, "s"), Schema.createField(DataType.STRING, "s2"),
				Schema.createField(DataType.INT, "i"), Schema.createField(DataType.INT, "i2"),
				Schema.createField(DataType.DOUBLE, "d"), Schema.createField(DataType.DOUBLE, "d2"),
				Schema.createField(DataType.BOOL, "b"), Schema.createField(DataType.BOOL, "b2"));
		
		tupleFromFlatSchema = flatSchema.createTuple();		
		tupleFromFlatSchema.setString("s", "string1");		tupleFromFlatSchema.setString("s2", "string2");
		tupleFromFlatSchema.setInt("i", 42);		tupleFromFlatSchema.setInt("i2", 43);
		tupleFromFlatSchema.setDouble("d", 2.5);		tupleFromFlatSchema.setDouble("d2", 3.5);
		tupleFromFlatSchema.setBoolean("b", true);		tupleFromFlatSchema.setBoolean("b2", false);

        point = new Schema(null,Schema.createField(DataType.INT, "x"), Schema.createField(DataType.INT, "y"));
        
        Schema sequence = new Schema(null, Schema.createField(DataType.BOOL, "unique"),
                Schema.createField(DataType.STRING, "prefix"), Schema.createField(DataType.LONG, "num")
                );
        Schema id = new Schema(null, Schema.createField(DataType.STRING, "name"), Schema.createTupleField("seq", sequence)
        		);
        complex = new Schema(null, Schema.createListField("points", CompleteDataType.forTuple(point)),
                Schema.createField(DataType.STRING, "color"),Schema.createTupleField("id", id), Schema.createField(DataType.TIMESTAMP, "dateTime")
        		);  
        SimpleDateFormat sdft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSZ");
		Date dt = sdft.parse("2012-11-22 14:50:12.123-0500");
        redJo = complex( Arrays.asList(point(1,2), point(3,4)), "red", "jo", "pre-", 1L, false, dt);          
        complexNoLists = new Schema(null, Schema.createTupleField("point", point), Schema.createField(DataType.STRING, "color"),Schema.createTupleField("id", id), Schema.createField(DataType.TIMESTAMP, "dateTime") );        
        greenNoPointLists = complexNoLists( 1,2, "red", "jo", "pre-", 1L, false, dt);
        blueDave = complex(Arrays.asList(point(1,2), point(3,4)),"blue", "dave", "pre-", 1L, false,dt);
    }
	
	/*
	 * helper methods
	 */
    public Tuple point(Integer x, Integer y) throws Exception {
        Tuple p = point.createTuple();  p.setField("x", x);  p.setField("y", y);
        return p;
    }
    
    public Tuple complex(List<Tuple> points, String color, String name, String prefix, Long sequence, Boolean unique, Date dateTime ) throws Exception {
        Tuple t = complex.createTuple();
        t.setField("points", points); t.setField("color", color); t.setField("id.name", name);
        t.setField("id.seq.prefix", prefix); t.setField("id.seq.num", sequence); t.setField("id.seq.unique", unique);
        t.setTimestamp("dateTime", new Timestamp(dateTime));
        return t;
    }  
    
    public Tuple complexNoLists(int x, int y, String color, String name, String prefix, Long sequence, Boolean unique, Date dateTime) throws Exception {
        Tuple t = complexNoLists.createTuple();
        t.setField("point.x", x); t.setField("point.y", y); t.setField("color", color); t.setField("id.name", name);
        t.setField("id.seq.prefix", prefix); t.setField("id.seq.num", sequence); t.setField("id.seq.unique", unique);
        t.setTimestamp("dateTime", new Timestamp(dateTime));
        return t;
    }
	
	@Test // test all fields of a tuple 
	public void allFields() throws Exception {
		JSONMatcherBuilder mb = new JSONMatcherBuilder(flatSchema);
		TupleMatcher m = mb.makeMatcher( flatTupleJSONstring );		
		Assert.assertTrue(m.matches(tupleFromFlatSchema));	
		
		mb = new JSONMatcherBuilder(complexNoLists);
		m = mb.makeMatcher(greenInFullJSONstring);
		Assert.assertTrue(m.matches(greenNoPointLists));
		
		mb = new JSONMatcherBuilder( complex );
		m = mb.makeMatcher(redJoInFullJSONstring);	
		Assert.assertTrue(m.matches(redJo));
	}

	@Test  // a test where only some fields are specified
	public void everyOtherField() throws Exception {
		JSONMatcherBuilder mb = new JSONMatcherBuilder(flatSchema).ignoreMissingFields(true);
		TupleMatcher m = mb.makeMatcher("{'s':'string1','i':42,'d':2.5,'b':true}");		
		Assert.assertTrue(m.matches(tupleFromFlatSchema));
	}

	
	@Test  // assert that a matcher on incorrect fails
	public void noMatchFromJSON() throws Exception {
		JSONMatcherBuilder mb = new JSONMatcherBuilder(flatSchema).ignoreMissingFields(true);
		TupleMatcher m = mb.makeMatcher("{'s':'***********Incorrect Value**********','i':42,'d':2.5,'b':true}");		
		Assert.assertFalse(m.matches(tupleFromFlatSchema));
		
		mb = new JSONMatcherBuilder(complex).ignoreMissingFields(true);
		m = mb.makeMatcher( redJoinFullWrongPointsWrongDatetimeJSONstring );	
		Assert.assertFalse(m.matches(redJo)); // should fail matcher
	}
	
	@Test  // assert Exception On Missing JSON field
	public void testExceptionOnMissingJSONfield() throws Exception {
		boolean exceptionCaught = false;
		JSONMatcherBuilder mb = new JSONMatcherBuilder(complex);//missing: .ignoreMissingFields(true);
		try {
			FieldBasedTupleMatcher m = mb.makeMatcher( redJoMinusSomeFieldsJSONstring );	// minus the 'prefix' in subTuple id.seq, and 'color'
		} catch (StreamBaseException e) {
			exceptionCaught = true;
		}
		Assert.assertTrue( exceptionCaught );
	}	
	
	@Test  // only match part of a complex schema
	public void matchPart() throws Exception {
		JSONMatcherBuilder mb = new JSONMatcherBuilder(complex).ignoreMissingFields(true);
		TupleMatcher m = mb.makeMatcher( redJoMinusSomeFieldsJSONstring );	// minus the 'prefix' in subTuple id.seq, and 'color'
		Assert.assertTrue(m.matches(redJo));		

		// minus the 'prefix' in subTuple id.seq, and 'color', and an "afterthought" to ignore points list also
		m = mb.makeMatcher( redJoinFullWrongPointsWrongDatetimeJSONstring ).ignore("points.x").ignore("dateTime");	
		Assert.assertTrue(m.matches(redJo));
		// minus the 'prefix' in subTuple id.seq, and 'color', and an "afterthought" to ignore points list also
		m = mb.makeMatcher( redJoinFullWrongPointsWrongDatetimeJSONstring ).ignore("points").ignore("dateTime");	
		Assert.assertTrue(m.matches(redJo));
	}
}
