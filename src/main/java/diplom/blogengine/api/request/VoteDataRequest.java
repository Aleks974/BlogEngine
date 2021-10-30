package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class VoteDataRequest {
    @JsonProperty("post_id")
    @NotNull(message = "{votePostId.notnull}")
    @Positive(message = "{votePostId.positive}")
    private long postId;
}
