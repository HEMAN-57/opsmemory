package com.opsmemory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "hindsight.api-url=http://localhost:8888",
        "hindsight.bank-id=test-bank"
})
class OpsMemoryApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context can start
    }
}
