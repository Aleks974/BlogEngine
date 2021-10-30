package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import diplom.blogengine.validation.EmailConstraint;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserLoginRequest {
     private static final int MAX_STRING_FIELD_LENGTH = 255;

     @JsonProperty("e_mail")
     @NotBlank(message = "{email.notblank}")
     @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
     @EmailConstraint(message = "{email.incorrect}")
     private String email;

     @NotBlank(message = "{password.notblank}")
     @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
     private String password;
}
