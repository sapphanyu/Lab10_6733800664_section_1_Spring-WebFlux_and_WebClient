package com.example.lab10;

import com.example.lab10.model.Product;
import com.example.lab10.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

/**
 * Lab10ApplicationTests — ทดสอบ Reactive code
 *
 * ✅ test findById() ทำเสร็จแล้วเป็นตัวอย่าง
 * ❌ TODO: เพิ่ม test สำหรับ method ที่นักศึกษาทำเอง
 *
 * StepVerifier — วิธีทดสอบ Mono/Flux:
 *   StepVerifier.create(mono/flux)
 *     .expectNext(value)     ← คาดหวังค่าที่ได้
 *     .expectNextCount(n)    ← คาดหวังจำนวน element
 *     .verifyComplete()      ← ยืนยัน onComplete
 *     .verifyError()         ← ยืนยัน onError
 */
@SpringBootTest
class Lab10ApplicationTests {

    @Autowired
    private ProductRepository repository;

    // ══════════════════════════════════════════════════════
    // ✅ ตัวอย่าง test — ศึกษาแล้วเพิ่ม test เอง
    // ══════════════════════════════════════════════════════

    @Test
    void contextLoads() {
        // Spring Application Context โหลดสำเร็จ
    }

    @Test
    void testFindById_found() {
        // ✅ ตัวอย่าง: ทดสอบ findById ที่พบข้อมูล
        StepVerifier.create(repository.findById("1"))
                .expectNextMatches(p -> p.getName().contains("iPhone"))
                .verifyComplete();
    }

    @Test
    void testFindById_notFound() {
        // ✅ ตัวอย่าง: ทดสอบ findById ที่ไม่พบข้อมูล
        StepVerifier.create(repository.findById("999"))
                .verifyComplete(); // Mono.empty() → onComplete ทันที
    }

    @Autowired
    private com.example.lab10.service.ProductService service;

    // ══════════════════════════════════════════════════════
    // ❌ TODO: เพิ่ม test ด้านล่างนี้
    // ══════════════════════════════════════════════════════

    @Test
    void testFindAll() {
        StepVerifier.create(repository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testSave() {
        Product newProduct = new Product("4", "iPad Air M2", "Electronics", "Apple", 10, 23900.0, "MEMBER");
        StepVerifier.create(repository.save(newProduct))
                .expectNextMatches(p -> p.getId().equals("4") && p.getName().equals("iPad Air M2"))
                .verifyComplete();
    }

    @Test
    void testFindByCategory() {
        StepVerifier.create(repository.findByCategory("Electronics"))
                .thenConsumeWhile(p -> "Electronics".equalsIgnoreCase(p.getCategory()))
                .verifyComplete();
    }

    @Test
    void testServiceGetById_notFound() {
        StepVerifier.create(service.getById("9999"))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().contains("Product not found: 9999"))
                .verify();
    }

    @Test
    void testServiceGetDiscountedPrice() {
        // Product 1: iPhone 15 Pro, price 39900.0, discountType "MEMBER" (10% off -> 35910.0)
        StepVerifier.create(service.getDiscountedPrice("1"))
                .expectNext(35910.0)
                .verifyComplete();
    }

    @Test
    void testReactiveOperatorChaining() {
        // ทดสอบการ chain operators: map -> filter -> defaultIfEmpty
        StepVerifier.create(
                service.getById("2")
                        .map(Product::getPrice)
                        .filter(price -> price < 10000.0)
                        .defaultIfEmpty(0.0)
        )
        .expectNext(0.0)
        .verifyComplete();
    }
}
