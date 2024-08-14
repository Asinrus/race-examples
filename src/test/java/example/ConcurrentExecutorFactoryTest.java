package example;

import io.github.asinrus.race.core.Configuration;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static io.github.asinrus.race.core.RaceTestSuitRegistry.race;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConcurrentExecutorFactoryTest {

    @Test
    void simpleTest() {
        class Holder {
            int i;
        }
        var holder = new Holder();
        race(() ->
        {
            int i = holder.i++;
            if (i != 0) {
                throw new RuntimeException("I'm the second. NOOOOO");
            }
            return i;
        })
                .withConfiguration(
                        Configuration.builder()
                                .setTimeout(Duration.of(4, ChronoUnit.SECONDS))
                                .build()
                )
                .withAssertion(executionResult -> {
                    Collection<Integer> res = executionResult.results();
                    Collection<Throwable> throwable = executionResult.errors();
                    assertFalse(throwable.isEmpty());
                    assertEquals(1, res.size());
                })
                .go();

    }

}