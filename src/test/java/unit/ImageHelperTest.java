package unit;


import diplom.blogengine.service.util.ImageHelper;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageHelperTest {

    @Test
    public void givenBufferedImage_whenResize_thenWidthAndHeightCorrect() throws Exception {
        int width = 700;
        int height = 500;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        ImageHelper imageHelper = new ImageHelper();

        int expectedWidth = 30;
        int expectedHeight = 30;

        byte[] resizedImageBytes = imageHelper.resizeImage(image, "jpg", expectedHeight, expectedWidth);
        InputStream in = new ByteArrayInputStream(resizedImageBytes);
        BufferedImage resizedImage = ImageIO.read(in);

        assertEquals(expectedWidth, resizedImage.getWidth());
        assertEquals(expectedHeight, resizedImage.getHeight());
    }
}
