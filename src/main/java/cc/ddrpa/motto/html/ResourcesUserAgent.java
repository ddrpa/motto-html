package cc.ddrpa.motto.html;

import com.lowagie.text.Image;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.util.ContentTypeDetectingInputStreamWrapper;
import org.xhtmlrenderer.util.XRLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import static org.xhtmlrenderer.util.IOUtil.readBytes;

public class ResourcesUserAgent extends ITextUserAgent {

    private static final String RESOURCES_PREFIX = "resources://";
    private static final int RESOURCES_PREFIX_LENGTH = RESOURCES_PREFIX.length();

    private final int dotsPerPixel;
    private int httpConnectionConnectTimeoutInMills = 3000;
    private int httpConnectionReadTimeoutInMMills = 5000;

    public ResourcesUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel) {
        super(outputDevice, dotsPerPixel);
        this.dotsPerPixel = dotsPerPixel;
    }

    /**
     * 设置远端资源（HTTP URL）连接超时时间，默认为 3 秒
     *
     * @param timeout
     * @param timeUnit
     * @return
     */
    public ResourcesUserAgent setHttpConnectTimeout(int timeout, TimeUnit timeUnit) {
        this.httpConnectionConnectTimeoutInMills = (int) timeUnit.toMillis(timeout);
        return this;
    }

    /**
     * 设置远端资源（HTTP URL）读取超时时间，默认为 5 秒
     *
     * @param timeout
     * @param timeUnit
     * @return
     */
    public ResourcesUserAgent setHttpReadTimeout(int timeout, TimeUnit timeUnit) {
        this.httpConnectionReadTimeoutInMMills = (int) timeUnit.toMillis(timeout);
        return this;
    }

    @Override
    protected URLConnection onHttpConnection(HttpURLConnection origin) throws IOException {
        origin.setConnectTimeout(this.httpConnectionConnectTimeoutInMills);
        origin.setReadTimeout(this.httpConnectionReadTimeoutInMMills);
        return super.onHttpConnection(origin);
    }

    @Override
    public ImageResource getImageResource(String uriStr) {
        if (!uriStr.startsWith(RESOURCES_PREFIX)) {
            return super.getImageResource(uriStr);
        }
        String filePath = uriStr.substring(RESOURCES_PREFIX_LENGTH);
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(filePath);
             ContentTypeDetectingInputStreamWrapper cis = new ContentTypeDetectingInputStreamWrapper(
                     is)) {
            Image image = Image.getInstance(readBytes(cis));
            scaleToOutputResolution(image);
            return new ImageResource(uriStr, new ITextFSImage(image));
        } catch (IOException e) {
            XRLog.exception(
                    "Can't read image file; unexpected problem for URI '" + uriStr + "'", e);
            return new ImageResource(uriStr, null);
        }
    }

    private void scaleToOutputResolution(Image image) {
        float factor = dotsPerPixel;
        if (factor != 1.0f) {
            image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
        }
    }
}