package diplom.blogengine.api.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Value
@Builder
public class UserInfoAuthResponse {
    private long id;
    private String name;
    private String photo;
    private String email;
    private boolean moderation;
    private long moderationCount;
    private boolean settings;
}
