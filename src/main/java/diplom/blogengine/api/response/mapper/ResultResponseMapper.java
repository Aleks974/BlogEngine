package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.*;
import diplom.blogengine.security.UserDetailsExt;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ResultResponseMapper {
    private static final ResultResponse successResponse = ResultResponse.builder().result(true).build();
    private static final ResultResponse failureResponse = ResultResponse.builder().result(false).build();

    public ResultResponse success() {
        return successResponse;
    }

    public ResultResponse success(long id) {
        return ResultResponse.builder()
                .id(id)
                .build();
    }

    public ResultResponse failure() {
        return failureResponse;
    }

    public ResultResponse failure(Map<String, String> errors) {
        return ResultResponse.builder()
                .result(false)
                .errors(errors)
                .build();
    }


}
