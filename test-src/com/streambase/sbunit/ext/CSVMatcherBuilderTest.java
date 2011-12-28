package com.streambase.sbunit.ext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.streambase.sb.DataType;
import com.streambase.sb.Schema;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.matcher.builder.CSVMatcherBuilder;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;


public class CSVMatcherBuilderTest {
	private Schema s;
	private Tuple t;
	
	@Before
	public void makeSchemaAndTuple() throws TupleException {
		s = new Schema(null,
				Schema.createField(DataType.STRING, "s"),
				Schema.createField(DataType.STRING, "s2"),
				Schema.createField(DataType.INT, "i"),
				Schema.createField(DataType.INT, "i2"),
				Schema.createField(DataType.DOUBLE, "d"),
				Schema.createField(DataType.DOUBLE, "d2"),
				Schema.createField(DataType.BOOL, "b"),
				Schema.createField(DataType.BOOL, "b2"));
		
		t = s.createTuple();
		
		t.setString("s", "string1");
		t.setString("s2", "string2");
		t.setInt("i", 42);
		t.setInt("i2", 43);
		t.setDouble("d", 2.5);
		t.setDouble("d2", 3.5);
		t.setBoolean("b", true);
		t.setBoolean("b2", false);
	}
	
	@Test
	public void everyFieldInOrder() throws Exception {
		CSVMatcherBuilder mb = new CSVMatcherBuilder(s, "s","s2", "i","i2", "d","d2", "b","b2");
		FieldBasedTupleMatcher m = mb.makeMatcher("string1","string2","42","43","2.5","3.5","true","false");
		
		Assert.assertTrue(m.matches(t));
	}

	@Test
	public void everyOtherField() throws Exception {
		CSVMatcherBuilder mb = new CSVMatcherBuilder(s, "s", "i", "d", "b");
		FieldBasedTupleMatcher m = mb.makeMatcher("string1","42","2.5","true");
		
		Assert.assertTrue(m.matches(t));
	}
	
	@Test
	public void matchFromCSV() throws Exception {
		CSVMatcherBuilder mb = new CSVMatcherBuilder(s, "s", "i", "d", "b");
		FieldBasedTupleMatcher m = mb.makeMatcher("string1,42,2.5,true");
		
		Assert.assertTrue(m.matches(t));
	}
	
	@Test
	public void noMatchFromCSV() throws Exception {
		CSVMatcherBuilder mb = new CSVMatcherBuilder(s, "s", "i", "d", "b");
		FieldBasedTupleMatcher m = mb.makeMatcher("string1,42,3.5,true");
		
		Assert.assertFalse(m.matches(t));
	}
	
	@Test
	public void nullFromCSV() throws Exception {
		CSVMatcherBuilder mb = new CSVMatcherBuilder(s, "s", "i", "d", "b");		
		FieldBasedTupleMatcher m = mb.makeMatcher("string1,,2.5,true");
		
		t.setNull("i");

		Assert.assertTrue(m.matches(t));
	}
	
	
}
