package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserProfileDataRequest {
    private String name;

    private String email;

    private String password;

    private int removePhoto;

    @JsonIgnore
    private MultipartFile photoFile;
}
