package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GlobalSettingsRequest {
    @JsonProperty("MULTIUSER_MODE")
    @NotNull(message = "settings.multiUserMode.notnull")
    private Boolean multiUserMode;

    @JsonProperty("POST_PREMODERATION")
    @NotNull(message = "settings.postPreModeration.notnull")
    private Boolean postPreModeration;

    @JsonProperty("STATISTICS_IS_PUBLIC")
    @NotNull(message = "settings.statisticsIsPublic.notnull")
    private Boolean statisticsIsPublic;
}
