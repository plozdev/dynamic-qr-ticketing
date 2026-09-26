package com.ticketing.platform.gatevalidator.infrastructure.adapter;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryReplayCheckAdapterTest {

    @Test
    void onlyOneConcurrentScanCanClaimTheSameToken() throws Exception {
        InMemoryReplayCheckAdapter adapter = new InMemoryReplayCheckAdapter();
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return adapter.markIfSeen("same-token", Duration.ofSeconds(60));
                }));
            }
            start.countDown();
            int firstScans = 0;
            for (Future<Boolean> result : results) {
                if (!result.get()) {
                    firstScans++;
                }
            }
            assertEquals(1, firstScans);
        }
    }
}
