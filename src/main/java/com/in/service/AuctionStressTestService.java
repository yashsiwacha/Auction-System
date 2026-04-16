package com.in.service;

import com.in.entity.Product;
import com.in.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AuctionStressTestService {

    public static class StressTestJob {
        private String jobId;
        private Integer auctionId;
        private int totalRequests;
        private int concurrency;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String status;
        private String message;
        private int successCount;
        private int failureCount;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public Integer getAuctionId() {
            return auctionId;
        }

        public void setAuctionId(Integer auctionId) {
            this.auctionId = auctionId;
        }

        public int getTotalRequests() {
            return totalRequests;
        }

        public void setTotalRequests(int totalRequests) {
            this.totalRequests = totalRequests;
        }

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        public LocalDateTime getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private BidService bidService;

    @Value("${stress.max-total-requests:20000}")
    private int maxTotalRequests;

    @Value("${stress.max-concurrency:256}")
    private int maxConcurrency;

    @Value("${stress.job-timeout-seconds:900}")
    private int jobTimeoutSeconds;

    private final ConcurrentHashMap<String, StressTestJob> jobs = new ConcurrentHashMap<>();
    private final Deque<String> recentJobIds = new ArrayDeque<>();
    private final ExecutorService orchestrator = Executors.newFixedThreadPool(2);

    public String startStressTest(Integer auctionId, Integer totalRequests, Integer concurrency) {
        Product product = productService.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (!product.isAuctionActive()) {
            throw new RuntimeException("Auction is not active for stress testing");
        }

        List<User> buyers = activeBuyers();
        if (buyers.isEmpty()) {
            throw new RuntimeException("No active buyers available for stress test");
        }

        int normalizedTotal = normalizeTotalRequests(totalRequests);
        int normalizedConcurrency = normalizeConcurrency(concurrency);

        StressTestJob job = new StressTestJob();
        job.setJobId(UUID.randomUUID().toString());
        job.setAuctionId(auctionId);
        job.setTotalRequests(normalizedTotal);
        job.setConcurrency(normalizedConcurrency);
        job.setStartedAt(LocalDateTime.now());
        job.setStatus("RUNNING");
        job.setMessage("Stress test started");

        jobs.put(job.getJobId(), job);
        rememberRecentJob(job.getJobId());

        orchestrator.submit(() -> runJob(job, buyers));
        return job.getJobId();
    }

    public Optional<StressTestJob> getJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public List<StressTestJob> recentJobs() {
        List<StressTestJob> list = new ArrayList<>();
        synchronized (recentJobIds) {
            for (String jobId : recentJobIds) {
                StressTestJob job = jobs.get(jobId);
                if (job != null) {
                    list.add(job);
                }
            }
        }
        return list;
    }

    @PreDestroy
    public void shutdown() {
        orchestrator.shutdown();
    }

    private void runJob(StressTestJob job, List<User> buyers) {
        ExecutorService workers = Executors.newFixedThreadPool(job.getConcurrency());
        CountDownLatch done = new CountDownLatch(job.getTotalRequests());
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();

        Optional<Product> productOpt = productService.findById(job.getAuctionId());
        BigDecimal startingHighest = productOpt
            .map(Product::getCurrentHighestBid)
            .orElse(new BigDecimal("0.00"));

        AtomicReference<BigDecimal> localHighest = new AtomicReference<>(scale(startingHighest));

        for (int i = 0; i < job.getTotalRequests(); i++) {
            final int index = i;
            workers.submit(() -> {
                try {
                    User bidder = buyers.get(index % buyers.size());
                    BigDecimal increment = incrementFor(index);

                    boolean placed = false;
                    for (int attempt = 0; attempt < 2 && !placed; attempt++) {
                        BigDecimal amount = scale(localHighest.get().add(increment));
                        try {
                            bidService.placeBid(job.getAuctionId(), bidder, amount);
                            localHighest.updateAndGet(existing -> amount.compareTo(existing) > 0 ? amount : existing);
                            success.incrementAndGet();
                            placed = true;
                        } catch (Exception bidError) {
                            Optional<Product> latestProduct = productService.findById(job.getAuctionId());
                            if (latestProduct.isPresent() && latestProduct.get().getCurrentHighestBid() != null) {
                                localHighest.set(scale(latestProduct.get().getCurrentHighestBid()));
                            }
                            if (attempt == 1) {
                                failure.incrementAndGet();
                            }
                        }
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        workers.shutdown();

        try {
            boolean completed = done.await(jobTimeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                job.setStatus("TIMEOUT");
                job.setMessage("Stress test timed out before all requests completed");
            } else {
                job.setStatus("COMPLETED");
                job.setMessage("Stress test completed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            job.setStatus("FAILED");
            job.setMessage("Stress test interrupted");
        } finally {
            workers.shutdownNow();
            job.setSuccessCount(success.get());
            job.setFailureCount(failure.get());
            job.setCompletedAt(LocalDateTime.now());
        }
    }

    private List<User> activeBuyers() {
        List<User> buyers = userService.findByRole(User.UserRole.BUYER);
        List<User> active = new ArrayList<>();
        for (User buyer : buyers) {
            if (buyer.getIsActive() != null && buyer.getIsActive()) {
                active.add(buyer);
            }
        }
        return active;
    }

    private int normalizeTotalRequests(Integer requested) {
        int value = requested == null ? 10000 : requested;
        if (value < 1) {
            value = 1;
        }
        return Math.min(value, maxTotalRequests);
    }

    private int normalizeConcurrency(Integer requested) {
        int value = requested == null ? 100 : requested;
        if (value < 1) {
            value = 1;
        }
        return Math.min(value, maxConcurrency);
    }

    private BigDecimal incrementFor(int index) {
        int bucket = (index % 5) + 1;
        return new BigDecimal(bucket).multiply(new BigDecimal("0.01"));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void rememberRecentJob(String jobId) {
        synchronized (recentJobIds) {
            recentJobIds.addFirst(jobId);
            while (recentJobIds.size() > 10) {
                recentJobIds.removeLast();
            }
        }
    }
}

