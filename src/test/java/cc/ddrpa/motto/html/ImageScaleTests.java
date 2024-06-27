package cc.ddrpa.motto.html;

import cc.ddrpa.motto.html.embedded.EmbeddedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;

public class ImageScaleTests {

    private static final Logger logger = LoggerFactory.getLogger(ImageScaleTests.class);
    private final float dotsPerPoint = ITextRenderer.DEFAULT_DOTS_PER_POINT;
    private final int dotsPerPixel =  ITextRenderer.DEFAULT_DOTS_PER_PIXEL;

    private void logImageSize(EmbeddedImage image) {
        int pixelWidth = image.getWidth();
        int pixelHeight = image.getHeight();
        logger.info("pixel size: width {}, height {}", pixelWidth, pixelHeight);
    }

    @Test
    void scaleTest() throws IOException {
        ITextOutputDevice iTextOutputDevice = new ITextOutputDevice(dotsPerPoint);
        DocumentBuilder builder = new DocumentBuilder(dotsPerPoint, dotsPerPixel, iTextOutputDevice,
            new ResourcesUserAgent(iTextOutputDevice, dotsPerPixel));
        builder.loadTemplate("scale.html");
        try (FileInputStream fis = new FileInputStream("src/test/resources/large-photo.jpeg")) {
            EmbeddedImage original = EmbeddedImage.newInstance(fis);
            logImageSize(original);
            builder.merge("ORIGINAL", original);
            original.exportAsJPEG(new FileOutputStream("target/original-image.jpeg"));
        }

        try (FileInputStream fis = new FileInputStream("src/test/resources/large-photo.jpeg")) {
            EmbeddedImage compressed = EmbeddedImage.newInstance(fis)
                .setDotsPerPoint(dotsPerPoint)
                .setDotsPerPixel(dotsPerPixel)
                .scaleWithPoint(228, 128);
            logImageSize(compressed);
            builder.merge("COMPRESSED", compressed);
            compressed.exportAsJPEG(new FileOutputStream("target/compressed-image.jpeg"));
        }

        try (FileInputStream fis = new FileInputStream("src/test/resources/large-photo.jpeg")) {
            EmbeddedImage dpr2 = EmbeddedImage.newInstance(fis)
                .setDotsPerPoint(dotsPerPoint)
                .setDotsPerPixel(dotsPerPixel)
                .setDevicePixelRatio(2)
                .scaleWithPoint(228, 128);
            logImageSize(dpr2);
            builder.merge("COMPRESSED_DPR_2", dpr2);
            dpr2.exportAsJPEG(new FileOutputStream("target/compressed-image-dpr2.jpeg"));
        }

        try (FileInputStream fis = new FileInputStream("src/test/resources/large-photo.jpeg")) {
            EmbeddedImage dpr4 = EmbeddedImage.newInstance(fis)
                .setDotsPerPoint(dotsPerPoint)
                .setDotsPerPixel(dotsPerPixel)
                .setDevicePixelRatio(4)
                .scaleWithPoint(228, 128);
            logImageSize(dpr4);
            builder.merge("COMPRESSED_DPR_4", dpr4);
            dpr4.exportAsJPEG(new FileOutputStream("target/compressed-image-dpr4.jpeg"));
        }

        try (FileInputStream fis = new FileInputStream("src/test/resources/large-photo.jpeg")) {
            EmbeddedImage dpr8 = EmbeddedImage.newInstance(fis)
                .setDotsPerPoint(dotsPerPoint)
                .setDotsPerPixel(dotsPerPixel)
                .setDevicePixelRatio(8)
                .scaleWithPoint(228, 128);
            logImageSize(dpr8);
            builder.merge("COMPRESSED_DPR_8", dpr8);
            dpr8.exportAsJPEG(new FileOutputStream("target/compressed-image-dpr8.jpeg"));
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(
            "target/scale.pdf")) {
            builder.save(fileOutputStream);
        }
    }
}