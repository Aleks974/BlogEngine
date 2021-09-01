package diplom.blogengine.model.dto;

import lombok.Getter;

@Getter
public class TagCountDto {
    private final String name;
    private final long count;

    public TagCountDto(String name, long count) {
        this.name = name;
        this.count = count;
    }
}
