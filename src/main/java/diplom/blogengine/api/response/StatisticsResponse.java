package diplom.blogengine.api.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = StatisticsResponse.StatisticsResponseBuilder.class)
public class StatisticsResponse {
    private final long postsCount;
    private final long likesCount;
    private final long dislikesCount;
    private final long viewsCount;
    private final long firstPublication;

    @JsonPOJOBuilder(withPrefix = "")
    public static class StatisticsResponseBuilder {

    }
}
