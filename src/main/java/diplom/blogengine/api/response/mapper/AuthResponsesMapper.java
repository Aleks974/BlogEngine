package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.*;
import diplom.blogengine.security.UserDetailsExt;
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

    public AuthResponse failAuthResponse() {
        return new AuthResponse(false);
    }

    public AuthResponse authResponse(UserDetailsExt userDetails, long moderationPostCount, boolean canEditSettings) {
        UserInfoAuthResponse userInfoResponse = UserInfoAuthResponse.builder()
                                                .id(userDetails.getId())
                                                .name(userDetails.getRealName())
                                                .photo(userDetails.getPhoto())
                                                .email(userDetails.getUsername())
                                                .moderation(userDetails.isModerator())
                                                .moderationCount(moderationPostCount)
                                                .settings(canEditSettings)
                                                .build();
        return new AuthResponse(true, userInfoResponse);
    }

    public LogoutResponse logoutResponse() {
        return new LogoutResponse(true);
    }
}
