package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class AuthCheckResponse {
    private final boolean result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final UserAuthInfoResponse userAuthInfoResponse;

    public AuthCheckResponse(Boolean result, UserAuthInfoResponse userAuthInfoResponse) {
        this.result = result;
        this.userAuthInfoResponse = userAuthInfoResponse;
    }
}
