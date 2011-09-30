package com.streambase.sbunit.ext;

import com.streambase.sb.Tuple;

public interface TupleMatcher {
    
    public boolean matches(Tuple a);
    
    public String describe();
}
