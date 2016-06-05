package com.streambase.sbunit.ext.matcher.builder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import com.streambase.sb.AbstractFunction;
import com.streambase.sb.BasicFunction;
import com.streambase.sb.CompleteDataType.FunctionType;
import com.streambase.sb.Function;
import com.streambase.sb.Schema;
import com.streambase.sb.Schema.Field;
import com.streambase.sb.SchemaUtil;
import com.streambase.sb.Timestamp;
import com.streambase.sb.TupleJSONUtil.Options;
import com.streambase.sb.ByteArrayView;
import com.streambase.sb.CompleteDataType;
import com.streambase.sb.DataType;
import com.streambase.sb.StreamBaseException;
import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.Matchers;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;
import com.streambase.sb.internal.CoercedFunction;
import com.streambase.sb.internal.SchemaJSONUtil;
import com.streambase.sb.util.Msg;
import com.streambase.sb.util.Util;
import com.streambase.sb.util.Xml;
import com.streambase.sb.TupleJSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/** 
 * Build matchers to match a subset of a tuple's fields from JSON strings. Any fields that aren't mentioned
 * in the constructor are ignored.
 * 
 * Some of this code is "borrowed" from TupleJSONUtil, where there are some private classes "JSONFunctions" and setTuple() (molded to include matcher.*() methods).
 * 
 * IMPORTANT NOTE:  this code is single threaded (synchronized makeMatcher()) as handling of lists is non-reentrant (class global variable "handlingAlist").
 * This is OK as very high performance is not critical in Unit testing 
 * 
 * This matcher builder builds its matcher on a FieldBasedTupleMatcher, so that fields can be ignored, etc 
 * 
 */
public class JSONMatcherBuilder { 
	private final Schema completeSchema;
	private FieldBasedTupleMatcher matcher;
	private boolean ignoreMissingFields = false;
	SimpleDateFormat sdft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSZ");
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
	 * Note that either a null column or an empty string matches a null field
	 * 
	 * similar to TupleJSONUtil.getTuplesFromJSON()
	 * 
	 * @param columns String value for each column matching the field names in the constructor
	 * @return The Matcher for this JSON row.
	 * @throws StreamBaseException
	 */
	public synchronized FieldBasedTupleMatcher makeMatcher(String columns) throws StreamBaseException {		
		matcher = Matchers.emptyFieldMatcher();  // create an empty field matcher
		Tuple tuple = completeSchema.createTuple(); // parallel tuple for sub-tuple processing
		Object jsonObject = parseJSONString(columns);	// create JSON object from string	
		setTupleAndMatcher( tuple, jsonObject, null ); // fill in values, throw away the scratch tuple  
		return matcher;
	}
	

	/**
	 * create JSON object from string
	 * 
	 * TODO: The method parseJSONString(String) from the type TupleJSONUtil is not visible, so is recopied here; could remove this if public
	 * 
	 * @param JSONString
	 * @return The object into which the JSON string was parsed
	 * @throws StreamBaseException
	 */
    public static Object parseJSONString(String JSONString)
            throws StreamBaseException {
        Object jsonObject = null;
        try {
            jsonObject = JSON.parse(JSONString);
        } catch (JSONException e) {
            // Error seems kinda nasty for something that could just be bad input
            // remap to SBException
            String msg = e.getMessage();
            if (msg == null || !(
                    msg.contains("syntax error") || 
                    msg.contains("error parse") || 
                    msg.contains("TODO"))) {
                throw e;
            }
        }
        if (jsonObject == null) {
            throw new StreamBaseException("Invalid JSON string: " + JSONString);
        }	
        return jsonObject;
    }
    
    
    /**
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
    public Tuple setTupleAndMatcher( Tuple tuple, Object jsonObject, String fieldParent ) throws StreamBaseException {
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
             return readFunctionFromJSON((FunctionType) type, jsonObject, false);
         }


     });


  } // end of:  private  class JSONFunctions

  public static AbstractFunction readFunctionFromJSON(CompleteDataType cdt, Object jsonObject, boolean strict) throws StreamBaseException {
      return readFunctionFromJSON(cdt, jsonObject, strict, "");
  }
  
  public static AbstractFunction readFunctionFromJSON(CompleteDataType cdt, Object jsonObject, boolean strict, String timestampFormat) throws StreamBaseException {
      FunctionType type = (FunctionType) cdt;
      // do some validation
      if (! (jsonObject instanceof JSONObject)) {
          throw new StreamBaseException("The format for functions is JSON object");
      }                
      JSONObject obj = (JSONObject) jsonObject;
      HashSet<String> keys = new HashSet<String>(obj.keySet());
      if (keys.contains("inner")) {
          // this is a coerced function.
          return readCoercedFunctionFromJSON(type, obj);
      } else {
          if (!keys.contains("body") && !keys.contains("function_definition")) {
              throw new StreamBaseException("JSON functions must have a body or definition");
          }
          keys.remove("body");
          keys.remove("function_definition");
          keys.remove("environment");
          keys.remove("name");
          keys.remove("environmentSchema");
          if (!keys.isEmpty()) {
              throw new StreamBaseException("Unknown JSON attributes: "+keys.toString());
          }                    
          return readBasicFunctionFromJSON(type, obj, strict, timestampFormat);           
      }
  }
  
  private static AbstractFunction readCoercedFunctionFromJSON(FunctionType functionType, JSONObject obj) {
      try {
          Object inTransformer = obj.get("inTransformer");
          Object outTransformer = obj.get("outTransformer");
          FunctionType innerType = (FunctionType) SchemaJSONUtil.typeFromJSON(obj.get("innerType"));
          AbstractFunction inner = readFunctionFromJSON(innerType, (JSONObject) obj.get("inner"), true);
          return new CoercedFunction(functionType, inner, inTransformer, outTransformer);
      } catch (StreamBaseException ex) {
          throw new RuntimeException(ex);
      }
  }

  private static AbstractFunction readBasicFunctionFromJSON(FunctionType functionType, JSONObject obj, boolean strict, String timestampFormat) {
      try {
          Schema envSchema = SchemaUtil.EMPTY_SCHEMA;
          Tuple environment;
          String name;
          Tuple env = null;
          String stringRep;
          String body;
          
          if (obj.containsKey("environment")) {
              envSchema = SchemaJSONUtil.schemaFromJSON((JSONObject)obj.get("environmentSchema"));
              env = envSchema.createTuple();
              TupleJSONUtil.setTuple(env, obj.get("environment"), strict);
              environment = env.createReadOnlyTuple();
          } else {
              environment = SchemaUtil.EMPTY_SCHEMA.createTuple();
          }
          if (obj.containsKey("name")) {
              name = obj.getString("name");
          } else {
              name = null;
          }
          stringRep = obj.getString("function_definition");
          body = obj.getString("body");
          return new BasicFunction(functionType, environment, stringRep, body, name);
      } catch (StreamBaseException ex) {
          throw new RuntimeException("Malformed JSON string for function", ex);
      }
  }


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

}
