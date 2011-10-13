A collection of extensions and utility routing to assist in writing tests for
StreamBase applications.

There are a few main classes which provide the lion's share of the flexibility
in this framework. Most interactions will be centered around creating and
configuring these classes.

com.streambase.sbunit.ext.Matchers
    provides factory routines for building up matchers.
    
com.streambase.sbunit.ext.StreamMatcher
    is the primary class used to expect values on dequeuers.
    
com.streambase.sbunit.ext.matchers.FieldBasedTupleMatcher
    provides a variety of routines to selectively ignore, require, and
    validate fields.

