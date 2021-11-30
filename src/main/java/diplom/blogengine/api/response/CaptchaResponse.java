package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class CaptchaResponse {
    @NonNull
    private final String secret;
    @NonNull
    private final String image;

    @JsonCreator
    public CaptchaResponse(@JsonProperty("secret") String secret, @JsonProperty("image") String image) {
        this.secret = secret;
        this.image = image;
    }
}
