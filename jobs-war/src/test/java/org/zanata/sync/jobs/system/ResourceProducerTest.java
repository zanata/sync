package org.zanata.sync.jobs.system;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ResourceProducerTest {
    private static final Logger log =
            LoggerFactory.getLogger(ResourceProducerTest.class);
    private ResourceProducer resourceProducer;

    @Before
    public void setUp() {
        resourceProducer = new ResourceProducer();
    }


    @Test
    @Ignore("just want to see how resteasy client behave in multithreaded environment")
    public void testResteasyClientInMultiThreadedEnvironment()
            throws InterruptedException {
        Client client = resourceProducer.client(2);
        List<Callable<Integer>> tasks = new ArrayList<>();
        int numOfThreads = 10;
        for (int i = 0; i < numOfThreads; i++) {
            tasks.add(newCallable(client, "http://httpstat.us/200"));
        }
        ExecutorService executorService =
                Executors.newFixedThreadPool(numOfThreads);
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        futures.forEach(future -> {
            try {
                Integer status = future.get();
                assertThat(status).isEqualTo(200);
            } catch (InterruptedException | ExecutionException e) {
                log.error("error getting result", e);
                fail(e.getMessage());
            }
        });

    }

    private Callable<Integer> newCallable(Client client, String url) {
        return () -> {
            Response response = null;
            try {
                response = client.target(url)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE).get();
                return response.getStatus();
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }

}
