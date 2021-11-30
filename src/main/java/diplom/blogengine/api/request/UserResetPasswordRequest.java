package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import diplom.blogengine.validation.EmailConstraint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.PropertySource;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
public class UserResetPasswordRequest {
    private static final int MAX_STRING_FIELD_LENGTH = 255;

    @NotBlank(message = "{email.notblank}")
    @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
    private String email;

    @JsonCreator
    public UserResetPasswordRequest(@JsonProperty("email") String email) {
        this.email = email;
    }
}
