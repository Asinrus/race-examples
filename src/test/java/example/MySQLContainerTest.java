package example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
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
    void run() {
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
    void complexTest() {
        race(Map.of
                ("John", () -> customerService.changeName(1L, "John"),
                        "Derek", () -> customerService.changeName(1L, "Derek")))
                .withAssertion(executionResult -> {
                    var derekResult = executionResult.get("Derek");
                    var johnResult = executionResult.get("John");
                    assertTrue(!derekResult.isHasError() && !johnResult.isHasError());
                })
                .go();
    }
}
