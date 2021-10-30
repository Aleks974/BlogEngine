package diplom.blogengine.api.response.mapper;

import diplom.blogengine.api.response.AuthResponse;
import diplom.blogengine.api.response.CaptchaResponse;
import diplom.blogengine.api.response.LogoutResponse;
import diplom.blogengine.api.response.UserInfoAuthResponse;
import diplom.blogengine.model.User;
import org.springframework.stereotype.Component;

@Component
public class AuthResponsesMapper {
    private static final AuthResponse failAuthResponse = new AuthResponse(false);

    public CaptchaResponse captchaResponse(String secret, String captcha) {
        return new CaptchaResponse(secret, captcha);
    }

    public AuthResponse failAuthResponse() {
        return failAuthResponse;
    }

    public AuthResponse authResponse(User user, long moderationPostCount, boolean canEditSettings) {
        UserInfoAuthResponse userInfoResponse = UserInfoAuthResponse.builder()
                                                .id(user.getId())
                                                .name(user.getName())
                                                .photo(user.getPhoto())
                                                .email(user.getEmail())
                                                .moderation(user.isModerator())
                                                .moderationCount(moderationPostCount)
                                                .settings(canEditSettings)
                                                .build();
        return new AuthResponse(true, userInfoResponse);
    }

    public LogoutResponse logoutResponse() {
        return new LogoutResponse(true);
    }
}
