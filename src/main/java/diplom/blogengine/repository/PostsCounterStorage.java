package diplom.blogengine.repository;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

public class PostsCounterStorage {
    private final ConcurrentHashMap<Long, Integer> counters = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Long> updatedPostIds = new ConcurrentLinkedQueue<>();
    private final int START_VALUE = 1;

    public int getOrUpdate(Long postId, int value) {
        Objects.requireNonNull(value, "getOrUpdate(): vslue is null for postId: " + postId);
        return counters.computeIfAbsent(postId, k -> value);
    }

    public int incrementAndGet(Long postId) {
        int value;
        //synchronized (postId) {
            value = counters.merge(postId, START_VALUE, Integer::sum);
       // }
        updatedPostIds.add(postId);
        return value;
    }

    public Set<Long> getAndClearUpdatedIds() {
        Set<Long> updated = new HashSet<>(updatedPostIds);
        updatedPostIds.clear();
        return updated;
    }

    public int get(Long postId) {
        return counters.get(postId);
    }
}

