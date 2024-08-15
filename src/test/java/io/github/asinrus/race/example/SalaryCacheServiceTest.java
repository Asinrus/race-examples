package io.github.asinrus.race.example;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.asinrus.race.core.RaceTestSuitRegistry.race;
import static org.junit.jupiter.api.Assertions.*;

public class SalaryCacheServiceTest {

    SalaryCacheService sut = new SalaryCacheService(Map.of(
            "Mike", 100,
            "John", 1000,
            "Richard", 20
    ));

    @Test
    void increaseSalaryToDifferentPersons() {
        race(Map.of(
                "Mike", () -> sut.increaseSalary("Mike", 20),
                "John", () -> sut.increaseSalary("John", 20))
            )
                .withAssertion(result -> {
                    var mikeResult = result.get("Mike");
                    var johnResult = result.get("John");
                    assertFalse(mikeResult.isHasError() || johnResult.isHasError());
                })
                .go();
    }

    @Test
    void increaseSalaryToSamePerson() {
        race(Map.of(
                "Richard", () -> sut.increaseSalary("Richard", 20),
                "Richard2", () -> sut.increaseSalary("Richard", 20))
        )
                .withAssertion(result -> {
                    var richardResult = result.get("Richard");
                    var richard2Result = result.get("Richard2");
                    assertEquals(sut.getSalary("Richard"), 40);
                    assertTrue(richardResult.isHasError() || richard2Result.isHasError());
                })
                .go();
    }
}
