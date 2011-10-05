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
     * @return a single {@link TupleMatcher} that will match if and only if at
     * least one of the argument {@link TupleMatcher}s does.
     */
    public static TupleMatcher anyOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AnyTupleMatcher(m, matchers);
    }
    
    /**
     * @return a single {@link TupleMatcher} that will match if and only if all
     * of the argument {@link TupleMatcher}s does.
     */
    public static TupleMatcher allOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AllTupleMatcher(m, matchers);
    }
    
    /**
     * @return a single {@link ValueMatcher} that will match if and only if at
     * least one of the argument {@link ValueMatcher}s does.
     */
    public static ValueMatcher anyOf(ValueMatcher m, ValueMatcher... matchers ) {
        return new AnyValueMatcher(m, matchers);
    }
    
    /**
     * @return a single {@link ValueMatcher} that will match if and only if all
     * of the argument {@link ValueMatcher}s does.
     */
    public static ValueMatcher allOf(ValueMatcher m, ValueMatcher... matchers ) {
        return new AllValueMatcher(m, matchers);
    }
    
    /**
     * @return a {@link TupleMatcher} that will match if and only if the
     * argument {@link TupleMatcher}s does not.
     */
    public static TupleMatcher not(TupleMatcher m) {
        return new NotTupleMatcher(m);
    }
    
    /**
     * @return a {@link ValueMatcher} that will match if and only if the
     * argument {@link ValueMatcher}s does not.
     */
    public static ValueMatcher not(ValueMatcher m) {
        return new NotValueMatcher(m);
    }
    
    /**
     * @return a {@link AnythingMatcher} that will always match
     */
    public static AnythingMatcher anything() {
        return new AnythingMatcher();
    }
    
    /**
     * @return a {@link NothingMatcher} that will never match
     */
    public static NothingMatcher nothing() {
        return new NothingMatcher();
    }
    
    /**
     * @return a {@link ValueMatcher} that will match anything non-null
     */
    public static NonNullValueMatcher isNonNull() {
        return new NonNullValueMatcher();
    }
    
    /**
     * @return a {@link ValueMatcher} that will match anything null
     */
    public static NullValueMatcher isNull() {
        return new NullValueMatcher();
    }
    
    /**
     * Create a list matcher which expects each matcher in sequence
     */
    public static ListValueMatcher list(List<? extends ValueMatcher> matchers) {
        return new ListValueMatcher(matchers);
    }
    
    /**
     * Create a list matcher which expects each matcher in sequence
     */
    public static ListValueMatcher list(ValueMatcher... matchers) {
        return list(Arrays.asList(matchers));
    }
    
    /**
     * Create a list matcher which expects each value in sequence
     */
    public static ListValueMatcher list(Object... values) {
        List<ValueMatcher> res = new ArrayList<ValueMatcher>();
        for (Object o : values) {
            res.add(literal(o));
        }
        return list(res);
    }
    
    /**
     * @return a {@link ValueMatcher} that will match the object
     */
    public static ValueMatcher literal(Object o) {
        return forType(null, o);
    }
    
    /**
     * @return a {@link FieldBasedTupleMatcher} that will match the Tuple
     * exactly
     */
    public static FieldBasedTupleMatcher literal(Tuple t) {
        LinkedHashMap<String, ValueMatcher> matchers = new LinkedHashMap<String, ValueMatcher>();
        buildMatcher(matchers, t, "");
        return FieldBasedTupleMatcher.of(matchers);
    }
    
    /**
     * @return a {@link FieldBasedTupleMatcher} that will match any Tuple.
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
