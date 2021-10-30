package diplom.blogengine.security;

import diplom.blogengine.api.request.UserLoginRequest;
import diplom.blogengine.api.response.AuthResponse;
import diplom.blogengine.api.response.LogoutResponse;
import diplom.blogengine.api.response.mapper.AuthResponsesMapper;
import diplom.blogengine.exception.UserNotFoundException;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.service.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Service
public class AuthenticationService implements IAuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final AuthResponsesMapper responsesMapper;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 AuthResponsesMapper responsesMapper,
                                 PostRepository postRepository,
                                 UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.responsesMapper = responsesMapper;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void loginUser(HttpServletRequest httpRequest, UserLoginRequest userLoginRequest) {
        String username = userLoginRequest.getEmail();
        String password = userLoginRequest.getPassword();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authenticationManager.authenticate(authToken);
        if (auth == null) {
            throw new BadCredentialsException("bad credentials");
        }
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
    }

    @Override
    public AuthResponse getAuthData() {
        log.debug("enter getAuthData()");

        UserDetailsExt authUser = getAuthenticated();
        if (authUser == null) {
            return responsesMapper.failAuthResponse();
        }

        long authId = authUser.getId();
        User user = userRepository.findById(authId).orElseThrow(() -> new UserNotFoundException(authId));
        long moderationPostCount;
        boolean canEditSettings;
        if (user.isModerator()) {
            moderationPostCount = postRepository.getModerationPostCount();
            canEditSettings = true;
        } else {
            moderationPostCount = 0;
            canEditSettings = false;
        }
        return responsesMapper.authResponse(user, moderationPostCount, canEditSettings);

    }

    @Override
    public void logoutUser(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        log.debug("enter logoutUser()");
        // ToDo invalidate session, delete cookies JSEESIONID, rememberme
        httpRequest.logout();
    }

    @Override
    public LogoutResponse getLogoutData() {
        log.debug("enter getLogoutData()");

        return responsesMapper.logoutResponse();
    }

    @Override
    public UserDetailsExt getAuthenticatedUser() {
        return getAuthenticated();
    }

    @Override
    public boolean isAuthenticated() {
        return getAuthenticated() != null;
    }

    @Override
    public long getAuthenticatedUserId() {
        return getAuthenticatedUid(getAuthenticated());
    }

    @Override
    public long getAuthenticatedUserId(UserDetailsExt authUser) {
        return getAuthenticatedUid(authUser);
    }

    private long getAuthenticatedUid(UserDetailsExt authUser) {
        final long NOT_AUTH_USER_ID = 0;
        return authUser != null ? authUser.getId() : NOT_AUTH_USER_ID;
    }

    private UserDetailsExt getAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken)  {
            return (UserDetailsExt) auth.getPrincipal();
        } else {
            return null;
        }
    }
}
