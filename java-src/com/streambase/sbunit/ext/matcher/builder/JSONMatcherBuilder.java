package com.streambase.sbunit.ext.matcher.builder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.TreeSet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.streambase.sb.AbstractFunction;
import com.streambase.sb.ByteArrayView;
import com.streambase.sb.CompleteDataType;
import com.streambase.sb.CompleteDataType.FunctionType;
import com.streambase.sb.DataType;
import com.streambase.sb.Function;
import com.streambase.sb.Schema;
import com.streambase.sb.Schema.Field;
import com.streambase.sb.StreamBaseException;
import com.streambase.sb.Timestamp;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleJSONUtil;
import com.streambase.sb.TupleJSONUtil.Options;
import com.streambase.sb.util.Msg;
import com.streambase.sb.util.Util;
import com.streambase.sb.util.Xml;
import com.streambase.sbunit.ext.Matchers;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;

/** 
 * Build matchers to match a subset of a tuple's fields from JSON strings. Any fields that aren't mentioned
 * in the constructor are ignored.
 * 
 * Some of this code is similar to that in com.streambase.sb.TupleJSONUtil, where there are some private classes
 * "JSONFunctions" and setTuple() (molded to include matcher.*() methods).
 * 
 * IMPORTANT NOTE:  this code is single threaded (synchronized makeMatcher()) as
 * handling of lists is non-reentrant (class global variable "handlingAlist").
 * This is OK as very high performance is not critical in Unit testing 
 * 
 * This matcher builder builds its matcher as a FieldBasedTupleMatcher, so that fields can be ignored, etc.
 * 
 * Timestamp value strings are parsed using java.util.SimpleDateFormat yyyy-MM-dd hh:mm:ss.SSSZ by default
 * 
 */
public class JSONMatcherBuilder { 
	private final Schema completeSchema;
	private FieldBasedTupleMatcher matcher;
	private boolean ignoreMissingFields = false;
	private String timestampFormat = "yyyy-MM-dd hh:mm:ss.SSSZ";
	SimpleDateFormat sdft = new SimpleDateFormat(getTimestampFormat());
	ArrayList<String> subFieldHandled = new ArrayList<String>();   // list of field that are of type "tuple"; don't want to "match.require" their parent, in case matching is "sparse"
	private boolean handlingAlist = false; // when handling lists, do not add a child node to matcher, only the top, parent node.  this is also true of lists of tuples
	
	/**
	 * create a Matcher that has a companion partial JSON string that mentions each node that will be of interest in the match 
	 * @param inSchema
	 */
	public JSONMatcherBuilder( Schema inSchema ) {
		this.completeSchema = inSchema;
	}
	
	/**
	 * Build a matcher from string values that matches only the fields given to the constructor.
	 * 
	 * Either a null or empty string matches a null field value
	 * 
	 * Behaves similarly to com.streambase.sb.TupleJSONUtil.getTuplesFromJSON(). That is, if the string
	 * represents a JSON object, then it is interpreted 
	 * as corresponding to the structure of the fields in tuple's schema and are mapped according to
	 * field names. If the string represents a JSON array, the values in the array are interpreted to
	 * correspond with the tuple fields' position in the tuple schema and are mapped by position. 
	 * 
	 * @param jsonString JSON string value specifying which field values to match
	 * @return The Matcher that corresponds to the specified JSON object.
	 * @throws StreamBaseException
	 */
	public synchronized FieldBasedTupleMatcher makeMatcher(String jsonString) throws StreamBaseException {		
		matcher = Matchers.emptyFieldMatcher();  // create an empty field matcher
		Tuple tuple = completeSchema.createTuple(); // parallel tuple for sub-tuple processing
		Object jsonObject = TupleJSONUtil.parseJSONString(jsonString);	// create JSON object from string	
		setTupleAndMatcher( tuple, jsonObject, null ); // fill in values, throw away the scratch tuple  
		return matcher;
	}
    
