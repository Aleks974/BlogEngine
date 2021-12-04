package diplom.blogengine.model.dto;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class TagDto {
    private final String name;

    public TagDto(String name) {
        this.name = name;
    }
}
