/**
 * 
 */
package com.streambase.sbunit.ext;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.streambase.sb.CompleteDataType;
import com.streambase.sb.DataType;
import com.streambase.sb.Schema;
import com.streambase.sb.StreamBaseException;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleJSONUtil;
import com.streambase.sbunit.ext.matcher.builder.JSONMatcherBuilder;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;

public class JSONMatcherBuilderNestedTest {
	
	public Schema makeNestedSchema()
	{
		Schema childSchema = new Schema(null, 
				Schema.createField(DataType.STRING, "ChildProperty1"), 
				Schema.createField(DataType.STRING, "ChildProperty2"));
		
		Schema listChildSchema = new Schema(null, 
				Schema.createField(DataType.INT, "ListIndex"), 
				Schema.createField(DataType.STRING, "ListProperty1"));
		
		Schema rootSchema = new Schema(null, 
				Schema.createField(DataType.STRING, "RootProperty1"), 
				Schema.createField(DataType.STRING, "RootProperty2"), 
				Schema.createTupleField("Child", childSchema),
				Schema.createListField("ChildList", CompleteDataType.forTuple(listChildSchema)));
		
		return rootSchema;
		
	}
	
	@Test
	/**
	 * At top level of schema only verify that found fields have the defined value (other fields in pattern are ignored)
	 */
	public void testTopLevel () throws StreamBaseException
	{
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\"}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);
		
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\"}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));
		
		String patternAsJSONString1 = "{\"RootProperty2\":\"rp2\"}";
		System.err.println("Pattern "+patternAsJSONString1);		
		FieldBasedTupleMatcher m1 = mb.makeMatcher(patternAsJSONString1);
		assertTrue(m1.matches(first));
		
		String patternAsJSONString2 = "{\"RootProperty1\":\"rpX\"}";
		System.err.println("Pattern "+patternAsJSONString2);		
		FieldBasedTupleMatcher m2 = mb.makeMatcher(patternAsJSONString2);
		assertFalse(m2.matches(first));
		
		// Pattern can't contain fields not in the schema
		String patternAsJSONString3 = "{\"RootProperty1\":\"rp1\", \"RootProperty3\":\"rp3\"}";
		System.err.println("Pattern "+patternAsJSONString3);
		boolean exceptionCaught = false;
		try {
			@SuppressWarnings("unused")

			FieldBasedTupleMatcher m3 = mb.makeMatcher(patternAsJSONString3);
		} catch (StreamBaseException e) {
			exceptionCaught  = true;
		}
		assertTrue( exceptionCaught );	
		
	}
	@Test
	/**
	 * For simple nesting of tuples it should be possible to have an empty child
	 */
	public void testNestEmptyTuple () throws StreamBaseException
	{
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\", \"Child\":{}}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\"}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));
		
		String patternAsJSONString1 = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\", \"Child\":{}}";
		System.err.println("Pattern "+patternAsJSONString1);		
		FieldBasedTupleMatcher m1 = mb.makeMatcher(patternAsJSONString1);
		assertTrue(m1.matches(first));

		// The empty child tuple shouldn't match a pattern for the child data
		String patternAsJSONString2 = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\"}}";
		System.err.println("Pattern "+patternAsJSONString2);		
		FieldBasedTupleMatcher m2 = mb.makeMatcher(patternAsJSONString2);
		assertFalse(m2.matches(first));
	}
	
