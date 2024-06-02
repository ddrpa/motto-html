package cc.ddrpa.motto.html;

import static org.xhtmlrenderer.util.IOUtil.readBytes;

import com.lowagie.text.Image;
import java.io.IOException;
import java.io.InputStream;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.util.ContentTypeDetectingInputStreamWrapper;
import org.xhtmlrenderer.util.XRLog;

public class ResourcesUserAgent extends ITextUserAgent {

    private static final String RESOURCES_PREFIX = "resources://";
    private static final int RESOURCES_PREFIX_LENGTH = RESOURCES_PREFIX.length();

    private final int dotsPerPixel;

    public ResourcesUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel) {
        super(outputDevice, dotsPerPixel);
        this.dotsPerPixel = dotsPerPixel;
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