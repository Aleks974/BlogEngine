package diplom.blogengine.service.util;

import diplom.blogengine.config.BlogSettings;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class PasswordGenerator {
    private final MessageDigest messageDigest;
    private final Base64.Encoder encoder = Base64.getEncoder();

    public PasswordGenerator(BlogSettings blogSettings) throws NoSuchAlgorithmException {
        this.messageDigest = MessageDigest.getInstance(blogSettings.getHashAlgorithm());

    }

    public byte[] generateHash(String value) {
        messageDigest.update(value.getBytes());
        byte[] digest = messageDigest.digest();
        messageDigest.reset();
        return digest;
    }

    public String generateHashEncode(String value) {
        byte[] digest = generateHash(value);
        return encoder.encodeToString(digest);
    }
}
