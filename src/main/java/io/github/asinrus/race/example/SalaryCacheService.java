package io.github.asinrus.race.example;

import java.util.Map;

public class SalaryCacheService extends DummyCacheWithLocks<String, Integer> {

    protected SalaryCacheService(Map<String, Integer> cache) {
        super(cache);
    }

    public Integer increaseSalary(String name, Integer delta) {
        return withLock(name, v -> {
            // long computations
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return v + delta;
        });
    }

    public Integer getSalary(String name) {
        return get(name);
    }
}
