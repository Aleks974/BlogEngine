package diplom.blogengine.api.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StatisticsResponse {
    private final long postsCount;

    private final long likesCount;

    private final long dislikesCount;

    private final long viewsCount;

    private final long firstPublication;
}
