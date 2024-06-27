package cc.ddrpa.motto.html;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

public class BufferedImageReadTests {

    @Test
    void readPNGAndWriteJPEGShouldExportEmptyImageTest() throws IOException {
        // read image as stream
        InputStream im = this.getClass().getClassLoader()
            .getResourceAsStream("rhodes.png");
        BufferedImage bufferedImage = ImageIO.read(im);
        // write to JPEG file
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", bos);
        System.out.println(Base64.getEncoder().encodeToString(bos.toByteArray()));
    }

    @Test
    void readPNGAndWriteJPEGWithAlphaChannelProcessedTest() throws IOException {
        // read image as stream
        InputStream im = this.getClass().getClassLoader()
            .getResourceAsStream("rhodes.png");
        BufferedImage bufferedImage = ImageIO.read(im);
        if (bufferedImage.getColorModel().hasAlpha()) {
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, null);
            bufferedImage = newBufferedImage;
        }
        // write to JPEG file
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", bos);
        System.out.println(Base64.getEncoder().encodeToString(bos.toByteArray()));
    }

    @Test
    void readJPEGAndWritePNGTest() throws IOException {
        // read image as stream
        InputStream im = this.getClass().getClassLoader()
            .getResourceAsStream("another-avatar.jpeg");
        BufferedImage bufferedImage = ImageIO.read(im);
        // write to JPEG file
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", bos);
        System.out.println(Base64.getEncoder().encodeToString(bos.toByteArray()));
    }
}