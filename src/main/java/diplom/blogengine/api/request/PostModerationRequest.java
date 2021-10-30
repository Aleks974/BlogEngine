package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import diplom.blogengine.service.ModerationDecision;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class PostModerationRequest {
    @JsonProperty("post_id")
    @NotNull(message = "{moderation.postId.notnull}")
    @Positive(message = "{moderation.postId.positive}")
    private long postId;

    @NotNull(message = "{moderation.decision.notnull}")
    private ModerationDecision decision;
}
