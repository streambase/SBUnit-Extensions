package com.streambase.sbunit.ext;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.streambase.sb.CompleteDataType;
import com.streambase.sb.Tuple;
import com.streambase.sbunit.ext.matchers.tuple.AllMatcher;
import com.streambase.sbunit.ext.matchers.tuple.AnyMatcher;
import com.streambase.sbunit.ext.matchers.tuple.AnythingMatcher;
import com.streambase.sbunit.ext.matchers.tuple.FieldBasedTupleMatcher;
import com.streambase.sbunit.ext.matchers.tuple.NotMatcher;
import com.streambase.sbunit.ext.matchers.tuple.NothingMatcher;
import com.streambase.sbunit.ext.matchers.value.EqualsValueMatcher;
import com.streambase.sbunit.ext.matchers.value.ListValueMatcher;
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
        return new AnyMatcher(m, matchers);
    }
    
    /**
     * @return a single {@link TupleMatcher} that will match if and only if all
     * of the argument {@link TupleMatcher}s does.
     */
    public static TupleMatcher allOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AllMatcher(m, matchers);
    }
    
    /**
     * @return a single {@link TupleMatcher} that will match if and only if the
     * argument {@link TupleMatcher}s does not.
     */
    public static TupleMatcher not(TupleMatcher m) {
        return new NotMatcher(m);
    }
    
    /**
     * @return a {@link TupleMatcher} that will match anything.
     */
    public static TupleMatcher anything() {
        return new AnythingMatcher();
    }
    
    /**
     * @return a {@link TupleMatcher} that will never match.
     */
    public static TupleMatcher nothing() {
        return new NothingMatcher();
    }
    
    public static ValueMatcher forType(CompleteDataType type, Object val) {
        if (val == null) {
            return new NullValueMatcher();
        }
        switch (type.getDataType()) {
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
                List<ValueMatcher> submatchers = new ArrayList<ValueMatcher>();
                for (Object s : (List<?>)val) {
                    submatchers.add(forType(type.getElementType(), s));
                }
                return new ListValueMatcher(submatchers);
            }
            break;
        case TUPLE:
            if (val instanceof Tuple) {
                return FieldBasedTupleMatcher.forTuple((Tuple)val);
            }
            break;
        default:
            throw new IllegalArgumentException(
                    "Cannot create a ValueMatcher for the StreamBase type "
                    + type.toHumanString());
        }
        throw new IllegalArgumentException(MessageFormat.format(
                "Cannot match a {0} type against an object of type {1}",
                type.toHumanString(), val.getClass().getSimpleName()));
    }
    
    
}
