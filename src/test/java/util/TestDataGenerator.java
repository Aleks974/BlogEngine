package util;

import diplom.blogengine.model.*;
import diplom.blogengine.service.util.PasswordHelper;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class TestDataGenerator {
    private PasswordHelper passwordHelper;

    public TestDataGenerator() {
        passwordHelper = new PasswordHelper("md5");
    }

    public Post generatePost(){
        Post post = new Post();
        post.setActive(true);
        post.setModerationStatus(ModerationStatus.ACCEPTED);
        post.setUser(generateUser());
        post.setTime(LocalDateTime.now());
        post.setTitle("Название поста");
        post.setText("текст поста");
        post.setViewCount(0);

        return post;
    }

    public User generateUser(){
        User user = new User();
        user.setModerator(false);
        user.setRegTime(LocalDateTime.now());
        user.setName("vasya");
        user.setEmail("test@test.ru");
        user.setPassword(passwordHelper.generateHashEncode("password"));
        return user;
    }

    public Tag generateTag(){
        Tag tag = new Tag();
        tag.setName("Тэг1");

        return tag;
    }


    public CaptchaCode generateCaptchaCode() {
        String code = "testcode";
        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode(code);
        captchaCode.setSecretCode("");
        captchaCode.setTime(LocalDateTime.now());
        return captchaCode;
    }

    public String generateUserDataRequestJson(String inputCode, String secretCode) {
        String json = "{\"password\":\"test\",\"name\":\"test\",\"e_mail\":\"test@test.tu\",\"captcha\":\"" +
                inputCode + "\",\"captcha_secret\":\"" + secretCode + "\"}";
        return json;
    }


}
