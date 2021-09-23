package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserDataRequest {
     private final static int MAX_STRING_FIELD_LENGTH = 255;
     
     @JsonProperty("e_mail")
     @NotBlank
     @Size(max = MAX_STRING_FIELD_LENGTH)
     @Email
     private String email;

     @NotBlank
     @Size(max = MAX_STRING_FIELD_LENGTH)
     private String password;

     @NotBlank
     @Size(max = MAX_STRING_FIELD_LENGTH)
     private String name;

     @NotBlank
     @Size(max = MAX_STRING_FIELD_LENGTH)
     private String captcha;

     @JsonProperty("captcha_secret")
     @NotBlank
     @Size(max = MAX_STRING_FIELD_LENGTH)
     private String captchaSecret;
}