    /*
     * Set the fields of a tuple based on the contents of a JSON object
     * -- similar to TupleJSONUtil.setTuple(), but handles depth of matching in subtuples
     * 
     * TODO: is there a way to do similar for lists?  This is not evident in FieldBasedTupleMatcher, so a bit tricky i think
     * 
     * @param tuple the tuple to set
     * @param jsonObject the JSON object; must have at least the fields of the tuple's schema
     * @throws JSONException
     * @throws StreamBaseException 
     */
    private Tuple setTupleAndMatcher( Tuple tuple, Object jsonObject, String fieldParent ) throws StreamBaseException {
    	TreeSet<String> missingInJSONString = new TreeSet<String>();  
    	Schema subSchema = tuple.getSchema();
        if (jsonObject instanceof JSONObject) {
            JSONObject jsonTuple = (JSONObject) jsonObject;
            for (Field field: subSchema.getFields()) {
            	String fieldName = field.getName();
                Object jsonField = jsonTuple.get(fieldName);
                String fullFieldName = ((fieldParent == null) || (handlingAlist) )?  fieldName : (fieldParent + "." + fieldName);
                if (jsonField != null) {
                	try {
                		Object o = jsonFunctions.get(field).convertJsonToTuple(fieldName, field.getCompleteDataType(), jsonField, fullFieldName );
                		// adjust for long field type (JSON only handles int), or for string that is not really a string (Timestamp)
                		if ( field.getDataType().equals(DataType.LONG)) {
                			o = new Long( ((Integer)o).longValue() );
                		} else if ( field.getDataType().equals(DataType.TIMESTAMP)) {
                			o = new Timestamp(sdft.parse((String)o));
                		}
                		if(o == null) {
                			if ((! handlingAlist) && (! subFieldHandled.contains(fullFieldName)) ){
                				matcher.requireNull(fullFieldName); // do not place into matcher if this is part of a list array, that is handled later
                			}
                			tuple.setNull(fieldName);
                		} else {
                			if ((! handlingAlist) && (! subFieldHandled.contains(fullFieldName)) ) {
                				matcher = matcher.require( fullFieldName, o );
                			}
                			tuple.setField( fieldName, o );
                		}
                	} catch (ClassCastException ex) {
                        throw new StreamBaseException(ex);
                    } catch (ParseException e) {
                    	throw new StreamBaseException(e);
					}
                } else 
                	if ( ignoreMissingFields ) { 
                	// TODO: fix "bug" in FieldBasedTupleMatcher: ignore will only work if field is first defined (.requireNull() is one way) 
                	matcher = matcher.requireNull(fullFieldName);  // this fixes the bug where ".ignore()" expects the matcher field to already be defined
                	matcher = matcher.ignore(fullFieldName);
                	tuple.setNull( fieldName );
                } else {
                	missingInJSONString.add( fullFieldName );
                }
            }
            if (!ignoreMissingFields && !missingInJSONString.isEmpty()) { // print error listing missing, mandatory fields
                StringBuilder err = new StringBuilder();
                err.append("Error setting tuple with schema ");
                err.append(subSchema.toHumanString());
                err.append(" from JSON string ");
                err.append(jsonTuple.toString());
                err.append(". The following ").append(missingInJSONString.size() > 1 ? "fields do" : "field does");
                err.append(" not exist in the JSON string ('use .ignoreMissingFields(true)) if appropriate: ");
                err.append(Util.join(", ", missingInJSONString));
                throw new StreamBaseException(err.toString());
            }
            
            // might as well give them a sorted error message
            TreeSet<String> unknownFields = new TreeSet<String>();
            for (Object jsonKey : jsonTuple.keySet()) {
                if ( subSchema.getFieldIndex(jsonKey.toString()) == Schema.NO_SUCH_FIELD) {
                    unknownFields.add(jsonKey.toString());
                }
            }
            
            if (!unknownFields.isEmpty()) {
                StringBuilder err = new StringBuilder();
                err.append("Error setting tuple with schema ");
                err.append(subSchema.toHumanString());
                err.append(" from JSON string ");
                err.append(jsonTuple.toString());
                err.append(". The following ").append(unknownFields.size() > 1 ? "fields do" : "field does");
                err.append(" not exist in the schema: ");
                err.append(Util.join(", ", unknownFields));
                throw new StreamBaseException(err.toString());
            }
        } else if (jsonObject instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonObject;
            if (jsonArray.size() != subSchema.getFieldCount()) { 
                throw new StreamBaseException(Msg.format(
                        "Error setting tuple with schema {0} from JSON string {1}. " +
                        "Tuple has {2} fields, JSON has {3} fields", 
                        subSchema.toHumanString(), jsonArray.toString(),
                        subSchema.getFieldCount(), jsonArray.size()));
            }
            for (int i = 0; i < jsonArray.size(); ++i) {
                Object jsonField = jsonArray.get(i);
                Schema.Field field = subSchema.getField(i);
                if (jsonField == null) {
                	matcher = matcher.require(field.getName(), matcher);
                } else {
                	matcher = matcher.ignore(field.getName());
                }
            }
        } else {
            throw new StreamBaseException("Unexpected type for jsonObject: " + jsonObject.getClass().getName());
        }
        return tuple;
    }

 
  /**
  * DataType.Registry machinery in support of TupleUtil.toJSONObject
  * >> similar to TupleJSONUtil, but handles depth of matching in subtuples
  */
 private  class JSONFunctions implements DataType.Registry.Functor {
     /**
      * Convert a Tuple object to a JSON object.
      */
     Object convertTupleToJson(CompleteDataType type, Object tupleObject, EnumSet<Options> options) throws StreamBaseException {
         return tupleObject;
     }
     
