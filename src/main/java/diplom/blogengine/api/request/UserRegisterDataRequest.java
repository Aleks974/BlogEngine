package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import diplom.blogengine.validation.EmailConstraint;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Locale;

@Getter
@Setter
public class UserRegisterDataRequest {
     private static final int MAX_STRING_FIELD_LENGTH = 255;

     @JsonProperty("e_mail")
     @NotBlank(message = "{email.notblank}")
     @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
     @EmailConstraint(message = "{email.incorrect}")
     private String email;

     @NotBlank(message = "{password.notblank}")
     @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
     private String password;

     @NotBlank(message = "{name.notblank}")
     @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
     private String name;

     @NotBlank(message = "{captcha.notblank}")
     @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
     private String captcha;

     @JsonProperty("captcha_secret")
     @NotBlank(message = "{captchasecret.notblank}")
     @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
     private String captchaSecret;

     @JsonIgnore
     private Locale locale;
}
