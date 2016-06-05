package com.streambase.sbunit.ext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.streambase.sb.CompleteDataType;
import com.streambase.sb.DataType;
import com.streambase.sb.Schema;
import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.matchers.AnythingMatcher;
import com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher;
import com.streambase.sbunit.ext.matchers.NothingMatcher;
import com.streambase.sbunit.ext.matchers.tuple.AllTupleMatcher;
import com.streambase.sbunit.ext.matchers.tuple.AnyTupleMatcher;
import com.streambase.sbunit.ext.matchers.tuple.NotTupleMatcher;
import com.streambase.sbunit.ext.matchers.value.AllValueMatcher;
import com.streambase.sbunit.ext.matchers.value.AnyValueMatcher;
import com.streambase.sbunit.ext.matchers.value.EqualsValueMatcher;
import com.streambase.sbunit.ext.matchers.value.ListValueMatcher;
import com.streambase.sbunit.ext.matchers.value.NonNullValueMatcher;
import com.streambase.sbunit.ext.matchers.value.NotValueMatcher;
import com.streambase.sbunit.ext.matchers.value.NullValueMatcher;
import com.streambase.sbunit.ext.matchers.value.RatioTestDoubleValueMatcher;

/**
 * Factory and utility methods for {@link TupleMatcher}
 * and {@link ValueMatcher} classes.
 */