     /**
      * Convert a JSON object to a tuple.  If the item could come from a list or a map, handle both.
      */
     Object convertJsonToTuple(String fieldName, CompleteDataType type, Object jsonObject, String fieldParent) throws StreamBaseException {
         return jsonObject;
     }
 }
 private  DataType.Registry<JSONFunctions> jsonFunctions = 
     new DataType.Registry<JSONFunctions>( new JSONFunctions(), DataType.BOOL, DataType.LONG, DataType.STRING); 
  {
     jsonFunctions.register(DataType.DOUBLE, new JSONFunctions() { 
         @Override 
         Object convertTupleToJson(CompleteDataType type, Object tupleObject, EnumSet<Options> options) throws StreamBaseException { 
             if (Double.POSITIVE_INFINITY == (Double)tupleObject) { 
                 return "Infinity"; 
             } 
             if (Double.NEGATIVE_INFINITY == (Double)tupleObject) { 
                 return "-Infinity"; 
             } 
             if (Double.isNaN((Double)tupleObject)) { 
                 return "NaN"; 
             } 
             return super.convertTupleToJson(type, tupleObject, options); 
          } 
          @Override 
          Object convertJsonToTuple(String fieldName, CompleteDataType type, Object jsonObject, String fieldParent ) 
         		 throws StreamBaseException { 
              if ("Infinity".equals(jsonObject)) {
                  return Double.POSITIVE_INFINITY; 
              }
              if ("-Infinity".equals(jsonObject)) { 
                  return Double.NEGATIVE_INFINITY; 
              }
              if ("NaN".equals(jsonObject)) { 
                  return Double.NaN; 
              }
              if (jsonObject instanceof Number) {
                  return ((Number)jsonObject).doubleValue(); 
              }
              return super.convertJsonToTuple( (fieldParent+fieldName), type, jsonObject, fieldParent);
          } 
     }); 

     jsonFunctions.register(DataType.BLOB, new JSONFunctions() {
         @Override
         Object convertTupleToJson(CompleteDataType type, Object tupleObject, EnumSet<Options> options) {
             return ((ByteArrayView) tupleObject).asString();
         }
         @Override
         Object convertJsonToTuple(String fieldName, CompleteDataType type, Object jsonObject, String fieldParent) throws StreamBaseException {
             return ByteArrayView.makeView(((String) jsonObject).getBytes());
         }
     });
     jsonFunctions.register(DataType.INT, new JSONFunctions() {
         @Override
         Object convertTupleToJson(CompleteDataType type, Object tupleObject, EnumSet<Options> options) {
             return ((Integer)tupleObject).longValue();
         }
         
         @Override
         Object convertJsonToTuple(String fieldName, CompleteDataType type, Object jsonObject, String fieldParent) throws StreamBaseException {
         	return ((Number)jsonObject).intValue();
         }
     });
     jsonFunctions.register(DataType.TIMESTAMP, new JSONFunctions() {
         @Override
         Object convertTupleToJson(CompleteDataType type, Object tupleObject, EnumSet<Options> options) {
             return tupleObject.toString();
         }
     });
     jsonFunctions.register(DataType.TUPLE, new JSONFunctions() {
         @Override
         Object convertTupleToJson(CompleteDataType type, Object tupleObject, EnumSet<Options> options) throws StreamBaseException {
             return TupleJSONUtil.toJSONObject((Tuple)tupleObject, options);
         }
         
         @Override
         Object convertJsonToTuple(String fieldName, CompleteDataType type, Object jsonObject, String fieldParent) throws StreamBaseException {
             Tuple tupleObject = type.getSchema().createTuple();
             /*if (fieldParent != null)*/ setTupleAndMatcher( tupleObject, jsonObject, fieldParent ); 
             subFieldHandled.add( fieldParent ); // don't want to redo "match.require(parent)", in case matching fields are "sparse"
             return tupleObject;
         }
         
     });
     jsonFunctions.register(DataType.LIST, new JSONFunctions() {
         @Override
         Object convertTupleToJson(CompleteDataType type, Object tupleObject, EnumSet<Options> options) throws StreamBaseException {
             List<?> tList = (List<?>) tupleObject;
             JSONArray jList = new JSONArray();
             JSONFunctions elemConvert = jsonFunctions.get(type.getElementType());
             for (Object item: tList) {
                 if (item == null) {
                     jList.add(null);
                 } else {
                     jList.add(elemConvert.convertTupleToJson(type.getElementType(), item, options));
                 }
             }
             return jList;
         }
         
         @Override
         Object convertJsonToTuple(String fieldName, CompleteDataType type, Object jsonObject, String fieldParent) throws StreamBaseException {
             List<?> jsonList = (List<?>)jsonObject;
             List<Object> tupleList = new ArrayList<Object>(jsonList.size());
             
             JSONFunctions elemConvert = jsonFunctions.get(type.getElementType());
             handlingAlist = true;
             for (Object item: jsonList) {
                 if (item == null) {
                     tupleList.add(null);
                 } else {
                     tupleList.add(elemConvert.convertJsonToTuple(fieldName, type.getElementType(), item, null));
                 }
             }
             handlingAlist = false;
//             matcher.require( fieldName, tupleList );  
             return tupleList;
         }
     });
     
     jsonFunctions.register(DataType.CAPTURE, new JSONFunctions() {
         @Override
         Object convertTupleToJson(CompleteDataType type, Object tupleObject, EnumSet<Options> options) throws StreamBaseException {                
             Tuple capt = (Tuple) tupleObject;
             JSONObject jsonCapt = new JSONObject(true);
             jsonCapt.put("schema", Xml.serialize(capt.getSchema().as_xml()));
             jsonCapt.put("value", TupleJSONUtil.toJSONObject(capt, options));
             return jsonCapt;
         }
         
         @Override
         Object convertJsonToTuple(String fieldName, CompleteDataType type, Object jsonObject, String fieldParent) throws StreamBaseException {
             JSONObject obj = (JSONObject) jsonObject;
             Schema s = new Schema(obj.getString("schema"));
             Tuple t = s.createTuple();
             setTupleAndMatcher( t, obj.get("value"), fieldParent );
             return t;
         }
     });

     jsonFunctions.register(DataType.FUNCTION, new JSONFunctions() {
         @Override
         Object convertTupleToJson(CompleteDataType type, Object functionObject, EnumSet<Options> options) throws StreamBaseException {
             // All our function implementations extend AbstractFunction
             AbstractFunction f = (AbstractFunction) functionObject;
             return f.getJSON();
         }
         
         @Override
         Function convertJsonToTuple(String fieldName, CompleteDataType type, Object jsonObject, String fieldParent) throws StreamBaseException {
             return TupleJSONUtil.readFunctionFromJSON((FunctionType) type, jsonObject, false);
         }


     });


  } // end of:  private  class JSONFunctions

  public FieldBasedTupleMatcher getMatcher() {
	  return matcher;
  }

  public boolean isIgnoreMissingFields() {
	  return ignoreMissingFields;
  }

  public void setIgnoreMissingFields(boolean ignoreMissingFields) {
	  this.ignoreMissingFields = ignoreMissingFields;
  }

  public JSONMatcherBuilder ignoreMissingFields(boolean ignoreMissingFields) {
	  this.ignoreMissingFields = ignoreMissingFields;
	  return this;
  }

  public String getTimestampFormat() {
	  return timestampFormat;
  }

  public void setTimestampFormat(String timestampFormat) {
	  this.timestampFormat = timestampFormat;
	  this.sdft = new SimpleDateFormat(getTimestampFormat());
  }

}
