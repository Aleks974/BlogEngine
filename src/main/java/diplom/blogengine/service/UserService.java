package diplom.blogengine.service;

import diplom.blogengine.api.request.UserDataRequest;
import diplom.blogengine.api.response.RegisterUserResponse;
import diplom.blogengine.api.response.mapper.AuthResponsesMapper;
import diplom.blogengine.exception.UserNotFoundException;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.repository.PostRepository;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.security.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserService implements IUserService {
    private final static int MIN_PASSWORD_LENGTH = 6;
    private final UserRepository userRepository;
    private final CaptchaCodeRepository captchaCodeRepository;
    private final PostRepository postRepository;
    private final AuthResponsesMapper responsesMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    public UserService(UserRepository userRepository,
                       CaptchaCodeRepository captchaCodeRepository,
                       AuthenticationService authenticationService,
                       PostRepository postRepository,
                       AuthResponsesMapper responsesMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.captchaCodeRepository = captchaCodeRepository;
        this.postRepository = postRepository;
        this.responsesMapper = responsesMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;

    }

    @Override
    public RegisterUserResponse registerUser(UserDataRequest userDataRequest) {
        log.debug("enter registerUser()");

        RegisterUserResponse resultResponse;
        Map<String, String> errors = validateUserRegisterData(userDataRequest);
        if (errors.isEmpty()) {
            saveUser(convertDtoToUser(userDataRequest));
            resultResponse = responsesMapper.registerSuccess();
        } else {
            resultResponse = responsesMapper.registerFailure(errors);
        }
        return resultResponse;
    }

    private User convertDtoToUser(UserDataRequest userDataRequest) {
        log.debug("enter convertDtoToUser()");

        User user = new User();
        user.setName(userDataRequest.getName());
        user.setEmail(userDataRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userDataRequest.getPassword()));
        user.setRegTime(LocalDateTime.now());
        user.setModerator(false);
        return user;
    }

    private User saveUser(User user) {
        log.debug("enter saveUser()");

        long id = user.getId();
        if (id != 0 && !userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id = " + id);
        }
        return userRepository.save(user);
    }

    private Map<String, String> validateUserRegisterData(UserDataRequest userDataRequest) {
        log.debug("enter validateUserRegisterData()");

        Map<String, String> errors = new HashMap<>();
        if (existEmail(userDataRequest.getEmail())) {
            errors.put("email", "Этот e-mail уже зарегистрирован");
        }
        if (existName(userDataRequest.getName())) {
            errors.put("name", "Имя указано неверно");
        }
        if (isPasswordShort(userDataRequest.getPassword())) {
            errors.put("password", "Пароль короче 6-ти символов");
        }
        if (!checkCaptcha(userDataRequest.getCaptcha(), userDataRequest.getCaptchaSecret())) {
            errors.put("captcha", "Код с картинки введён неверно");
        }
        return errors;
    }

    private boolean existEmail(String email) {
        log.debug("enter existEmail()");

        return userRepository.findUserIdByEmail(email).isPresent();
    }

    private boolean existName(String name) {
        log.debug("enter existName()");

        return userRepository.findUserIdByName(name).isPresent();
    }

    private boolean isPasswordShort(String password) {
        log.debug("enter isPasswordShort()");

        return password.length() > MIN_PASSWORD_LENGTH;
    }

    private boolean checkCaptcha(String inputCode, String secretCode) {
        log.debug("enter checkCaptcha()");

        String code = captchaCodeRepository.findCodeBySecret(secretCode);
        return code != null && code.equals(inputCode);
    }
}
