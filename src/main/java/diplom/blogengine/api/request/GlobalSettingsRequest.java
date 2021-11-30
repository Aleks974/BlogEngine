package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@Builder
@JsonDeserialize(builder = GlobalSettingsRequest.GlobalSettingsRequestBuilder.class)
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

    @JsonPOJOBuilder(withPrefix = "")
    public static class GlobalSettingsRequestBuilder {

    }

}