	@Test
	/**
	 * For simple nesting of tuples check that found fields have the defined value (other fields in pattern are ignored)
	 */
	public void testSimpleNesting () throws StreamBaseException
	{
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\", \"ChildProperty2\":\"cp2\"}}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		// We don't care if there's a child or not
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\"}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));

		// There must be a child but we don't care what it contains
		String patternAsJSONString1 = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\", \"Child\":{}}";
		System.err.println("Pattern "+patternAsJSONString1);		
		FieldBasedTupleMatcher m1 = mb.makeMatcher(patternAsJSONString1);
		assertTrue(m1.matches(first));

		// There must be a child and we care what it contains
		String patternAsJSONString2 = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\"}}";
		System.err.println("Pattern "+patternAsJSONString2);		
		FieldBasedTupleMatcher m2 = mb.makeMatcher(patternAsJSONString2);
		assertTrue(m2.matches(first));

		// There must be a child and we care what it contains
		String patternAsJSONString3 = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cpX\"}}";
		System.err.println("Pattern "+patternAsJSONString3);		
		FieldBasedTupleMatcher m3 = mb.makeMatcher(patternAsJSONString3);
		assertFalse(m3.matches(first));
}

	@Test
	// We don't care if there's a list or not
	public void testNestedArray() throws StreamBaseException {
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", "
				+"\"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\", \"ChildProperty2\":\"cp2\"},"
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		// We don't care if there's a list or not
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\"}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));		
	}

	@Test
	// There must be a list, we don't care what it contains
	public void testNestedArray1() throws StreamBaseException {
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", "
				+"\"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\", \"ChildProperty2\":\"cp2\"},"
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		// There must be a list, we don't care what it contains
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\","
				+"\"ChildList\":[]}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));
	}

	@Test
	// There must be a list, we want it to contain at least element provided
	public void testNestedArray2() throws StreamBaseException {
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", "
				+"\"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\", \"ChildProperty2\":\"cp2\"},"
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		// There must be a list, we don't care what it contains
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\","
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"]}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));
	}
	
	@Test
	// There must be a list, we want it to contain the element provided
	public void testNestedArray3() throws StreamBaseException {
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", "
				+"\"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\", \"ChildProperty2\":\"cp2\"},"
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		// There must be a list, we want it to contain the elements provided
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\","
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));
	}

	@Test
	// The pattern looks for different values in the list than provided in the data.
	public void testNestedArray4() throws StreamBaseException {
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", "
				+"\"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\", \"ChildProperty2\":\"cp2\"},"
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		// The pattern looks for different values in the list than provided in the data.
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\","
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":3,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertFalse(m.matches(first));
	}
	
	@Test
	// There must be a list, we want it to contain the elements provided (and ignore missing ones)
	public void testNestedArray5() throws StreamBaseException {
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", "
				+"\"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\", \"ChildProperty2\":\"cp2\"},"
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		// There must be a list, we want it to contain the elements provided (and ignore missing ones)
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\","
				+"\"ChildList\":["
				+"{\"ListIndex\":0}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));
	}

	@Test
	// The pattern looks for the list elements in a different order than in the list.
	public void testNestedArray6() throws StreamBaseException {
		Schema nestSchema = makeNestedSchema();
		JSONMatcherBuilder mb = new JSONMatcherBuilder(nestSchema).ignoreMissingFields(true);
		
		String tupleAsJSONString = "{\"RootProperty1\":\"rp1\", "
				+"\"RootProperty2\":\"rp2\", \"Child\":{\"ChildProperty1\":\"cp1\", \"ChildProperty2\":\"cp2\"},"
				+"\"ChildList\":["
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"]}";
		System.err.println("Tuple "+tupleAsJSONString);
		List<Tuple> listOfTuples = TupleJSONUtil.getTuplesFromJSON(nestSchema, tupleAsJSONString);
		Tuple first = listOfTuples.get(0);

		// The pattern looks for different values in the list than provided in the data.
		String patternAsJSONString = "{\"RootProperty1\":\"rp1\", \"RootProperty2\":\"rp2\","
				+"\"ChildList\":["
				+"{\"ListIndex\":1,\"ListProperty1\":\"lp2\"}"
				+"{\"ListIndex\":0,\"ListProperty1\":\"lp1\"}"
				+"]}";
		System.err.println("Pattern "+patternAsJSONString);		
		FieldBasedTupleMatcher m = mb.makeMatcher(patternAsJSONString);
		assertTrue(m.matches(first));
	}

}
