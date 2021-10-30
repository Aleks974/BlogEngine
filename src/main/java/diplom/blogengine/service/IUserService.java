package diplom.blogengine.service;

import diplom.blogengine.api.request.UserProfileDataRequest;
import diplom.blogengine.api.request.UserRegisterDataRequest;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.security.UserDetailsExt;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

public interface IUserService {
    ResultResponse registerUser(UserRegisterDataRequest userRegisterDataRequest, Locale locale);

    ResultResponse saveProfile(UserProfileDataRequest userProfileDataRequest, UserDetailsExt authenticatedUser, Locale locale);
}
