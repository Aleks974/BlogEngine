package diplom.blogengine.api.response;

import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
public class CalendarPostsResponse {
    private final List<Integer> years;
    private final Map<String, Long> posts;

    public CalendarPostsResponse(List<Integer> years, Map<String, Long> posts) {
        this.years = years;
        this.posts = posts;
    }
}
