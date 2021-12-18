package diplom.blogengine.model.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class StatVotesDto {
    private final long likesCount;
    private final long dislikesCount;

    public StatVotesDto(Long likesCount, Long dislikesCount) {
        this.likesCount = likesCount != null ? likesCount : 0L;
        this.dislikesCount = dislikesCount != null ? dislikesCount : 0L;
    }
}
