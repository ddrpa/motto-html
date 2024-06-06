package cc.ddrpa.motto.html.embedded;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * 一种用于在文档中嵌入图像的的方案，实际上是将图像转换为 Base64 编码的字符串保存在 {@code <img>} 标签中
 */
public class EmbeddedImage {

    private static float DEFAULT_DOTS_PER_POINT = ITextRenderer.DEFAULT_DOTS_PER_POINT;
    private static int DEFAULT_DOTS_PER_PIXEL = ITextRenderer.DEFAULT_DOTS_PER_PIXEL;
    private static int DEFAULT_DEVICE_PIXEL_RATIO = 1;

    private float dotsPerPoint;
    private int dotsPerPixel;
    private int devicePixelRatio;
    private BufferedImage image;

    private EmbeddedImage(BufferedImage image) {
        this.image = image;
        this.dotsPerPoint = DEFAULT_DOTS_PER_POINT;
        this.dotsPerPixel = DEFAULT_DOTS_PER_PIXEL;
        this.devicePixelRatio = DEFAULT_DEVICE_PIXEL_RATIO;
    }

    private EmbeddedImage(BufferedImage image, float dotsPerPoint, int dotsPerPixel,
        int devicePixelRatio) {
        this.image = image;
        this.dotsPerPoint = dotsPerPoint;
        this.dotsPerPixel = dotsPerPixel;
        this.devicePixelRatio = devicePixelRatio;
    }

    /**
     * 设置默认的点大小
     *
     * @param dotsPerPoint
     */
    public static void setDefaultDotsPerPoint(float dotsPerPoint) {
        DEFAULT_DOTS_PER_POINT = dotsPerPoint;
    }

    /**
     * 设置默认的像素大小
     *
     * @param dotsPerPixel
     */
    public static void setDefaultDotsPerPixel(int dotsPerPixel) {
        DEFAULT_DOTS_PER_PIXEL = dotsPerPixel;
    }

    /**
     * 设置默认的设备像素比
     *
     * @param devicePixelRatio
     */
    public static void setDefaultDevicePixelRatio(int devicePixelRatio) {
        DEFAULT_DEVICE_PIXEL_RATIO = devicePixelRatio;
    }

    /**
     * 从输入流中创建一个新的 {@link EmbeddedImage} 实例
     *
     * @param inputStream 图像输入流
     * @return instance of {@link EmbeddedImage}
     * @throws IOException
     */
    public static EmbeddedImage newInstance(InputStream inputStream) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        return new EmbeddedImage(image);
    }

    /**
     * 从 {@link BufferedImage} 创建一个新的 {@link EmbeddedImage} 实例
     *
     * @param bufferedImage
     * @return instance of {@link EmbeddedImage}
     */
    public static EmbeddedImage newInstance(BufferedImage bufferedImage) {
        return new EmbeddedImage(bufferedImage);
    }

    /**
     * 从字节数组中创建一个新的 {@link EmbeddedImage} 实例
     *
     * @param bytes
     * @return instance of {@link EmbeddedImage}
     * @throws IOException
     */
    public static EmbeddedImage newInstance(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            return EmbeddedImage.newInstance(bis);
        }
    }

    /**
     * 设置 dots 与 point 的换算关系
     *
     * @param dotsPerPoint
     * @return instance of {@link EmbeddedImage}
     */
    public EmbeddedImage setDotsPerPoint(float dotsPerPoint) {
        this.dotsPerPoint = dotsPerPoint;
        return this;
    }

    /**
     * 设置 dots 与 pixel 的换算关系
     *
     * @param dotsPerPixel
     * @return instance of {@link EmbeddedImage}
     */
    public EmbeddedImage setDotsPerPixel(int dotsPerPixel) {
        this.dotsPerPixel = dotsPerPixel;
        return this;
    }

    /**
     * 设置设备像素比
     * <p>
     * 指定图像大小缩放为为某一具体数值时，通过这个数值获得比预定尺寸大得多的图像，再通过 CSS 样式进行控制，
     * 获得更精细的图像
     *
     * @param devicePixelRatio
     * @return instance of {@link EmbeddedImage}
     */
    public EmbeddedImage setDevicePixelRatio(int devicePixelRatio) {
        this.devicePixelRatio = devicePixelRatio;
        return this;
    }

    /**
     * 按照毫米为目标尺寸的单位缩放图像
     *
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public EmbeddedImage scaleWithMillimetre(double targetWidth, double targetHeight) {
        return scaleWithPixel(millimetre2Pixel(targetWidth), millimetre2Pixel(targetHeight));
    }

    /**
     * 按照点为目标尺寸的单位缩放图像
     *
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public EmbeddedImage scaleWithPoint(int targetWidth, int targetHeight) {
        return scaleWithPixel(point2Pixel(targetWidth), point2Pixel(targetHeight));
    }

    /**
     * 将图片转换为 data URL
     *
     * @return Data URL in Base64
     * @throws IOException
     */
    public String toDataURL() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(this.image, "jpeg", bos);
            return String.format("data:image/jpeg;base64,%s", Base64.getEncoder()
                .encodeToString(bos.toByteArray()));
        }
    }

    /**
     * 恰当的方法应该是使用 `toDataURL` 这样的名字，不过我卡在了如何获得
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
        try {
            return this.toDataURL();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取图像的宽度
     *
     * @return width in pixel
     */
    public int getWidth() {
        return this.image.getWidth();
    }

    /**
     * 获取图像的高度
     *
     * @return height in pixel
     */
    public int getHeight() {
        return this.image.getHeight();
    }

    /**
     * 这个方法是调试使用的，可以导出为 JPEG 格式，随时可能发生变更，请勿在生产环境中使用
     *
     * @param outputStream
     * @throws IOException
     */
    @Deprecated
    public void exportAsJPEG(OutputStream outputStream) throws IOException {
        ImageIO.write(this.image, "jpeg", outputStream);
    }

    /**
     * 换算点为像素
     *
     * @param point 点值
     * @return 像素值
     */
    private int point2Pixel(int point) {
        // point -> dots -> pixel
        return (int) Math.ceil(point * this.dotsPerPoint / this.dotsPerPixel);
    }

    /**
     * 换算毫米为像素
     *
     * @param millimetre 毫米值
     * @return 像素值
     */
    private int millimetre2Pixel(double millimetre) {
        // 为防止精度损失故不直接调用 point2Pixel
        return (int) Math.ceil(millimetre * this.dotsPerPoint / this.dotsPerPixel / 0.35);
    }

    /**
     * 按照像素为目标尺寸的单位缩放图像
     *
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private EmbeddedImage scaleWithPixel(int targetWidth, int targetHeight) {
        int targetWidthWithDPR = targetWidth * this.devicePixelRatio;
        int targetHeightWithDPR = targetHeight * this.devicePixelRatio;
        BufferedImage before = this.image;
        int originalWidth = before.getWidth();
        int originalHeight = before.getHeight();
        BufferedImage after = new BufferedImage(targetWidthWithDPR, targetHeightWithDPR,
            before.getType());
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(
            1.0 * targetWidthWithDPR / originalWidth, 1.0 * targetHeightWithDPR / originalHeight);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleTransform,
            AffineTransformOp.TYPE_BICUBIC);
        after = scaleOp.filter(before, after);
        this.image = after;
        return this;
    }
}