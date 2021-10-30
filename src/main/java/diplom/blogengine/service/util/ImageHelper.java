package diplom.blogengine.service.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class ImageHelper {

    public byte[] resizeImage(BufferedImage sourceImage, String imageFormat, int dW, int dH) throws IOException {
        log.debug("enter resizeImage()");

        BufferedImage resizedImage = new BufferedImage(dW, dH, sourceImage.getType());
        int sW = sourceImage.getWidth();
        int sH = sourceImage.getHeight();
        int kf;
        int offsetX = 0;
        int offsetY = 0;
        if (sW >= sH) {
            kf = sH / dH;
            offsetX = (sW / 2) - (sH / 2);
        } else {
            kf = sW / dW;
            offsetY = (sH / 2) - (sW / 2);
        }
        //log.debug("sW={}, sH={}, kf={}, offsetX={}, offsetY={}", sW, sH, kf, offsetX, offsetY);
        int sX;
        int sY;
        for (int y = 0; y < dW; y++) {
            sY = y * kf + offsetY;
            for (int x = 0; x < dH; x++) {
                sX = x * kf + offsetX;
                //log.debug("sX={}, sY={}", sX, sY);
                int sRGB = sourceImage.getRGB(sX, sY );
                resizedImage.setRGB(x, y, sRGB);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, imageFormat, baos);
        return baos.toByteArray();
    }

}
