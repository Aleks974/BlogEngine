package util;

import diplom.blogengine.api.request.*;
import diplom.blogengine.model.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public class TestDataGenerator {
    private final PasswordEncoder passwordEncoder;

    public TestDataGenerator() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    public Post generatePost(){
        Post post = new Post();
        post.setActive(true);
        post.setModerationStatus(ModerationStatus.ACCEPTED);
        post.setUser(generateUser());
        post.setTime(LocalDateTime.now());
        post.setTitle("Название поста");
        post.setText("текст поста");
        post.setAnnounce("Анонс");
        post.setViewCount(0);

        return post;
    }

    public User generateUser(){
        Role role = new Role();
        role.setName("ROLE_USER");

        User user = new User();
        user.setModerator(false);
        user.setRegTime(LocalDateTime.now());
        user.setName("vasya");
        user.setEmail("test@test.ru");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Collections.singleton(role));

        return user;
    }

    public Tag generateTag(){
        Tag tag = new Tag();
        tag.setName("Тэг1");

        return tag;
    }


    public CaptchaCode generateCaptchaCode() {
        String code = "testcode";
        String secretCode = "testcode123";
        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode(code);
        captchaCode.setSecretCode(secretCode);
        captchaCode.setTime(LocalDateTime.now());
        return captchaCode;
    }

    public String generateUserRegisterDataRequestJson(String inputCode, String secretCode) {
        String json = "{\"password\":\"test\",\"name\":\"test\",\"e_mail\":\"test@test.tu\",\"captcha\":\"" +
                inputCode + "\",\"captcha_secret\":\"" + secretCode + "\"}";
        return json;
    }

    public UserRegisterDataRequest generateUserRegisterDataRequest(String userInputCaptcha, String secretCode) {
        UserRegisterDataRequest request = generateUserRegisterDataRequest();
        request.setEmail("test@test.ru");
        request.setPassword("password");
        request.setName("test");
        request.setCaptcha(userInputCaptcha);
        request.setCaptchaSecret(secretCode);
        return request;
    }

    public UserRegisterDataRequest generateUserRegisterDataRequest() {
        return new UserRegisterDataRequest();
    }

    public PostDataRequest generatePostDataRequest() {
        PostDataRequest postDataRequest = new PostDataRequest();
        postDataRequest.setTitle("Новый пост");
        postDataRequest.setText("Текст для нового поста. Текст. Текст. Текст. Текст. Текст. Текст. Текст. Текст. Текст. Текст. Текст.");
        postDataRequest.setTimestamp(Instant.now().getEpochSecond());
        postDataRequest.setActive(1);
        Set<String> tags = Set.of("Тэг");
        postDataRequest.setTags(tags);
        return postDataRequest;
    }

    public PostDataRequest generatePostDataRequest(Consumer<PostDataRequest> requestConsumer) {
        PostDataRequest postDataRequest = generatePostDataRequest();
        if (requestConsumer != null) {
            requestConsumer.accept(postDataRequest);
        }
        return postDataRequest;
    }

    public PostCommentDataRequest generateCommentDataRequest(long postId) {
        PostCommentDataRequest commentDataRequest = new PostCommentDataRequest();
        commentDataRequest.setPostId(postId);
        commentDataRequest.setText("Новый комментарий");
        return commentDataRequest;
    }


    public PostCommentDataRequest generateCommentDataRequest(long postId, long parentId) {
        PostCommentDataRequest commentDataRequest = generateCommentDataRequest(postId);
        commentDataRequest.setParentId(parentId);
        return commentDataRequest;
    }

    public VoteDataRequest generateVoteDataRequest(long postId) {
        return new VoteDataRequest(postId);
    }

    public GlobalSettingsRequest genGlobalSettingsRequest() {
        return GlobalSettingsRequest.builder()
                .multiUserMode(false)
                .postPreModeration(false)
                .statisticsIsPublic(false)
                .build();
    }

    public Path createTempFile(String extension, int size) throws Exception {
        Path tmpFile = Files.createTempFile("temp", "." + extension);
        byte[] data = new byte[size];
        Files.write(tmpFile, data); // StandardOpenOption.DELETE_ON_CLOSE
        return tmpFile;
    }

    public Path createPhotoFile(String mode, int width, int height) throws Exception {
        Path tmpFile = Files.createTempFile("temp", "." + mode);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        try(OutputStream out = Files.newOutputStream(tmpFile)) {
            ImageIO.write(image, mode, out);
        }
        return tmpFile;
    }

    public UserProfileDataRequest genUserProfileDataRequest(Consumer<UserProfileDataRequest> consumer) {
        UserProfileDataRequest request = new UserProfileDataRequest();
        if (consumer != null) {
            consumer.accept(request);
        }
        return request;
    }

}
