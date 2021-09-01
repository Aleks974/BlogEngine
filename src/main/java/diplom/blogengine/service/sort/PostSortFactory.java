package diplom.blogengine.service.sort;

import diplom.blogengine.config.BlogSettings;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PostSortFactory {
    private final Map<PostSortMode, Sort> sorts;

    PostSortFactory(BlogSettings blogSettings) {
        sorts = Collections.unmodifiableMap(generateSorts(blogSettings));
    }

    private Map<PostSortMode, Sort> generateSorts(BlogSettings blogSettings) {
        Map<PostSortMode, Sort> sortsPrepared = new HashMap<>();
        for (PostSortMode sortMode : PostSortMode.values()) {
            Map<String, SortField> sortModesMap = Objects.requireNonNull(blogSettings.getPostSortModes(), "Configuration does not contain post sorting mappings");
            SortField sortField = Objects.requireNonNull(sortModesMap.get(sortMode.toString()), "Configuration does not contain mapping for post sort mode: " + sortMode);

            String fieldName = Objects.requireNonNull(sortField.getFieldName(), "Post sort mode " + sortMode + " has got null field name in the Configuration");
            Sort.Direction direction = Objects.requireNonNull(sortField.getDirection(), "Post sort mode " + sortMode + " has got null direction in the Configuration");
            Sort sort;
            if (sortField.isSortUnsafe()) {
                sort = JpaSort.unsafe(direction, fieldName);
            } else {
                sort = Sort.by(direction, fieldName);
            }
            sortsPrepared.put(sortMode, sort);
        }
        return sortsPrepared;
    }

    public Sort getSort(PostSortMode mode) {
        return Objects.requireNonNull(sorts.get(mode), "Sort null for post sort mode:" + mode);
    }
}
