package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import diplom.blogengine.validation.EmailConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.ManyToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UserProfileDataRequest {
    private static final int MAX_STRING_FIELD_LENGTH = 255;

    @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
    private String name;

    @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
    @EmailConstraint(message = "{email.incorrect}")
    private String email;

    @Size(max = MAX_STRING_FIELD_LENGTH, message = "{stringfield.exceedlength}")
    private String password;

    @Min(value = 0, message = "{userProfile.removePhoto.minMax}")
    @Max(value = 1, message = "{userProfile.removePhoto.minMax}")
    private int removePhoto;

    @JsonIgnore
    private MultipartFile photoFile;
}
