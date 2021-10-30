package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
public class PostCommentDataRequest {
    @JsonProperty("parent_id")
    @PositiveOrZero(message = "{commentParentId.positiveOrZero}")
    private long parentId;

    @JsonProperty("post_id")
    @NotNull(message = "{commentPostId.notnull}")
    @Positive(message = "{commentPostId.positive}")
    private long postId;

    @NotNull(message = "{commentText.notnull}")
    private String text;
}
