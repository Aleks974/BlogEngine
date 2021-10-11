package diplom.blogengine.service;

import diplom.blogengine.api.response.TagResponse;
import diplom.blogengine.api.response.MultipleTagsResponse;
import diplom.blogengine.api.response.mapper.TagsResponseMapper;
import diplom.blogengine.model.dto.TagCountDto;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.repository.TagRepository;
import diplom.blogengine.service.util.cache.TagsCacheHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagsService implements ITagsService {

    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final TagsResponseMapper tagsResponseMapper;
    private final TagsCacheHandler tagsCache;

    public TagsService(TagRepository tagRepository,
                       PostRepository postRepository,
                       TagsResponseMapper tagsResponseMapper,
                       TagsCacheHandler tagsCache) {
        this.tagRepository = tagRepository;
        this.postRepository = postRepository;
        this.tagsResponseMapper = tagsResponseMapper;
        this.tagsCache = tagsCache;
    }

    @Override
    public MultipleTagsResponse getTagsData(String query) {
        log.debug("enter getTagsData()");

        MultipleTagsResponse response;
        List<TagCountDto> tagsDto;
        long maxTagCount = 0;
        if (query == null || query.isEmpty()) {
            tagsDto = tagsCache.getCachedAllQuery().orElseGet(tagRepository::findAllTagsCounts);
            if (!tagsDto.isEmpty()) {
                maxTagCount = tagsDto.get(0).getCount();
            }
            tagsCache.cacheAllQuery(tagsDto);
        } else {
            tagsDto = tagsCache.getCachedQuery(query).orElseGet(() -> tagRepository.findTagsCountsByQuery(query));
            if (!tagsDto.isEmpty()) {
                maxTagCount = tagRepository.findMaxTagCount();
            }
            tagsCache.cacheQuery(query, tagsDto);
        }

        long postsTotalCount = postRepository.getTotalPostsCount();

        if (tagsDto == null || tagsDto.isEmpty()) {
            response = tagsResponseMapper.emptyResponse();
        } else {
            response = tagsResponseMapper.tagsResponse(getTagResponseList(tagsDto, maxTagCount, postsTotalCount));
        }
        return response;
    }

    private List<TagResponse> getTagResponseList(List<TagCountDto> tagsDto, long maxTagCount, long postsTotalCount) {
        log.debug("enter getTagResponseList()");

        double kMaxNormalized = getKMaxNormalized(maxTagCount, postsTotalCount);
        return tagsDto.stream()
                .map(t -> new TagResponse(t.getName(), getNormalizedWeight(t.getCount(), postsTotalCount, kMaxNormalized)))
                .collect(Collectors.toList());
    }

    private double getKMaxNormalized(long maxTagCount, long postsTotalCount) {
        log.trace("enter getKMaxNormalized()");

        double result;
        if (maxTagCount == 0 | postsTotalCount == 0) {
            result = 0;
        } else {
            result = 1 / ((double) maxTagCount / postsTotalCount);
        }
        return result;
    }

    private double getNormalizedWeight(double count, long postsTotalCount, double kMaxNormalized) {
        log.trace("enter getNormalizedWeight()");

        double result;
        if (postsTotalCount == 0) {
            result = 0;
        } else {
            result = (count / postsTotalCount) * kMaxNormalized;
            result = Math.round(result * 100) / 100.0;
        }
        return result;
    }

}
