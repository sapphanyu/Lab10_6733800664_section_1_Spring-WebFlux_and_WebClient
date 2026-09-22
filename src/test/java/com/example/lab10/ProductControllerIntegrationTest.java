package com.example.lab10;

import com.example.lab10.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ProductControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetAllProducts() {
        webTestClient.get()
                .uri("/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Product.class)
                .hasSize(3);
    }

    @Test
    void testGetProductById() {
        webTestClient.get()
                .uri("/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.name").value(org.hamcrest.Matchers.containsString("673380066-4"))
                .jsonPath("$.price").isEqualTo(39900.0);
    }

    @Test
    void testGetByCategory() {
        webTestClient.get()
                .uri("/products/category/Electronics")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .consumeWith(response -> {
                    response.getResponseBody().forEach(product -> {
                        org.junit.jupiter.api.Assertions.assertEquals("Electronics", product.getCategory());
                    });
                });
    }

    @Test
    void testGetDiscountedPrice() {
        // Product 1: 39900 * 0.90 = 35910.0 (MEMBER)
        webTestClient.get()
                .uri("/products/1/price")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Double.class).isEqualTo(35910.0);
    }

    @Test
    void testCreateAndDeleteProduct() {
        Product newProduct = new Product(null, "Test Device", "Gadget", "Acme", 10, 5000.0, "NONE");

        // 1. POST /products
        Product created = webTestClient.post()
                .uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .returnResult()
                .getResponseBody();

        org.junit.jupiter.api.Assertions.assertNotNull(created);
        org.junit.jupiter.api.Assertions.assertNotNull(created.getId());
        org.junit.jupiter.api.Assertions.assertEquals("Test Device", created.getName());

        // 2. DELETE /products/{id}
        webTestClient.delete()
                .uri("/products/" + created.getId())
                .exchange()
                .expectStatus().isOk();

        // 3. GET /products/{id} -> should return error / 500
        webTestClient.get()
                .uri("/products/" + created.getId())
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
