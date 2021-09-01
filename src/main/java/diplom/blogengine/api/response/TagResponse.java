package diplom.blogengine.api.response;

import lombok.Getter;

@Getter
public class TagResponse {
    private final String name;
    private final double weight;

    public TagResponse(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }
}
