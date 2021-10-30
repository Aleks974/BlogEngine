package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse {
    private final Long id;
    private final Boolean result;
    private final Map<String, String> errors;
}
