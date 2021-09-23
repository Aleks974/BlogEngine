package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoPhotoResponse {
    private final long id;
    private final String name;
    private final String photo;

    public UserInfoPhotoResponse(long id, String name, String photo) {
        this.id = id;
        this.name = name;
        this.photo = photo;
    }
}
