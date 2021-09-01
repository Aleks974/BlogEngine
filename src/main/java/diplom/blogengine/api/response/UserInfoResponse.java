package diplom.blogengine.api.response;

import lombok.Getter;

@Getter
public class UserInfoResponse {
    private final long id;
    private final String name;

    public UserInfoResponse(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
