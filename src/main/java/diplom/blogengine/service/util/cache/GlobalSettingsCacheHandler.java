package diplom.blogengine.service.util.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalSettingsCacheHandler {
    private final AtomicReference<Map<String, Boolean>> handler = new AtomicReference<>();

    public Map<String, Boolean> cache(Map<String, Boolean> settings) {
        handler.set(settings);
        return settings;
    }

    public Optional<Map<String, Boolean>> getCached() {
        return Optional.ofNullable(handler.get());
    }
}
