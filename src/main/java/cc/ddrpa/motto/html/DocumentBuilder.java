package cc.ddrpa.motto.html;

import com.lowagie.text.pdf.BaseFont;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.CJKFontResolver;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;

public class DocumentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);

    private static final CJKFontResolver fontResolver = new CJKFontResolver();
    private static final VelocityEngine velocityEngine;
    private static final RuntimeServices runtimeServices;

    static {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class",
            ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        runtimeServices = RuntimeSingleton.getRuntimeServices();
    }

    private final float dotsPerPoint;
    private final int dotsPerPixel;
    private final ITextOutputDevice iTextOutputDevice;
    private final ITextUserAgent iTextUserAgent;
    private VelocityContext velocityContext = new VelocityContext();
    private Template template;


    public DocumentBuilder() {
        this.dotsPerPoint = ITextRenderer.DEFAULT_DOTS_PER_POINT;
        this.dotsPerPixel = ITextRenderer.DEFAULT_DOTS_PER_PIXEL;
        this.iTextOutputDevice = new ITextOutputDevice(ITextRenderer.DEFAULT_DOTS_PER_POINT);
        this.iTextUserAgent = new ResourcesUserAgent(iTextOutputDevice,
            ITextRenderer.DEFAULT_DOTS_PER_PIXEL);
    }

    public DocumentBuilder(float dotsPerPoint,
        int dotsPerPixel,
        ITextOutputDevice iTextOutputDevice,
        ITextUserAgent iTextUserAgent) {
        this.dotsPerPoint = dotsPerPoint;
        this.dotsPerPixel = dotsPerPixel;
        this.iTextOutputDevice = iTextOutputDevice;
        this.iTextUserAgent = iTextUserAgent;
    }

    /**
     * 注册字体
     * <p>
     * 不同于模版，字体文件一般较大，不建议打包到 jar 包中，所以这里使用外部文件路径加载字体
     *
     * @param fontFilePath 字体文件路径
     * @throws IOException
     */
    public static void addFont(String fontFilePath) throws IOException {
        fontResolver.addFont(fontFilePath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    }

    /**
     * 获取已注册的字体，可以在作为模版的 HTML 中设置元素的 font-family 属性为列表中的值
     *
     * @return list of font family
     */
    public static List<String> listFontFamily() {
        return fontResolver.getFonts().keySet().stream().toList();
    }

    /**
     * 使用 classpath 中的文件路径加载模版
     * <p>
     * 现版本设置了基于 classpath 的资源加载器，所以文件路径是相对于 classpath 的，
     * 作者还没有想到更好的方法允许调用者修改成别的方法。如果文件存储在 classpath 之外，
     * 请使用 {@link #loadTemplateFromStream(InputStream)} 或
     * {@link #loadTemplateFromPlainText(String)} 方法。
     *
     * @param templateFileClassPath 模版的文件路径
     * @return
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     */
    public DocumentBuilder loadTemplate(String templateFileClassPath)
        throws ResourceNotFoundException, ParseErrorException {
        template = velocityEngine.getTemplate(templateFileClassPath);
        return this;
    }

    /**
     * 从输入流加载模版
     *
     * @param inputStream 包含模版内容的输入流
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public DocumentBuilder loadTemplateFromStream(InputStream inputStream)
        throws IOException, ParseException {
        template = new Template();
        template.setRuntimeServices(runtimeServices);
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            template.setData(runtimeServices.parse(reader, template));
        }
        template.initDocument();
        return this;
    }

    /**
     * 从字符串加载模版
     *
     * @param templateContent 包含模版内容的字符串
     * @return
     * @throws ParseException
     */
    public DocumentBuilder loadTemplateFromPlainText(String templateContent) throws ParseException {
        StringReader reader = new StringReader(templateContent);
        template = new Template();
        template.setRuntimeServices(runtimeServices);
        template.setData(runtimeServices.parse(reader, template));
        template.initDocument();
        return this;
    }

    /**
     * 将数据合并到模版中
     *
     * @param dataMap
     * @return
     */
    public DocumentBuilder merge(Map<String, Object> dataMap) {
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            velocityContext.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * 将数据合并到模版中
     *
     * @param key
     * @param value
     * @return
     */
    public DocumentBuilder merge(String key, Object value) {
        velocityContext.put(key, value);
        return this;
    }

    /**
     * 将文件写到输出流，
     * <B>NOTE:</B> Caller is responsible for cleaning up the OutputStream if
     * something goes wrong.
     *
     * @param outputStream
     * @return
     */
    public DocumentBuilder save(OutputStream outputStream) {
        ITextRenderer renderer = new ITextRenderer(dotsPerPoint, dotsPerPixel, iTextOutputDevice,
            iTextUserAgent, fontResolver);
        StringWriter stringWriter = new StringWriter();
        template.merge(velocityContext, stringWriter);
        renderer.setDocumentFromString(stringWriter.toString());
        renderer.layout();
        renderer.createPDF(outputStream);
        return this;
    }

    /**
     * 重置 DocumentBuilder 的状态，用于创建下一个文档
     *
     * @return
     */
    public DocumentBuilder reset() {
        velocityContext = new VelocityContext();
        return this;
    }
}
