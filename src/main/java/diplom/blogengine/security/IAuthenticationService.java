package diplom.blogengine.security;

import diplom.blogengine.api.request.UserLoginRequest;
import diplom.blogengine.api.response.AuthResponse;
import diplom.blogengine.api.response.LogoutResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IAuthenticationService {
    void loginUser(HttpServletRequest httpRequest, UserLoginRequest userLoginRequest);

    AuthResponse getAuthData();

    void logoutUser(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception;

    LogoutResponse getLogoutData();

    boolean isAuthenticated();

    UserDetailsExt getAuthenticatedUser();

    long getAuthenticatedUserId();

    long getAuthenticatedUserId(UserDetailsExt authUser);
}
