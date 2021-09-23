package diplom.blogengine.service;

import diplom.blogengine.api.request.UserDataRequest;
import diplom.blogengine.api.response.RegisterUserResponse;

public interface IUserService {
    RegisterUserResponse registerUser(UserDataRequest newUserData);
}
