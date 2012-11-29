package com.streambase.sbunit.ext;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.streambase.sb.unittest.Dequeuer;
import com.streambase.sb.unittest.JSONSingleQuotesTupleMaker;
import com.streambase.sb.unittest.SBServerManager;
import com.streambase.sb.unittest.ServerManagerFactory;
import com.streambase.sbunit.ext.StreamMatcher.ExtraTuples;
import com.streambase.sbunit.ext.StreamMatcher.Ordering;
import com.streambase.sbunit.ext.matcher.builder.JSONMatcherBuilder;

public class JSONStreamMatcherTest {    
    private static SBServerManager server;
    private static Dequeuer dequeuer;

    @BeforeClass
    public static void setupServer() throws Exception {
        // create a StreamBase server and load applications once for all tests
        // I know the apps are all stateless, so I am going to start them
        // and keep them running the entire time
        server = ServerManagerFactory.getEmbeddedServer();
        server.startServer();
        server.loadApp("passthrough.sbapp");
        server.startContainers();
        dequeuer = server.getDequeuer("OutputStream");
    }

    @AfterClass
    public static void stopServer() throws Exception {
        if (server != null) {
            server.stopContainers();
            server.shutdownServer();
            server = null;
        }
    }
    
    @Before
    public void drain() throws Exception {
        dequeuer.drain();
    }
    
   
    @Test
    public void testExpectFields() throws Exception {
    	StreamMatcher matcher = StreamMatcher.on(dequeuer).onExtra(ExtraTuples.IGNORE).ordering(Ordering.ORDERED).automaticTimeout();
    	ArrayList<String> JSONstrings = new ArrayList<String>(Arrays.asList("{'x':1,'y':4}", "{'x':1,'y':2}", "{'x':2,'y':4}", "{'x':1,'y':3}")); 
    	
    	server.getEnqueuer("InputStream").enqueue( JSONSingleQuotesTupleMaker.MAKER, JSONstrings );
        
        JSONMatcherBuilder mb = new JSONMatcherBuilder( server.getDequeuer("OutputStream").getSchema() ); 
    	ArrayList<TupleMatcher> matchers = new ArrayList<TupleMatcher>(); 
    	for ( String jsonS : JSONstrings ) matchers.add( mb.makeMatcher(jsonS));

        matcher.expectTuples( matchers );
    }
}
