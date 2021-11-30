package diplom.blogengine.repository;

import diplom.blogengine.model.Tag;
import diplom.blogengine.model.dto.TagCountDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class CachedTagRepository {
    private final TagRepository tagRepository;
    private final ConcurrentMap<String, List<TagCountDto>> cacheStoreTags = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Tag> cacheStoreTag = new ConcurrentHashMap<>();
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    private final Object lock3 = new Object();

    public CachedTagRepository(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public void clearAllCache() {
        cacheStoreTags.clear();
        cacheStoreTag.clear();
    }


    public List<TagCountDto> findAllTagsCounts() {
        final String key = "all";
        return cacheStoreTags.computeIfAbsent(key, k -> {
            List<TagCountDto> tags = null;
            synchronized (lock1) {
                tags = tagRepository.findAllTagsCounts();
            }
            return tags;
        });
    }

    public List<TagCountDto> findTagsCountsByQuery(String query) {
        final String key = "query_" + query;
        return cacheStoreTags.computeIfAbsent(key, k -> {
            List<TagCountDto> tags = null;
            synchronized (lock2) {
                tags = tagRepository.findTagsCountsByQuery(query);
            }
            return tags;
        });
    }

    public Tag findByName(String name) {
        return cacheStoreTag.computeIfAbsent(name, k -> {
            Tag tag = null;
            synchronized (lock3) {
                tag = tagRepository.findByName(name);
            }
            return  tag;
        });
    }

}
