package diplom.blogengine.api.response;

import lombok.Getter;
import lombok.Setter;

// ToDo final, builder
@Getter
@Setter
public class UserAuthInfoResponse {
    private int id;
    private String name;
    private String photo;
    private String email;
    private boolean moderation;
    private int moderationCount;
    private boolean settings;
}
