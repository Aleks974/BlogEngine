package diplom.blogengine.service.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalSettingsCacheHandler {
    private final ConcurrentMap<String, Boolean> cacheHandler = new ConcurrentHashMap<>();

    public Map<String, Boolean> cache(Map<String, Boolean> settings) {
        cacheHandler.putAll(settings);
        return settings;
    }

    public Optional<Map<String, Boolean>> getCached() {
        Map<String, Boolean> cached = null;
        if (!cacheHandler.isEmpty()) {
            cached = new HashMap<>(cacheHandler);
        }
        return Optional.ofNullable(cached);
    }
}
