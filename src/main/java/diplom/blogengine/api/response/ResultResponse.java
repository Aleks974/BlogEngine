package diplom.blogengine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = ResultResponse.ResultResponseBuilder.class)
public class ResultResponse {
    private final Long id;
    private final Boolean result;
    private final Map<String, String> errors;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResultResponseBuilder {

    }
}
