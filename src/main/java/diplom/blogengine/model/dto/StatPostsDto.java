package diplom.blogengine.model.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StatPostsDto {
    private final long postsCount;
    private final long viewsCount;
    private final LocalDateTime firstPublication;

    public StatPostsDto(long postsCount, long viewsCount, LocalDateTime firstPublication) {
        this.postsCount = postsCount;
        this.viewsCount = viewsCount;
        this.firstPublication = firstPublication;
    }
}
