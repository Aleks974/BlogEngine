package diplom.blogengine.service;

import diplom.blogengine.api.request.UserNewPasswordRequest;
import diplom.blogengine.api.request.UserProfileDataRequest;
import diplom.blogengine.api.request.UserRegisterDataRequest;
import diplom.blogengine.api.request.UserResetPasswordRequest;
import diplom.blogengine.api.response.ResultResponse;
import diplom.blogengine.api.response.mapper.ResultResponseMapper;
import diplom.blogengine.config.BlogSettings;
import diplom.blogengine.exception.UserNotFoundException;
import diplom.blogengine.exception.ValidationException;
import diplom.blogengine.model.PasswordResetToken;
import diplom.blogengine.model.User;
import diplom.blogengine.repository.CachedPostRepository;
import diplom.blogengine.repository.CaptchaCodeRepository;
import diplom.blogengine.repository.PasswordTokenRepository;
import diplom.blogengine.repository.UserRepository;
import diplom.blogengine.security.UserDetailsExt;
import diplom.blogengine.service.util.MailHelper;
import diplom.blogengine.service.util.UriHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService implements IUserService {
    private final static int MIN_PASSWORD_LENGTH = 6;
    private final static Pattern NAME_PATTERN = Pattern.compile("(?i)^[a-zA-Zа-яА-Я]+[a-zA-Zа-яА-Я.\\s]{2,}$");
    private final static int PHOTO_WIDTH = 36;
    private final static int PHOTO_HEIGHT = 36;

    private final BlogSettings blogSettings;
    private final UserRepository userRepository;
    private final CachedPostRepository cachedPostRepository;
    private final PasswordTokenRepository passwordTokenRepository;
    private final CaptchaCodeRepository captchaCodeRepository;
    private final IFileStorageService fileStorageService;
    private final ResultResponseMapper responsesMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final MailHelper mailHelper;

    public UserService(BlogSettings blogSettings,
                       UserRepository userRepository,
                       CachedPostRepository cachedPostRepository,
                       PasswordTokenRepository passwordTokenRepository,
                       CaptchaCodeRepository captchaCodeRepository,
                       IFileStorageService fileStorageService,
                       ResultResponseMapper responsesMapper,
                       PasswordEncoder passwordEncoder,
                       MessageSource messageSource,
                       MailHelper mailHelper) {
        this.blogSettings = blogSettings;
        this.userRepository = userRepository;
        this.cachedPostRepository = cachedPostRepository;
        this.passwordTokenRepository = passwordTokenRepository;
        this.captchaCodeRepository = captchaCodeRepository;
        this.fileStorageService = fileStorageService;
        this.responsesMapper = responsesMapper;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.mailHelper = mailHelper;
    }

    @Transactional
    @Override
    public ResultResponse registerUser(UserRegisterDataRequest userData, Locale locale) {
        log.debug("enter registerUser()");

        Objects.requireNonNull(userData, "userData is null");
        Objects.requireNonNull(locale, "locale is null");

        Map<String, String> errors = validateUserRegisterData(userData, locale);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        userRepository.saveAndFlush(convertDtoToUser(userData));

        return responsesMapper.success();
    }

    @Transactional
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
            throw new ValidationException(errors);
        }

        final int REMOVE_PHOTO_FLAG = 1;
        if (userData.getRemovePhoto() == REMOVE_PHOTO_FLAG) {
            fileStorageService.deleteFile(updateUser.getPhoto());
            updateUser.setPhoto(null);
        } else if (userData.getPhotoFile() != null) {
            String uri = fileStorageService.storeImage(userData.getPhotoFile(), authUserId, PHOTO_WIDTH, PHOTO_HEIGHT);
            updateUser.setPhoto(uri);
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
        userRepository.saveAndFlush(updateUser);

        cachedPostRepository.clearAllCache();

        return responsesMapper.success();
    }

    @Transactional
    @Override
    public ResultResponse resetPassword(UserResetPasswordRequest resetPasswordRequest, Locale locale) {
        log.debug("enter resetPasswordSendToken()");

        Objects.requireNonNull(resetPasswordRequest, "resetPasswordRequest is null");
        Objects.requireNonNull(locale, "locale is null");

        String email = resetPasswordRequest.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordTokenRepository.saveAndFlush(passwordResetToken);

        mailHelper.sendResetPasswordEmail(email, token, locale);
        return responsesMapper.success();
    }

    @Transactional
    @Override
    public ResultResponse saveNewPassword(UserNewPasswordRequest newPasswordRequest, Locale locale) {
        log.debug("enter saveNewPassword()");

        Objects.requireNonNull(newPasswordRequest, "newPasswordRequest is null");
        Objects.requireNonNull(locale, "locale is null");

        String token = newPasswordRequest.getCode();
        String newPassword = newPasswordRequest.getPassword();
        String captcha = newPasswordRequest.getCaptcha();
        String captchaSecret = newPasswordRequest.getCaptchaSecret();

        Map<String, String> errors = new HashMap<>();
        if (!validateCaptcha(captcha, captchaSecret, errors, locale) ) {
            throw new ValidationException(errors);
        }

        PasswordResetToken passwordResetToken = passwordTokenRepository.findByToken(token);
        if (!validatePasswordResetToken(passwordResetToken, errors, locale) || !validatePassword(newPassword, errors, locale)) {
            throw new ValidationException(errors);
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);

        passwordTokenRepository.delete(passwordResetToken);

        return responsesMapper.success();
    }

    private User convertDtoToUser(UserRegisterDataRequest userRegisterDataRequest) {
        log.debug("enter convertDtoToUser()");

        User user = new User();
        user.setName(userRegisterDataRequest.getName());
        user.setEmail(userRegisterDataRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRegisterDataRequest.getPassword()));
        user.setRegTime(LocalDateTime.now());
        boolean isModerator = false;
        user.setModerator(isModerator);
        return user;
    }

    private Map<String, String> validateUserRegisterData(UserRegisterDataRequest userData, Locale locale) {
        log.debug("enter validateUserRegisterData()");

        Map<String, String> errors = new HashMap<>();
        if (!validateCaptcha(userData.getCaptcha(), userData.getCaptchaSecret(), errors, locale)) {
            return errors;
        }
        validateEmail(userData.getEmail(), errors, locale);
        validateName(userData.getName(), errors, locale);
        validatePassword(userData.getPassword(), errors, locale);
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
        log.debug("enter validateName()");

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

    private boolean validateCaptcha(String inputCode, String secretCode, Map<String, String> errors, Locale locale) {
        boolean valid = true;
        if (inputCode != null && secretCode != null && !captchaIsValid(inputCode, secretCode)) {
            errors.put("captcha", messageSource.getMessage("captcha.isincorrect", null, locale));
            valid = false;
        }
        return valid;
    }

    private boolean validatePasswordResetToken(PasswordResetToken passwordResetToken, Map<String, String> errors, Locale locale) {
        boolean valid = true;
        if (passwordResetToken == null || passwordResetToken.getUser() == null) {
            errors.put("code", messageSource.getMessage("token.resetPassword.isIncorrect", null, locale));
            valid = false;
        } else if (passwordResetToken.isExpired()) {
            errors.put("code", messageSource.getMessage("token.resetPassword.isExpired", null, locale));
            valid = false;
        }
        return valid;
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
        log.debug("enter captchaIsValid()");

        LocalDateTime expiryDate = LocalDateTime.now().minusSeconds(blogSettings.getCaptchaDeleteTimeout());
        String code = captchaCodeRepository.findCodeBySecretAndNotExpired(secretCode, expiryDate);
        return code != null && code.equals(inputCode);
    }



}


