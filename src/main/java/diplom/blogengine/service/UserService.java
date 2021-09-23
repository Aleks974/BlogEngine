package diplom.blogengine.service;

import diplom.blogengine.api.request.UserDataRequest;
import diplom.blogengine.api.response.RegisterUserResponse;
import diplom.blogengine.api.response.mapper.AuthResponsesMapper;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.service.util.PasswordHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService implements IUserService {
    private final static int MIN_PASSWORD_LENGTH = 6;
    private final UserRepository userRepository;
    private final CaptchaCodeRepository captchaCodeRepository;
    private final AuthResponsesMapper responsesMapper;
    private final PasswordHelper passwordHelper;

    public UserService(UserRepository userRepository,
                       CaptchaCodeRepository captchaCodeRepository,
                       AuthResponsesMapper responsesMapper,
                       PasswordHelper passwordHelper) {
        this.userRepository = userRepository;
        this.captchaCodeRepository = captchaCodeRepository;
        this.responsesMapper = responsesMapper;
        this.passwordHelper = passwordHelper;
    }


    @Override
    public RegisterUserResponse registerUser(UserDataRequest userData) {
        RegisterUserResponse resultResponse;
        Map<String, String> errors = validateUserRegisterData(userData);
        if (errors.isEmpty()) {
            userRepository.save(convertToUser(userData));
            resultResponse = responsesMapper.registerSuccess();
        } else {
            resultResponse = responsesMapper.registerFailure(errors);
        }
        return resultResponse;
    }

    private User convertToUser(UserDataRequest userRequest) {
        User newUser = new User();
        newUser.setName(userRequest.getName());
        newUser.setEmail(userRequest.getEmail());
        newUser.setPassword(passwordHelper.generateHashEncode(userRequest.getPassword()));
        newUser.setRegTime(LocalDateTime.now());
        newUser.setModerator(false);
        return newUser;
    }

    private Map<String, String> validateUserRegisterData(UserDataRequest userData) {
        Map<String, String> errors = new HashMap<>();
        if (existEmail(userData.getEmail())) {
            errors.put("email", "Этот e-mail уже зарегистрирован");
        }
        if (existName(userData.getName())) {
            errors.put("name", "Имя указано неверно");
        }
        if (isPasswordShort(userData.getPassword())) {
            errors.put("password", "Пароль короче 6-ти символов");
        }
        if (!checkCaptcha(userData.getCaptcha(), userData.getCaptchaSecret())) {
            errors.put("captcha", "Код с картинки введён неверно");
        }
        return errors;
    }

    private boolean existEmail(String email) {
        Long id = userRepository.findUserIdByEmail(email);
        return id != null && id > 0;
    }

    private boolean existName(String name) {
        Long id = userRepository.findUserIdByName(name);
        return id != null && id > 0;
    }

    private boolean isPasswordShort(String password) {
        return password.length() > MIN_PASSWORD_LENGTH;
    }

    private boolean checkCaptcha(String inputCode, String secret) {
        long id;
        try {
            id = Long.parseLong(secret);
        } catch (NumberFormatException ex) {
            return false;
        }
        String code = captchaCodeRepository.findCodeById(id);
        return code != null && code.equals(inputCode);
    }

}
