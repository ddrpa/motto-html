package cc.ddrpa.motto.html;

import cc.ddrpa.motto.html.embedded.EmbeddedImage;
import com.github.javafaker.Faker;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 展示批量创建 PDF 文档的方法
 */
public class DocumentBuildTests {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuildTests.class);
    private final static Faker faker = new Faker();

    private static final List<String> FONTS_THAT_IS_OK_TO_USE = List.of(
        "font-seems-okay/Noto_Sans_SC/static/NotoSansSC-Regular.ttf",
        "font-seems-okay/Noto_Serif_SC/static/NotoSerifSC-Regular.ttf");

    static {
        System.setProperty("xr.util-logging.loggingEnabled", "true");
    }

    @Test
    void trainingRecordGenerateTest() throws IOException {
        // add fonts to fontResolver
        FONTS_THAT_IS_OK_TO_USE.forEach(path -> {
            try {
                DocumentBuilder.addFont(path);
            } catch (IOException e) {
                logger.atError()
                    .addArgument(path)
                    .setMessage("Failed to add font: {}")
                    .setCause(e)
                    .log();
            }
        });

        DocumentBuilder builder = new DocumentBuilder()
            .loadTemplate("src/test/resources/record-template.html");

        IntStream.range(1, 4).forEach(index -> {
            builder.merge(
                Map.of("name", faker.name().fullName(), "idCard", faker.idNumber().invalidSvSeSsn(),
                    "category", "吃瓜群众", "position", faker.job().position(), "education",
                    "有文化的人", "major", faker.educator().course(), "company",
                    faker.company().name() + "有限公司", "companyCode", "321123234679826ft34267XX",
                    "all_course_records",
                    List.of(new CourseRecord(1, "课程名称1", "培训策略1", 11, 100),
                        new CourseRecord(2, "课程名称2", "培训策略2", 22, 98),
                        new CourseRecord(3, "课程名称3", "培训策略3", 33, 79))));
            if (index == 1) {
                // load image with file path
                EmbeddedImage image;
                try (FileInputStream fis = new FileInputStream("src/test/resources/avatar.jpeg")) {
                    // 由于图片在生成文件中的视觉尺寸已经被 CSS 样式确定，将其缩放为 8x8 的资源会使产物可见地模糊
                    image = EmbeddedImage.newInstance(fis);
                    builder.merge("avatar", image);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (index == 2) {
                // load image with resources prefix
                // it will load image from src/test/resources/avatar.jpeg
                // resources:// 不是我们的目的，只是用来演示如何通过自定义前缀从不同的途径加载资源
                builder.merge("avatar", "resources://avatar.jpeg");
            } else {
                // load form url
                builder.merge("avatar", "https://avatars.githubusercontent.com/u/9947768?v=4");
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(
                "target/record-sample-" + index + ".pdf")) {
                builder.save(fileOutputStream).reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}