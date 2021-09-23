package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class AuthCheckResponse {
    private final boolean result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final UserInfoAuthResponse userAuthInfoResponse;

    public AuthCheckResponse(Boolean result, UserInfoAuthResponse userAuthInfoResponse) {
        this.result = result;
        this.userAuthInfoResponse = userAuthInfoResponse;
    }
}
