package diplom.blogengine.api.response;

import lombok.Getter;

@Getter
public class LogoutResponse {
    private final boolean result;

    public LogoutResponse(boolean result) {
        this.result = result;
    }

}
