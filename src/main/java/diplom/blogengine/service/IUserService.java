package diplom.blogengine.service;

import diplom.blogengine.api.request.UserNewPasswordRequest;
import diplom.blogengine.api.request.UserProfileDataRequest;
import diplom.blogengine.api.request.UserRegisterDataRequest;
import diplom.blogengine.api.request.UserResetPasswordRequest;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.security.UserDetailsExt;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

public interface IUserService {
    ResultResponse registerUser(UserRegisterDataRequest userRegisterDataRequest, Locale locale);

    ResultResponse saveProfile(UserProfileDataRequest userProfileDataRequest, UserDetailsExt authenticatedUser, Locale locale);

    ResultResponse resetPassword(UserResetPasswordRequest resetPasswordRequest, Locale locale);

    ResultResponse saveNewPassword(UserNewPasswordRequest passwordRequest, Locale localest);
}
