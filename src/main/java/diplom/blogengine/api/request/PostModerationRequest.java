package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import diplom.blogengine.model.ModerationStatus;
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

    public PostModerationRequest() {
    }

    public PostModerationRequest(long postId, ModerationDecision decision) {
        this.postId = postId;
        this.decision = decision;
    }
}
