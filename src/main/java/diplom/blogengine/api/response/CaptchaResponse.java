package diplom.blogengine.api.response;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class CaptchaResponse {
    @NonNull
    private final String secret;
    @NonNull
    private final String image;

    public CaptchaResponse(String secret, String image) {
        this.secret = secret;
        this.image = image;
    }
}
