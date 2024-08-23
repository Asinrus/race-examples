package io.github.asinrus.race.example;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Dummy implementation without considering corner cases. Just a simple example for demonstrating
 *
 * @param <K>
 * @param <V>
 */
public abstract class DummyCacheWithLocks<K,V> {

    private record ValueWithLock<V> (V value, ReentrantLock lock) {}
    private record Pair<K, V>(K first, V second) {}

    private final Map<K, ValueWithLock<V>> cache;

    protected DummyCacheWithLocks(Map<K, V> cache) {
        // here we use not safe thread implementation
        // in case of concurrent hash map it is possible to use a built-in sync
        // here I want to demonstrate
        this.cache = cache.entrySet()
                .stream()
                .map(e -> new Pair<>(e.getKey(),
                        new ValueWithLock<>(e.getValue(), new ReentrantLock())))
                .collect(Collectors.toMap(p-> p.first, p -> p.second));
    }

    protected V withLock(K key, Function<V, V> operation) {
        var valueAndLock = cache.get(key);
        var lock = valueAndLock.lock;
        try {
            if (lock.tryLock()) {
                V transformed = operation.apply(valueAndLock.value);
                Objects.requireNonNull(transformed);
                cache.put(key, new ValueWithLock<>(transformed, lock));
                return transformed;
            } else {
                throw new LockAcquired("For key " + key + " lock is acquired" );
            }
        }
        finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        return cache.get(key).value;
    }

    private static class LockAcquired extends RuntimeException {

        public LockAcquired(String message) {
            super(message);
        }
    }
}
