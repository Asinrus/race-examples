package io.github.asinrus.race.example;

import io.github.asinrus.race.core.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static io.github.asinrus.race.core.RaceTestSuitRegistry.race;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PostgresSQLContainerTest {

    @Autowired
    CustomerService customerService;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");

    @Test
    void testWithoutNaming() {
        race(() -> customerService.changeName(1L, "John"))
                .withConfiguration(Configuration.builder()
                        .setNumThreads(2)
                        .build())
                .withAssertion(executionResult -> {
                    var errors = executionResult.errors();
                    var results = executionResult.results();
                    assertFalse(errors.isEmpty());
                    assertEquals(1, results.size());
                })
                .go();
    }

    @Test
    void testWithNaming() {
        race(Map.of
                ("Mike", () -> customerService.changeName(1L, "Mike"),
                        "Derek", () -> customerService.changeName(1L, "Derek")))
                .withAssertion(executionResult -> {
                    var derekResult = executionResult.get("Derek");
                    var mikeResult = executionResult.get("Mike");
                    var oneContainsError = mikeResult.isHasError() ^ derekResult.isHasError();
                    assertTrue(oneContainsError);
                })
                .go();
    }
}
