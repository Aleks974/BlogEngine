package diplom.blogengine.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Locale;
import java.util.Set;

@Getter
@Setter
public class PostDataRequest {
    private static final int ACTIVE_MIN = 0;
    private static final int ACTIVE_MAX = 1;

    @NotNull(message = "{timestamp.notnull}")
    @Positive(message = "{timestamp.positive}")
    private long timestamp;

    @NotNull(message = "{active.notnull}")
    @Min(value = ACTIVE_MIN, message = "active.minmax")
    @Max(value = ACTIVE_MAX, message = "active.minmax")
    private int active;

    @NotNull(message = "{title.notnull}")
    private String title;

    @NotNull(message = "{text.notnull}")
    private String text;

    private Set<String> tags;

    @JsonIgnore
    private Locale locale;
}
