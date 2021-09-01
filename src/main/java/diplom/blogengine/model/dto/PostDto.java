package diplom.blogengine.model.dto;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

public interface PostDto {
    long getId();
    LocalDateTime getTime();
    UserDto getUser();
    String getTitle();
    String getText();
    long getViewCount();

    @Value("#{target.getTimestamp(@blogSettings.getServerTimeZone())}")
    long getTimestamp();

    @Value("#{target.getAnnounce(@contentProcessor)}")
    String getAnnounce();

}
