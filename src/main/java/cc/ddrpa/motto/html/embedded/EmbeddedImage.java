package cc.ddrpa.motto.html.embedded;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 一种用于在文档中嵌入图像的的方案，实际上是将图像转换为 Base64 编码的字符串保存在 {@code <img>} 标签中
 */
public class EmbeddedImage {

    private byte[] byteArray;
    private String fileType;

    private EmbeddedImage(byte[] byteArray, String fileType) {
        this.byteArray = byteArray;
        this.fileType = fileType;
    }

    /**
     * 从输入流中创建一个新的 {@link EmbeddedImage} 实例
     *
     * @param inputStream 图像输入流
     * @param fileType    图像文件类型，从 {@link EmbeddedResource} 中选取
     * @return
     * @throws IOException
     */
    public static EmbeddedImage newInstance(InputStream inputStream, String fileType)
        throws IOException {
        if (!List.of(EmbeddedResource.JPEG, EmbeddedResource.PNG).contains(fileType)) {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
        return new EmbeddedImage(inputStream.readAllBytes(), fileType);
    }

    /**
     * 从 {@link BufferedImage} 创建一个新的 {@link EmbeddedImage} 实例
     *
     * @param bufferedImage
     * @return
     * @throws IOException
     */
    public static EmbeddedImage newInstance(BufferedImage bufferedImage) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpeg", bos);
            return new EmbeddedImage(bos.toByteArray(), EmbeddedResource.JPEG);
        }
    }

    /**
     * 从输入流中创建一个新的 {@link EmbeddedImage} 实例，并指定目标宽度和高度
     *
     * @param inputStream  图像输入流
     * @param fileType     图像文件类型，从 {@link EmbeddedResource} 中选取
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return
     * @throws IOException
     */
    public static EmbeddedImage newInstance(
        InputStream inputStream,
        String fileType,
        int targetWidth,
        int targetHeight) throws IOException {
        if (!List.of(EmbeddedResource.JPEG, EmbeddedResource.PNG).contains(fileType)) {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
        BufferedImage tempImage = ImageIO.read(inputStream);
        return EmbeddedImage.newInstance(scale(tempImage, targetWidth, targetHeight));
    }

    /**
     * 从 {@link BufferedImage} 创建一个新的 {@link EmbeddedImage} 实例，并指定目标宽度和高度
     *
     * @param bufferedImage
     * @param targetWidth
     * @param targetHeight
     * @return
     * @throws IOException
     */
    public static EmbeddedImage newInstance(BufferedImage bufferedImage, int targetWidth,
        int targetHeight) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(scale(bufferedImage, targetWidth, targetHeight), "jpeg", bos);
            return new EmbeddedImage(bos.toByteArray(), EmbeddedResource.JPEG);
        }
    }


    private static BufferedImage scale(BufferedImage before, int targetWidth, int targetHeight) {
        int originalWidth = before.getWidth();
        int originalHeight = before.getHeight();
        BufferedImage after = new BufferedImage(targetWidth, targetHeight, before.getType());
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(
            1.0 * targetWidth / originalWidth, 1.0 * targetHeight / originalHeight);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleTransform,
            AffineTransformOp.TYPE_BILINEAR);
        return scaleOp.filter(before, after);
    }

    /**
     * 将图片转换为 data URL
     * <p>
     * 更恰当的方法应该是使用 `toDataURL` 这样的名字，不过我卡在了如何获得
     * {@link org.apache.velocity.util.introspection.TypeConversionHandler} 实例来
     * {@code addConverter}
     * 的方法，照着 <a href="https://github.com/apache/velocity-engine/blob/86cfcf41105f8a25db11ca6483e33c20fc0804d9/velocity-engine-core/src/test/java/org/apache/velocity/test/util/introspection/ConversionHandlerTestCase.java#L127">velocity-engine-core/src/test/java/org/apache/velocity/test/util/introspection/ConversionHandlerTestCase.java</a> 试了下 <pre><code>
     * RuntimeInstance ri = new RuntimeInstance();
     * ri.addProperty(Velocity.UBERSPECT_CLASSNAME,"org.apache.velocity.util.introspection.UberspectImpl");
     * ri.init();
     * typeConversionHandler = ((UberspectImpl) ri.getUberspect()).getConversionHandler();
     * typeConversionHandler.addConverter(String.class, EmbeddedImage.class, (object) -> ((EmbeddedImage) object).toDataURL());
     * </code></pre> 似乎没有成功
     *
     * @return Data URL in Base64
     */
    public String toString() {
        return String.format("data:image/%s;base64,%s", fileType, Base64.getEncoder()
            .encodeToString(byteArray));
    }
}