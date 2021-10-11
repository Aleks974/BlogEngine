package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class AuthResponse {
    private final boolean result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final UserInfoAuthResponse user;

    public AuthResponse(boolean result) {
        this.result = result;
        this.user = null;
    }

    public AuthResponse(boolean result, UserInfoAuthResponse userAuthInfoResponse) {
        this.result = result;
        this.user = userAuthInfoResponse;
    }
}
