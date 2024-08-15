package io.github.asinrus.race.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static io.github.asinrus.race.core.RaceTestSuitRegistry.race;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MySQLContainerTest {

    @Autowired
    CustomerService customerService;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:oraclelinux8");

    @Test
    void testWithoutNaming() {
        race(() -> customerService.changeName(1L, "John"))
                .withAssertion(executionResult -> {
                    var errors = executionResult.errors();
                    var results = executionResult.results();
                    assertTrue(errors.isEmpty());
                    assertEquals(2, results.size());
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
                    assertTrue(!derekResult.isHasError() && !mikeResult.isHasError());
                })
                .go();
    }
}
