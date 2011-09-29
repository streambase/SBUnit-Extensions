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

public class TestStreamMatcher {
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
        } catch (AssertionError e) {
            // ok
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
        } catch (AssertionError e) {
            // ok
        }
        finish = System.currentTimeMillis();
        assertFast("expectNothing() does not need to wait for a failure", start, finish);
    }
}
