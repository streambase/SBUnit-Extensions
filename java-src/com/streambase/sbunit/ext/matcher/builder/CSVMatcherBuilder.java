package com.streambase.sbunit.ext.matcher.builder;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import com.streambase.sb.Schema;
import com.streambase.sb.StreamBaseException;
import com.streambase.sb.Tuple;
import com.streambase.sb.adapter.common.csv.CSVTupleReader;
import com.streambase.sb.adapter.common.csv.RFC4180FormatException;
import com.streambase.sbunit.ext.Matchers;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;


/** 
 * Build matchers to match a subset of a tuple's fields from CSV strings. Any fields that aren't mentioned
 * in the constructor are ignored.
 */
public class CSVMatcherBuilder {
	private final String [] fieldNames;
	private final Schema s;
	
	public CSVMatcherBuilder(Schema s, String... fieldNames) {
		this.s = s;
		this.fieldNames = fieldNames;
	}
	
	/**
	 * Build a matcher from this CSV text that matches only the fields given to the constructor.
	 * 
	 * Note that an empty column is null, e.g. "hi,,there" has null for the 2nd column. The
	 * corresponding field in the matched tuple must be null.
	 * 
	 * @return The Matcher for this CSV row.
	 * @throws StreamBaseException
	 */
	public FieldBasedTupleMatcher makeMatcher(String csvText) throws StreamBaseException {
		try {
			return makeMatcher(parseCSV(csvText, ',', '\''));
		} catch (IOException e) {
			throw new StreamBaseException(e);
		}
	}
	
	/**
	 * Build a matcher from string values that matches only the fields given to the constructor.
	 * 
	 * Note that either a null column or an empty string matches a null field
	 * 
	 * @param colums String value for each column matching the field names in the constructor
	 * @return The Matcher for this CSV row.
	 * @throws StreamBaseException
	 */
	public FieldBasedTupleMatcher makeMatcher(String... columns) throws StreamBaseException {
		if(fieldNames.length != columns.length) {
			throw new StreamBaseException(MessageFormat.format("field lengh {0} != column length {1}", fieldNames.length, columns.length));
		}
		
		FieldBasedTupleMatcher m = Matchers.emptyFieldMatcher();
		
		for(int i=0; i < fieldNames.length; ++i) {
			if(columns[i] == null || columns[i].length() == 0) {
				m = m.requireNull(fieldNames[i]);
			} else {
				Tuple t = s.createTuple();
				
				t.setField(fieldNames[i], columns[i]);
				m = m.require(fieldNames[i], t.getField(fieldNames[i]));
			}
		}
		
		return m;
	}
	
    /**
     * Convenience method for parsing 1 row of csv. If you have many rows to parse, it's more efficient to make a CSVTupleReader from a StringReader
     * and call readRecord.  
     * @throws IOException 
     * @throws RFC4180FormatException 
     */
    private static String [] parseCSV(String csvText, char delimiter, char quoteChar) throws RFC4180FormatException, IOException {
    	CSVTupleReader reader = new CSVTupleReader(new StringReader(csvText), delimiter, quoteChar);
    	    	
    	return reader.readRecord();
    }

}
