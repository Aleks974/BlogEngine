package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import diplom.blogengine.validation.EmailConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
@Builder
@JsonDeserialize(builder = UserNewPasswordRequest.UserNewPasswordRequestBuilder.class)
public class UserNewPasswordRequest {
    private static final int MAX_STRING_FIELD_LENGTH = 255;

    @NotBlank(message = "{code.notblank}")
    @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
    private String code;

    @NotBlank(message = "{password.notblank}")
    @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
    private String password;

    @NotBlank(message = "{captcha.notblank}")
    @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
    private String captcha;

    @JsonProperty("captcha_secret")
    @NotBlank(message = "{captchasecret.notblank}")
    private String captchaSecret;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserNewPasswordRequestBuilder {

    }


/*    @JsonCreator
    public UserNewPasswordRequest(@JsonProperty("code") String code, @JsonProperty("password") String password,
                                  @JsonProperty("captcha") String captcha, @JsonProperty("captchaSecret") String captchaSecret) {

    }*/


}
