package diplom.blogengine.api.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = UserInfoAuthResponse.UserInfoAuthResponseBuilder.class)
public class UserInfoAuthResponse {
    private long id;
    private String name;
    private String photo;
    private String email;
    private boolean moderation;
    private long moderationCount;
    private boolean settings;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserInfoAuthResponseBuilder {

    }

}
