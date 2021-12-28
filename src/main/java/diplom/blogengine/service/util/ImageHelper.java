package diplom.blogengine.service.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class ImageHelper {

    public byte[] resizeImage(byte[] sourceImageBytes, String imageFormat, int dW, int dH) throws IOException {
        log.debug("enter resizeImage()");

        Objects.requireNonNull(sourceImageBytes);
        Objects.requireNonNull(imageFormat);
        if (dW <= 0 || dH <= 0 || imageFormat.isBlank()) {
            throw new IllegalArgumentException("input params are invalid");
        }

        BufferedImage sourceImage;
        try (ByteArrayInputStream imageStream = new ByteArrayInputStream(sourceImageBytes)) {
            sourceImage = ImageIO.read(imageStream);
        }
        Objects.requireNonNull(sourceImage, "sourceImage is null");

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
