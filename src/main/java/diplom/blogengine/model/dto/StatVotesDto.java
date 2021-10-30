package diplom.blogengine.model.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StatVotesDto {
    private final long likesCount;
    private final long dislikesCount;

    public StatVotesDto(long likesCount, long dislikesCount) {
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
    }
}