public class Matchers {
    /**
     * create a {@link TupleMatcher} that will match if and only if at
     * least one of the argument {@link TupleMatcher}s does.
     */
    public static TupleMatcher anyOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AnyTupleMatcher(m, matchers);
    }
    
    /**
     * create a {@link TupleMatcher} that will match if and only if all
     * of the argument {@link TupleMatcher}s does.
     */
    public static TupleMatcher allOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AllTupleMatcher(m, matchers);
    }
    
    /**
     * create a {@link ValueMatcher} that will match if and only if at
     * least one of the argument {@link ValueMatcher}s does.
     */
    public static ValueMatcher anyOf(ValueMatcher m, ValueMatcher... matchers ) {
        return new AnyValueMatcher(m, matchers);
    }
    
    /**
     * create a {@link ValueMatcher} that will match if and only if all
     * of the argument {@link ValueMatcher}s does.
     */
    public static ValueMatcher allOf(ValueMatcher m, ValueMatcher... matchers ) {
        return new AllValueMatcher(m, matchers);
    }
    
    /**
     * create a {@link TupleMatcher} that will match if and only if the
     * argument {@link TupleMatcher} does not.
     */
    public static TupleMatcher not(TupleMatcher m) {
        return new NotTupleMatcher(m);
    }
    
    /**
     * create a {@link ValueMatcher} that will match if and only if the
     * argument {@link ValueMatcher} does not.
     */
    public static ValueMatcher not(ValueMatcher m) {
        return new NotValueMatcher(m);
    }
    
    /**
     * create an {@link AnythingMatcher} that will always match
     */
    public static AnythingMatcher anything() {
        return new AnythingMatcher();
    }
    
    /**
     * create a {@link NothingMatcher} that will never match
     */
    public static NothingMatcher nothing() {
        return new NothingMatcher();
    }
    
    /**
     * create a {@link ValueMatcher} that will match anything non-null
     */
    public static NonNullValueMatcher isNonNull() {
        return new NonNullValueMatcher();
    }
    
    /**
     * create a {@link ValueMatcher} that will only match null values
     */
    public static NullValueMatcher isNull() {
        return new NullValueMatcher();
    }
    
    /**
     * create a {@link ListValueMatcher} that will match a list of values
     * which match each matcher
     */
    public static ListValueMatcher list(List<? extends ValueMatcher> matchers) {
        return new ListValueMatcher(matchers);
    }
    
    /**
     * create a {@link ListValueMatcher} that will match a list of values
     * which match each matcher
     */
    public static ListValueMatcher list(ValueMatcher... matchers) {
        return list(Arrays.asList(matchers));
    }
    
    /**
     * create a {@link ListValueMatcher} that will match a list of values
     * which match each value
     */
    public static ListValueMatcher list(Object... values) {
        List<ValueMatcher> res = new ArrayList<ValueMatcher>();
        for (Object o : values) {
            res.add(literal(o));
        }
        return list(res);
    }
    
    /**
     * a {@link ValueMatcher} that will match the object.
     * <p/>
     * NOTE: This method will attempt to do the correct thing, returning
     * {@link RatioTestDoubleValueMatcher} for doubles and properly trawling
     * the hierarchy of the provided Object to create a suitably composed
     * {@link ValueMatcher}
     */
    public static ValueMatcher literal(Object o) {
        return forType(null, o);
    }
    
    /**
     * create a {@link FieldBasedTupleMatcher} that will match the Tuple exactly.
     * <p/>
     * NOTE: This method will attempt to do the correct thing, returning
     * {@link RatioTestDoubleValueMatcher} for doubles and properly trawling
     * the hierarchy of the provided {@link Tuple} to create a suitably 
     * composed {@link FieldBasedTupleMatcher}
     */
    public static FieldBasedTupleMatcher literal(Tuple t) {
        LinkedHashMap<String, ValueMatcher> matchers = new LinkedHashMap<String, ValueMatcher>();
        buildMatcher(matchers, t, "");
        return FieldBasedTupleMatcher.of(matchers);
    }
    
    /**
     * create a {@link FieldBasedTupleMatcher} that will match any Tuple.
     * <p/>
     * This is usually used as a base for more complicated matchers.
     */
    public static FieldBasedTupleMatcher emptyFieldMatcher() {
        return FieldBasedTupleMatcher.empty();
    }
    
    private static void buildMatcher(LinkedHashMap<String, ValueMatcher> res, Tuple t, String baseName) {
        for (Schema.Field f : t.getSchema().getFields()) {
            Object o = t.getField(f);
            if (f.getDataType() == DataType.TUPLE) {
                buildMatcher(res, (Tuple) o, baseName + f.getName() + ".");
            } else {
                res.put(baseName + f.getName(), forType(f.getCompleteDataType(), o));
            }
        }
    }
    
    private static ValueMatcher forType(CompleteDataType fullType, Object val) {
        if (val == null) {
            return new NullValueMatcher();
        }
        
        // try really hard to figure out the types involved
        DataType t;
        String typeString = null;
        if (fullType != null) {
            t = fullType.getDataType();
            typeString = fullType.toHumanString();
        } else {
            t = DataType.forType(val);
            if (t == null) {
                return new EqualsValueMatcher(val);
            }
            typeString = t.toString();
        }
        
        switch (t) {
        case BLOB:
        case BOOL:
        case INT:
        case LONG:
        case STRING:
        case TIMESTAMP:
        case FUNCTION:
            return new EqualsValueMatcher(val);
        case DOUBLE:
            if (val instanceof Number) {
                return new RatioTestDoubleValueMatcher(((Number) val).doubleValue());
            }
            break;
        case LIST:
            if (val instanceof List) {
                CompleteDataType elementType = null;
                if (fullType != null) {
                    elementType = fullType.getElementType();
                }
                
                List<ValueMatcher> submatchers = new ArrayList<ValueMatcher>();
                for (Object s : (List<?>)val) {
                    submatchers.add(forType(elementType, s));
                }
                return new ListValueMatcher(submatchers);
            }
            break;
        case TUPLE:
            if (val instanceof Tuple) {
                return literal((Tuple)val);
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Cannot create a ValueMatcher for the StreamBase type "
                    + typeString);
        }
        throw new IllegalArgumentException(MessageFormat.format(
                "Cannot match a {0} type against an object of type {1}",
                typeString, val.getClass().getSimpleName()));
    }

    
}
