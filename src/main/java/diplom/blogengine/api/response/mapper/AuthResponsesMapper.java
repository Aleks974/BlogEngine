package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.CaptchaResponse;
import diplom.blogengine.api.response.RegisterUserResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthResponsesMapper {


    public RegisterUserResponse registerFailure(Map<String, String> errors) {
        return new RegisterUserResponse(false, errors);
    }

    public RegisterUserResponse registerSuccess() {
        return new RegisterUserResponse(true);
    }

    public CaptchaResponse captchaResponse(String secret, String captcha) {
        return new CaptchaResponse(secret, captcha);
    }
}
