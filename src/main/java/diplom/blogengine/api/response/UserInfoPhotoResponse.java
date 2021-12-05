package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoPhotoResponse {
    private final long id;
    private final String name;
    private final String photo;

    @JsonCreator
    public UserInfoPhotoResponse(@JsonProperty("id") long id,
                                 @JsonProperty("name") String name,
                                 @JsonProperty("photo") String photo) {
        this.id = id;
        this.name = name;
        this.photo = photo;
    }
}
