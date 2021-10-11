package diplom.blogengine.service.util.cache;

import diplom.blogengine.model.dto.TagCountDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TagsCacheHandler {
    private final String queryKeyPrefix = "query_";
    private final String queryAllKeyPrefix = "all";
    private final ConcurrentMap<String, List<TagCountDto>> tagsQueryCache = new ConcurrentHashMap<>();

    public Optional<List<TagCountDto>> getCachedAllQuery() {
        return getCached(queryAllKeyPrefix);
    }

    public Optional<List<TagCountDto>> getCachedQuery(String query) {
        return getCached(queryKeyPrefix.concat(query));
    }

    public void cacheAllQuery(List<TagCountDto> tags) {
        tagsQueryCache.put(queryAllKeyPrefix, tags);
    }

    public void cacheQuery(String query, List<TagCountDto> tags) {
        addToCache(queryKeyPrefix.concat(query), tags);
    }

    public void clearCache() {
        tagsQueryCache.clear();
    }


    private Optional<List<TagCountDto>> getCached(String key) {
        return Optional.ofNullable(tagsQueryCache.get(key));
    }

    private void addToCache(String key, List<TagCountDto> tags) {
        if (tags != null) {
            tagsQueryCache.put(key, tags);
        }
    }
}
