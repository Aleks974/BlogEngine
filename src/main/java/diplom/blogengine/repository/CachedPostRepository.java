package diplom.blogengine.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class CachedPostRepository {
    private final PostRepository postRepository;
    private final ConcurrentMap<StoreKey, List<Object[]>> cacheStorePostsData = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> cacheStorePostsCount = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, List<Object[]>> cacheStoreSinglePostData = new ConcurrentHashMap<>();

    public CachedPostRepository(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void clearAllCache() {
        cacheStorePostsData.clear();
        cacheStorePostsCount.clear();
        cacheStoreSinglePostData.clear();
    }

    public void clearMultiplePostsAndCountsCache() {
        cacheStorePostsData.clear();
        cacheStorePostsCount.clear();
    }

    public void clearSinglePostCache(Long id) {
        cacheStoreSinglePostData.remove(id);
    }

    public List<Object[]> findPostsData(Pageable pageRequest) {
        final StoreKey key = createKey("postsData", pageRequest);
        //log.debug(key.toString());
        return cacheStorePostsData.computeIfAbsent(key, k -> postRepository.findPostsData(pageRequest));
    }

    public long getTotalPostsCount() {
        final String key = "totalPostsCount";
        return cacheStorePostsCount.computeIfAbsent(key, k -> postRepository.getTotalPostsCount());
    }


    public List<Object[]> findPostsDataOrderByLikesCount(Pageable pageRequest) {
        final StoreKey key = createKey("postsDataOrderByLikesCount", pageRequest);
        //log.debug(key.toString());
        return cacheStorePostsData.computeIfAbsent(key, k -> postRepository.findPostsDataOrderByLikesCount(pageRequest));
    }

    public long getTotalPostsCountExcludeDislikes() {
        final String key = "totalPostsCountExcludeDislikes";
        return cacheStorePostsCount.computeIfAbsent(key, k -> postRepository.getTotalPostsCountExcludeDislikes());
    }


    public List<Object[]> findPostsDataOrderByCommentsCount(Pageable pageRequest) {
        final StoreKey key = createKey("postsDataOrderByCommentsCount", pageRequest);
        //log.debug(key.toString());
        return cacheStorePostsData.computeIfAbsent(key, k -> postRepository.findPostsDataOrderByCommentsCount(pageRequest));
    }

    public List<Object[]> findPostsByQuery(String query, Pageable pageRequest) {
        final StoreKey key = createKey("postsByQuery_" + query, pageRequest);
        //log.debug(key.toString());
        return cacheStorePostsData.computeIfAbsent(key, k -> postRepository.findPostsByQuery(query, pageRequest));
    }

    public long getTotalPostsCountByQuery(String query) {
        final String key = "totalPostsCountByQuery_" + query;
        return cacheStorePostsCount.computeIfAbsent(key, k -> postRepository.getTotalPostsCountByQuery(query));
    }

    public List<Object[]> findPostsByDate(Date date, Pageable pageRequest) {
        final StoreKey key = createKey("postsByDate_" + date, pageRequest);
        //log.debug(key.toString());
        return cacheStorePostsData.computeIfAbsent(key, k -> postRepository.findPostsByDate(date, pageRequest));
    }

    public long getTotalPostsCountByDate(Date date) {
        final String key = "totalPostsCountByDate_" + date;
        return cacheStorePostsCount.computeIfAbsent(key, k -> postRepository.getTotalPostsCountByDate(date));
    }

    public List<Object[]> findPostsByTag(String tag, Pageable pageRequest) {
        final StoreKey key = createKey("postsByTag_" + tag, pageRequest);
        //log.debug(key.toString());
        return cacheStorePostsData.computeIfAbsent(key, k -> postRepository.findPostsByTag(tag, pageRequest));
    }

    public long getTotalPostsCountByTag(String tag) {
        final String key = "totalPostsCountByTag_" + tag;
        return cacheStorePostsCount.computeIfAbsent(key, k -> postRepository.getTotalPostsCountByTag(tag));
    }

    public List<Object[]> findPostById(long postId, long authUserId, boolean authUserIsModerator) {
        return cacheStoreSinglePostData.computeIfAbsent(postId, k -> postRepository.findPostById(postId, authUserId, authUserIsModerator));
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    private static class StoreKey {
        private final long offset;
        private final int limit;
        private final Sort sort;
        private final String additionalKey;

        public StoreKey(String additionalKey, long offset, int limit, Sort sort) {
            this.offset = offset;
            this.limit = limit;
            this.sort = sort;
            this.additionalKey = additionalKey;
        }
    }

    private StoreKey createKey(String addKey, Pageable pageRequest) {
        return new StoreKey(addKey, pageRequest.getPageNumber(), pageRequest.getPageSize(), pageRequest.getSort());
    }


}
