package com.in.service;

import com.in.App;
import com.in.entity.Bid;
import com.in.entity.Product;
import com.in.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(
    classes = App.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:auction-concurrency;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "server.port=0"
    }
)
public class BidConcurrencyIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BidService bidService;

    @Test
    public void shouldKeepSingleWinningBidWithConcurrentWriters() throws Exception {
        User seller = userService.createUser(
            "seller-concurrent-" + System.nanoTime(),
            "seller123",
            "seller-concurrent-" + System.nanoTime() + "@mail.com",
            "Concurrent Seller",
            "9999999999",
            User.UserRole.SELLER
        );

        Product product = productService.createProduct(
            "Concurrent GPU",
            "High-value auction item",
            new BigDecimal("1000.00"),
            null,
            LocalDateTime.now().minusMinutes(2),
            LocalDateTime.now().plusMinutes(30),
            seller,
            null
        );
        product.setStatus(Product.ProductStatus.APPROVED);
        product = productService.updateProduct(product);
        final Integer productId = product.getId();

        int bidderCount = 60;
        List<User> buyers = new ArrayList<>();
        for (int i = 0; i < bidderCount; i++) {
            buyers.add(
                userService.createUser(
                    "buyer-concurrent-" + i + "-" + System.nanoTime(),
                    "buyer123",
                    "buyer-concurrent-" + i + "-" + System.nanoTime() + "@mail.com",
                    "Buyer " + i,
                    "90000000" + i,
                    User.UserRole.BUYER
                )
            );
        }

        ExecutorService executor = Executors.newFixedThreadPool(bidderCount);
        CountDownLatch ready = new CountDownLatch(bidderCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger(0);
        AtomicInteger conflicts = new AtomicInteger(0);

        for (int i = 0; i < bidderCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    BigDecimal amount = new BigDecimal("1000.00").add(new BigDecimal(idx + 1));
                    bidService.placeBid(productId, buyers.get(idx).getId(), amount);
                    accepted.incrementAndGet();
                } catch (RuntimeException ex) {
                    // Expected for stale bids that lose the race.
                    conflicts.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Assertions.assertTrue(ready.await(15, TimeUnit.SECONDS), "All bidder threads should be ready");
        start.countDown();

        executor.shutdown();
        Assertions.assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS), "Executor should finish in time");

        Product refreshedProduct = productService.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product missing"));
        Optional<Bid> highestOpt = bidService.findHighestBidForProduct(refreshedProduct);

        Assertions.assertTrue(accepted.get() > 0, "There should be at least one accepted bid");
        Assertions.assertTrue(conflicts.get() > 0, "There should be at least one conflict under load");
        Assertions.assertTrue(highestOpt.isPresent(), "Highest bid should exist");

        Bid highestBid = highestOpt.get();
        Assertions.assertEquals(1,
            bidService.findBidsByProduct(refreshedProduct).stream()
                .filter(b -> b.getStatus() == Bid.BidStatus.WINNING)
                .count(),
            "Exactly one bid must remain winning");

        Assertions.assertEquals(highestBid.getBidAmount(), refreshedProduct.getCurrentHighestBid(),
            "Product highest bid should match winning bid");
    }
}
