package com.streambase.sbunit.ext;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.streambase.sb.unittest.CSVTupleMaker;
import com.streambase.sb.unittest.Enqueuer;
import com.streambase.sb.unittest.SBServerManager;
import com.streambase.sb.unittest.ServerManagerFactory;
import com.streambase.sbunit.ext.StreamMatcher.ExtraTuples;

public class TestStreamMatcher {
    private static SBServerManager server;

    @BeforeClass
    public static void setupServer() throws Exception {
        // create a StreamBase server and load applications once for all tests
        // I know the apps are all stateless, so I am going to start them
        // and keep them running the entire time
        server = ServerManagerFactory.getEmbeddedServer();
        server.startServer();
        server.loadApp("passthrough.sbapp");
        server.startContainers();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        if (server != null) {
            server.stopContainers();
            server.shutdownServer();
            server = null;
        }
    }

    @Test
    public void testExpectNTuples() throws Exception {
        final int TEST_TIMEOUT_MS = 50;
        Enqueuer enqueuer = server.getEnqueuer("InputStream");

        StreamMatcher matcher = StreamMatcher
		        .on(server.getDequeuer("OutputStream"))
		        .onExtra(ExtraTuples.ERROR)
		        .timeout(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        enqueuer.enqueue(CSVTupleMaker.MAKER, "1,2", "3,4");
        long start = System.currentTimeMillis();
        matcher.expectTuples(2);
        long finish = System.currentTimeMillis();
        Assert.assertTrue("expectNothing() does not need to wait for success", finish - start < TEST_TIMEOUT_MS);
        
        enqueuer.enqueue(CSVTupleMaker.MAKER, "1,2");
        start = System.currentTimeMillis();
        try {
            matcher.expectTuples(2);
            Assert.fail("expectTuples() should have failed");
        } catch (AssertionError e) {
            // ok
        }
        finish = System.currentTimeMillis();
        Assert.assertTrue("expectNothing() must wait for a failure", finish - start > TEST_TIMEOUT_MS);
    }
    
    @Test
    public void testExpectNothing() throws Exception {
        final int TEST_TIMEOUT_MS = 50;
        StreamMatcher matcher = StreamMatcher
		        .on(server.getDequeuer("OutputStream"))
		        .timeout(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        long start = System.currentTimeMillis();
        matcher.expectNothing();
        long finish = System.currentTimeMillis();
        Assert.assertTrue("expectNothing() must wait for success", finish - start > TEST_TIMEOUT_MS);
        
        Enqueuer enqueuer = server.getEnqueuer("InputStream");
        enqueuer.enqueue(CSVTupleMaker.MAKER, "1,2", "3,4");
        
        start = System.currentTimeMillis();
        try {
            matcher.expectNothing();
            Assert.fail("expectNothing() should have failed");
        } catch (AssertionError e) {
            // ok
        }
        finish = System.currentTimeMillis();
        Assert.assertTrue("expectNothing() does not need to wait for a failure", finish - start < TEST_TIMEOUT_MS);
    }
}
