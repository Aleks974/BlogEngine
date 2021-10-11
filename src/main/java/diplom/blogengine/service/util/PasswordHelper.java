package diplom.blogengine.service.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Deprecated
public class PasswordHelper {
    private final String hashAlgorithm;
    private final Base64.Encoder encoder = Base64.getEncoder();
    //private final MessageDigest messageDigest;

    public PasswordHelper(String algorithm) {
        //this.messageDigest = MessageDigest.getInstance(algorithm);
        this.hashAlgorithm = algorithm;
    }

    public byte[] generateHash(String value) {
        byte[] digest;
        //synchronized (lock) { // ToDo test perfomance with synchronized and without
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);
            messageDigest.update(value.getBytes());
            digest = messageDigest.digest();
            messageDigest.reset();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return digest;
    }

    public String generateHashEncode(String value) {
        byte[] digest = generateHash(value);
        return encoder.encodeToString(digest);
    }
}
