package diplom.blogengine.repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

public class PostsCounterStorage {
    private static Object lock = new Object();
    private final ConcurrentHashMap<Long, Integer> counters = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Long> updatedPostIds = new ConcurrentLinkedQueue<>();
    private final int ONE = 1;

    public int getOrSet(Long postId, int value) {
        Objects.requireNonNull(value, "getOrSet(): value is null for postId: " + postId);
        return counters.computeIfAbsent(postId, k -> value);
    }

    public int incrementAndGet(Long postId, int initialValue) {
        Integer value;
        if ((value = counters.get(postId)) == null) {
            value = initialValue + 1;
            counters.put(postId, value);
        } else {
            value = counters.merge(postId, ONE, Integer::sum);
        }

        updatedPostIds.add(postId);
        return value;
    }

    public Set<Long> getAndClearUpdatedIds() {
        Set<Long> updated = new HashSet<>(updatedPostIds);
        updatedPostIds.clear();
        return updated;
    }

    public Integer get(Long postId) {
        return counters.get(postId);
    }

    public Integer set(Long postId, Integer value) {
        counters.put(postId, value);
        updatedPostIds.add(postId);
        return value;
    }

    public void remove(Long postId) {
        counters.remove(postId);
        updatedPostIds.remove(postId);
    }
}

