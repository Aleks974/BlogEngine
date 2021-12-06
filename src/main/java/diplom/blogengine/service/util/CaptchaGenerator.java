package diplom.blogengine.service.util;

import com.github.cage.Cage;
import com.github.cage.GCage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class CaptchaGenerator {
    private final Base64.Encoder encoder = Base64.getEncoder();
    private final Cage captchaGen = new GCage();
    private final int CAPTCHA_WEIGHT = 100;
    private final int CAPTCHA_HEIGHT = 35;
    private final String IMG_FORMAT = "jpg";
    private final char[] CAPTCHA_LETTERS = "abcdefghijkmnopqrstuvwxyz".toCharArray();

    public byte[] genCaptchaBytes(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage captcha = resizeImg(captchaGen.drawImage(text));
            ImageIO.write(captcha, IMG_FORMAT, baos);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return baos.toByteArray();
    }

    public String genCaptchaEncoded(String text) {
        return encoder.encodeToString(genCaptchaBytes(text));
    }

    public String genRandomString(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char[] codeArr = new char[length];
        for (int i = 0; i < codeArr.length; i++) {
            int ind = random.nextInt(CAPTCHA_LETTERS.length);
            codeArr[i] = CAPTCHA_LETTERS[ind];
        }
        return String.valueOf(codeArr);
        //return captchaGen.getTokenGenerator().next();
    }

    private BufferedImage resizeImg(BufferedImage source) {
        BufferedImage result;
        int sWidth = source.getWidth();
        int sHeight = source.getHeight();
        if (sWidth > CAPTCHA_WEIGHT || sHeight > CAPTCHA_HEIGHT) {
            int dWidth=  sWidth > CAPTCHA_WEIGHT ? CAPTCHA_WEIGHT : sWidth;
            int dHeight=  sHeight > CAPTCHA_HEIGHT ? CAPTCHA_HEIGHT : sHeight;
            int kW = sWidth / dWidth;
            int kH = sHeight / dHeight;
            result = new BufferedImage(dWidth, dHeight, source.getType());
            for (int i = 0; i < dWidth; i++) {
                for (int j = 0; j < dHeight; j++) {
                    int RGB = source.getRGB(i * kW, j * kH);
                    result.setRGB(i, j, RGB);
                }
            }
        } else {
            result = source;
        }
        return result;
    }


}
