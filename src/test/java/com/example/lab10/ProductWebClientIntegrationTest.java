package com.example.lab10;

import com.example.lab10.client.ProductWebClient;
import com.example.lab10.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductWebClientIntegrationTest {

    @LocalServerPort
    private int port;

    private ProductWebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = new ProductWebClient("http://localhost:" + port);
    }

    @Test
    void testGetProductById() {
        StepVerifier.create(webClient.getProductById("1"))
                .expectNextMatches(p -> p.getId().equals("1") && p.getName().contains("673380066-4"))
                .verifyComplete();
    }

    @Test
    void testGetAllProducts() {
        StepVerifier.create(webClient.getAllProducts())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testGetByCategory() {
        StepVerifier.create(webClient.getByCategory("Electronics"))
                .thenConsumeWhile(p -> "Electronics".equalsIgnoreCase(p.getCategory()))
                .verifyComplete();
    }

    @Test
    void testGetDiscountedPrice() {
        StepVerifier.create(webClient.getDiscountedPrice("1"))
                .expectNext(35910.0)
                .verifyComplete();
    }

    @Test
    void testCreateAndDeleteProduct() {
        Product prod = new Product(null, "Client Test Headphone", "Audio", "Sony", 5, 8900.0, "NONE");

        Product created = webClient.createProduct(prod).block();
        org.junit.jupiter.api.Assertions.assertNotNull(created);
        org.junit.jupiter.api.Assertions.assertNotNull(created.getId());

        StepVerifier.create(webClient.deleteProduct(created.getId()))
                .verifyComplete();
    }

    @Test
    void testWebClientChainingWithSubscribe() {
        AtomicBoolean consumed = new AtomicBoolean(false);

        // ทดสอบ chain operators: map -> defaultIfEmpty -> subscribe
        webClient.getProductById("1")
                .map(Product::getName)
                .defaultIfEmpty("Unknown")
                .subscribe(name -> {
                    System.out.println("Subscribed product name: " + name);
                    consumed.set(true);
                });

        // รอสักครู่ให้ reactive pipeline ทำงาน
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        assertTrue(consumed.get());
    }
}
