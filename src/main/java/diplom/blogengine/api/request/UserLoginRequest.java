package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserLoginRequest {
     @JsonProperty("e_mail")
     @NotBlank
     private String email;

     @NotBlank
     private String password;
}
