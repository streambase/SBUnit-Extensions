package com.streambase.sbunit.ext;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.streambase.sb.unittest.CSVTupleMaker;
import com.streambase.sb.unittest.Dequeuer;
import com.streambase.sb.unittest.Enqueuer;
import com.streambase.sb.unittest.SBServerManager;
import com.streambase.sb.unittest.ServerManagerFactory;
import com.streambase.sbunit.ext.StreamMatcher.ExtraTuples;
import com.streambase.sbunit.ext.StreamMatcher.Ordering;

public class StreamMatcherTest {
    private static final int TEST_TIMEOUT_MS = 50;
    
    private static SBServerManager server;
    private static Enqueuer enqueuer;
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
        enqueuer = server.getEnqueuer("InputStream");
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
    
    private static void assertFast(String msg, long start, long finish) {
        Assert.assertTrue(msg, finish - start <= TEST_TIMEOUT_MS);
    }
    
    private static void assertSlow(String msg, long start, long finish) {
        Assert.assertTrue(msg, finish - start >= TEST_TIMEOUT_MS);
    }

    @Test
    public void testExpectNTuples() throws Exception {
        StreamMatcher matcher = StreamMatcher
		        .on(dequeuer)
		        .timeout(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        enqueuer.enqueue(CSVTupleMaker.MAKER, "1,2", "3,4");
        long start = System.currentTimeMillis();
        matcher.expectTuples(2);
        long finish = System.currentTimeMillis();
        assertFast("expectTuples() does not need to wait for success", start, finish);
        
        enqueuer.enqueue(CSVTupleMaker.MAKER, "1,2");
        start = System.currentTimeMillis();
        try {
            matcher.expectTuples(2);
            Assert.fail("expectTuples() should have failed");
        } catch (ExpectTuplesFailure f) {
            ErrorReport report = f.getReport();
            Assert.assertEquals(1, report.getMissingMatchers().size());
            Assert.assertEquals(0, report.getUnexpectedTuples().size());
            Assert.assertEquals(1, report.getFoundTuples().size());
        }
        finish = System.currentTimeMillis();
        assertSlow("expectTuples() must wait for a failure", start, finish);
    }
    
    @Test
    public void testExpectNothing() throws Exception {
        StreamMatcher matcher = StreamMatcher
		        .on(dequeuer)
		        .timeout(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        long start = System.currentTimeMillis();
        matcher.expectNothing();
        long finish = System.currentTimeMillis();
        assertSlow("expectNothing() must wait for success", start, finish);
        
        enqueuer.enqueue(CSVTupleMaker.MAKER, "1,2");
        
        start = System.currentTimeMillis();
        try {
            matcher.expectNothing();
            Assert.fail("expectNothing() should have failed");
        } catch (ExpectTuplesFailure f) {
            ErrorReport report = f.getReport();
            Assert.assertEquals(0, report.getMissingMatchers().size());
            Assert.assertEquals(1, report.getUnexpectedTuples().size());
            Assert.assertEquals(0, report.getFoundTuples().size());
        }
        finish = System.currentTimeMillis();
        assertFast("expectNothing() does not need to wait for a failure", start, finish);
    }
    
    @Test
    public void testExpectFields() throws Exception {
    	StreamMatcher matcher = StreamMatcher.on(dequeuer)
    			.onExtra(ExtraTuples.IGNORE)
    			.ordering(Ordering.UNORDERED)
    			.automaticTimeout();
    	
    	
        enqueuer.enqueue(CSVTupleMaker.MAKER, "1,4", "1,2", "2,4", "1,3");
        
        TupleMatcher m1 = Matchers
		        .emptyFieldMatcher()
        		.require("x", 1)
        		.require("y", Matchers.not(Matchers.literal(4)));
        TupleMatcher m2 = Matchers
	        .emptyFieldMatcher()
			.require("x", 2)
			.require("y", 4);
        matcher.expectTuples(m1, m2, m1);
    }
}
