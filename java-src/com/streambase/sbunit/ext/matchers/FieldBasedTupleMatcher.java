package com.streambase.sbunit.ext.matchers;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.streambase.sb.Tuple;
import com.streambase.sb.TupleException;
import com.streambase.sbunit.ext.Matchers;
import com.streambase.sbunit.ext.TupleMatcher;
import com.streambase.sbunit.ext.ValueMatcher;

/**
 * {@link FieldBasedTupleMatcher} matches tuples by comparing their fields against 
 * {@link ValueMatcher}s.  It can be used as a {@link ValueMatcher} for sub-tuples 
 * as well.  More complicated behavior can be achieved by calling configuration 
 * methods.  For example:
 * <p/>
 * <pre>
 * FieldBasedTupleMatcher m = FieldBasedTupleMatcher.of(t)
 *          .ignoreNulls()
 *          .require("name", "Steven")
 *          .requireNonNull("value");
 * </pre>
 * <p/>
 * <b>Warning: {@link FieldBasedTupleMatcher} instances are always immutable</b>; 
 * a configuration method such as code>ignoreNulls</code> has no effect on the 
 * instance it is invoked on.
 */
public class FieldBasedTupleMatcher implements TupleMatcher, ValueMatcher, IgnoreNullTransform {
    private final LinkedHashMap<String, ValueMatcher> matchers;
    
    private FieldBasedTupleMatcher(LinkedHashMap<String, ValueMatcher> matchers) {
        this.matchers = matchers;
    }
    
    /**
     * get a {@link FieldBasedTupleMatcher} which will match anything.
     * <p/>
     * This is usually used as a base for more complicated matchers.
     */
    public static FieldBasedTupleMatcher empty() {
        return new FieldBasedTupleMatcher(new LinkedHashMap<String, ValueMatcher>());
    }
    
    /**
     * get a {@link FieldBasedTupleMatcher} with all the provided matchers.
     * @param matchers  A map of field names to matchers.
     */
    public static FieldBasedTupleMatcher of(LinkedHashMap<String, ValueMatcher> matchers) {
        return new FieldBasedTupleMatcher(new LinkedHashMap<String, ValueMatcher>(matchers));
    }
    
    /**
     * get an identical {@link FieldBasedTupleMatcher} to this, except
     * that it will ignore any null values that it is <b>currently</b> 
     * configured to enforce.
     */
    @Override
    public FieldBasedTupleMatcher ignoreNulls() {
        LinkedHashMap<String, ValueMatcher> newMatchers = new LinkedHashMap<String, ValueMatcher>();
        for (Map.Entry<String, ValueMatcher> e : matchers.entrySet()) {
        	ValueMatcher vm = e.getValue();
            if (vm instanceof IgnoreNullTransform) {
                vm = ((IgnoreNullTransform) vm).ignoreNulls();
            }
            if (vm != null) {
            	newMatchers.put(e.getKey(), vm);
            }
        }
        return new FieldBasedTupleMatcher(newMatchers);
    }
    
    /**
     * get an identical {@link FieldBasedTupleMatcher} to this, except
     * that it will ignore the field identified by <code>field</code>
     */
    public FieldBasedTupleMatcher ignore(String field) {
        LinkedHashMap<String, ValueMatcher> newMatchers = new LinkedHashMap<String, ValueMatcher>(matchers);
        newMatchers.remove(field);
        return new FieldBasedTupleMatcher(newMatchers);
    }
    
    /**
     * get an identical {@link FieldBasedTupleMatcher} to this, except
     * that it will require the field identified by <code>field</code> to match
     * <code>m</code>
     */
    public FieldBasedTupleMatcher require(String field, ValueMatcher m) {
        LinkedHashMap<String, ValueMatcher> newMatchers = new LinkedHashMap<String, ValueMatcher>(matchers);
        newMatchers.put(field, m);
        return new FieldBasedTupleMatcher(newMatchers);
    }
    
    /**
     * get an identical {@link FieldBasedTupleMatcher} to this, except
     * that it will require the field identified by <code>field</code> to match
     * the literal <code>val</code>.
     * <p/>
     * Equivalent to <code>require(field, Matchers.literal(val))</code>.
     */
    public FieldBasedTupleMatcher require(String field, Object val) {
        return require(field, Matchers.literal(val));
    }
    
    /**
     * get an identical {@link FieldBasedTupleMatcher} to this, except
     * that it will require the field identified by <code>field</code> to be
     * null.
     * <p/>
     * Equivalent to <code>require(field, Matchers.isNull())</code>.
     */ 
    public FieldBasedTupleMatcher requireNull(String field) {
        return require(field, Matchers.isNull());
    }
    
    /**
     * get an identical {@link FieldBasedTupleMatcher} to this, except
     * that it will require the field identified by <code>field</code> to be
     * non-null.
     * <p/>
     * Equivalent to <code>require(field, Matchers.isNonNull())</code>.
     */ 
    public FieldBasedTupleMatcher requireNonNull(String field) {
        return require(field, Matchers.isNonNull());
    }
    
    
    @Override
    public boolean matches(Object field) throws TupleException {
        if (field instanceof Tuple) {
            return matches((Tuple)field);
        }
        return false;
    }


    @Override
    public boolean matches(Tuple t) throws TupleException {
        for (Map.Entry<String, ValueMatcher> e : matchers.entrySet()) {
            Object field = t.getField(e.getKey());
            boolean matches = e.getValue().matches(field);
            if (!matches) {
                return false;
            }
        }
        return true;
    }
    

    @Override
    public JsonElement describe(Gson gson) {
    	JsonObject obj = new JsonObject();
        for (Map.Entry<String, ValueMatcher> e : matchers.entrySet()) {
            obj.add(e.getKey(), e.getValue().describe(gson));
        }
        return obj;
    }
}
