package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private boolean result;
    private UserInfoAuthResponse user;

    public AuthResponse(boolean result) {
        this.result = result;
    }

    @JsonCreator
    public AuthResponse(@JsonProperty("result") boolean result, @JsonProperty("user") UserInfoAuthResponse userInfoAuthResponse) {
        this.result = result;
        this.user = userInfoAuthResponse;
    }
}
