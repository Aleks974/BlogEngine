package diplom.blogengine.service;

import diplom.blogengine.api.request.UserProfileDataRequest;
import diplom.blogengine.api.request.UserRegisterDataRequest;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.mapper.ResultResponseMapper;
import diplom.blogengine.exception.UserNotFoundException;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.security.UserDetailsExt;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService implements IUserService {
    private final static int MIN_PASSWORD_LENGTH = 6;
    private final static Pattern NAME_PATTERN = Pattern.compile("(?ui)^[\\w]+[\\w -.]+[\\w.]+$");


    private final UserRepository userRepository;
    private final CaptchaCodeRepository captchaCodeRepository;
    private final IFileStorageService fileStorageService;
    private final ResultResponseMapper responsesMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;


    public UserService(UserRepository userRepository,
                       CaptchaCodeRepository captchaCodeRepository,
                       IFileStorageService fileStorageService,
                       ResultResponseMapper responsesMapper,
                       PasswordEncoder passwordEncoder,
                       MessageSource messageSource) {
        this.userRepository = userRepository;
        this.captchaCodeRepository = captchaCodeRepository;
        this.fileStorageService = fileStorageService;
        this.responsesMapper = responsesMapper;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    @Override
    public ResultResponse registerUser(UserRegisterDataRequest userData, Locale locale) {
        log.debug("enter registerUser()");

        Objects.requireNonNull(userData, "userData is null");
        Objects.requireNonNull(locale, "locale is null");

        Map<String, String> errors = validateUserRegisterData(userData, locale);
        if (!errors.isEmpty()) {
            return responsesMapper.failure(errors);
        }
        saveUser(convertDtoToUser(userData));
        return responsesMapper.success();
    }

    @Override
    public ResultResponse saveProfile(UserProfileDataRequest userData,
                                      UserDetailsExt authUser,
                                      Locale locale) {
        log.debug("enter saveProfile()");

        Objects.requireNonNull(userData, "userData is null");
        Objects.requireNonNull(authUser, "authUser is null");
        Objects.requireNonNull(locale, "locale is null");

        long authUserId = authUser.getId();
        User updateUser = userRepository.findById(authUserId).orElseThrow(() -> new UserNotFoundException(authUserId));

        Map<String, String> errors = new HashMap<>();
        boolean doUpdateName = false;
        boolean doUpdateEmail = false;
        boolean doUpdatePassword = false;

        String newName = userData.getName();
        String newEmail = userData.getEmail();
        String newPassword = userData.getPassword();

        if (newName != null && !newName.equals(updateUser.getName())) {
            if (validateName(newName, errors, locale)) {
                doUpdateName = true;
            }
        }
        if (newEmail != null && !newEmail.equalsIgnoreCase(updateUser.getEmail())) {
            if (validateEmail(newEmail, errors, locale)) {
                doUpdateEmail = true;
            }
        }
        if (newPassword != null) {
            if (validatePassword(newPassword, errors, locale)) {
                doUpdatePassword = true;
            }
        }

        if (!errors.isEmpty()) {
            return responsesMapper.failure(errors);
        }

        final int REMOVE_PHOTO_FLAG = 1;
        if (userData.getRemovePhoto() == REMOVE_PHOTO_FLAG) {
            fileStorageService.deleteFile(updateUser.getPhoto());
            updateUser.setPhoto(null);
        } else if (userData.getPhotoFile() != null) {
            String newPhoto = fileStorageService.storePhoto(userData.getPhotoFile(), authUserId);
            updateUser.setPhoto(newPhoto);
        }

        if (doUpdateName) {
            updateUser.setName(newName);
        }
        if (doUpdateEmail) {
            updateUser.setEmail(newEmail);
        }
        if (doUpdatePassword) {
            updateUser.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(updateUser);

        return responsesMapper.success();
    }

    private User convertDtoToUser(UserRegisterDataRequest userRegisterDataRequest) {
        log.debug("enter convertDtoToUser()");

        User user = new User();
        user.setName(userRegisterDataRequest.getName());
        user.setEmail(userRegisterDataRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRegisterDataRequest.getPassword()));
        user.setRegTime(LocalDateTime.now());
        user.setModerator(false);
        return user;
    }

    private User saveUser(User user) {
        log.debug("enter saveUser()");

        return userRepository.save(user);
    }

    private Map<String, String> validateUserRegisterData(UserRegisterDataRequest userData, Locale locale) {
        log.debug("enter validateUserRegisterData()");

        Map<String, String> errors = new HashMap<>();

        validateEmail(userData.getEmail(), errors, locale);
        validateName(userData.getName(), errors, locale);
        validatePassword(userData.getPassword(), errors, locale);
        validateCaptcha(userData.getCaptcha(), userData.getCaptchaSecret(), errors, locale);

        return errors;
    }

    private boolean validateEmail(String email, Map<String, String> errors, Locale locale) {
        boolean valid = true;
        if (email != null && existEmail(email)) {
            errors.put("email", messageSource.getMessage("email.alreadyexists", null, locale));
            valid = false;
        }
        return valid;
    }

    private boolean validateName(String name, Map<String, String> errors, Locale locale) {
        boolean valid = true;
        if (name == null) {
            return true;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            errors.put("name", messageSource.getMessage("name.incorrect", null, locale));
            valid = false;
        } else if (existName(name)) {
            errors.put("name", messageSource.getMessage("name.alreadyexists", null, locale));
            valid = false;
        }

        if (existName(name)) {
            errors.put("name", messageSource.getMessage("name.alreadyexists", null, locale));
            valid = false;
        }
        return valid;
    }

    private boolean validatePassword(String pass, Map<String, String> errors, Locale locale) {
        boolean valid = true;
        if (pass != null && isPasswordShort(pass)) {
            errors.put("password", messageSource.getMessage("password.isshort", null, locale));
            valid = false;
        }
        return valid;
    }

    private void validateCaptcha(String inputCode, String secretCode, Map<String, String> errors, Locale locale) {
        if (inputCode != null && secretCode != null && !captchaIsValid(inputCode, secretCode)) {
            errors.put("captcha", messageSource.getMessage("captcha.isincorrect", null, locale));
        }
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

        return password.length() < MIN_PASSWORD_LENGTH;
    }

    private boolean captchaIsValid(String inputCode, String secretCode) {
        log.debug("enter checkCaptcha()");

        String code = captchaCodeRepository.findCodeBySecret(secretCode);
        return code != null && code.equals(inputCode);
    }
}
