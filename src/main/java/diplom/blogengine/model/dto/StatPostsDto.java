package diplom.blogengine.model.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class StatPostsDto {
    private final long postsCount;
    private final long viewsCount;
    private final LocalDateTime firstPublication;

    public StatPostsDto(Long postsCount, Long viewsCount, LocalDateTime firstPublication) {
        this.postsCount = postsCount != null ? postsCount : 0L;
        this.viewsCount = viewsCount != null ? viewsCount : 0L;
        this.firstPublication = firstPublication;
    }
}
