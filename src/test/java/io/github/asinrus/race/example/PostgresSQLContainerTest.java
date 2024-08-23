package io.github.asinrus.race.example;

import io.github.asinrus.race.core.Configuration;
import io.github.asinrus.race.core.domain.result.ComplexExecutionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static io.github.asinrus.race.core.RaceTestSuitRegistry.race;
import static io.github.asinrus.race.core.RaceTestSuitRegistry.raceByFutures;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
       Map<String, Callable<String>> tasks = Map.of
                ("Mike", () -> customerService.changeName(1L, "Mike"),
                        "Derek", () -> customerService.changeName(1L, "Derek"));
        race(tasks)
                .withAssertion(standardResultOfConflictedOperations("Mike", "Derek"))
                .go();
    }

    @Test
    void testWithNamingAsync() {
        raceByFutures(Map.of
                ("Tom", customerService.changeNameAsync(1L, "Tom"),
                        "Joshua", customerService.changeNameAsync(1L, "Joshua")))
                .withAssertion(standardResultOfConflictedOperations("Tom", "Joshua"))
                .go();
    }

    private Consumer<ComplexExecutionResult<String, String>> standardResultOfConflictedOperations(String op1Key, String op2Key) {
        return (result) -> {
            var op1Result = result.get(op1Key);
            var op2Result = result.get(op2Key);
            var oneContainsError = op1Result.isHasError() ^ op2Result.isHasError();
            assertTrue(oneContainsError);

            var exception = op1Result.error() == null
                    ? op2Result.error()
                    : op1Result.error();
            assertNotNull(exception);
            assertInstanceOf(CannotAcquireLockException.class, exception);
        };
    }
}
