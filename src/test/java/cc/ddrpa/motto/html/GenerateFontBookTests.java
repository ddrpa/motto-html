package cc.ddrpa.motto.html;

import com.lowagie.text.DocumentException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 展示中文字体效果
 */
public class GenerateFontBookTests {

    private static final Logger logger = LoggerFactory.getLogger(GenerateFontBookTests.class);

    private static final List<String> FONTS_MAY_HAVE_LICENSE_ISSUE = List.of(
        "/Users/yufan/Library/Fonts/HanaMinA.ttf",
        "/Users/yufan/Downloads/SIMSUN.TTC,0",
        "/Users/yufan/Downloads/SIMSUN.TTC,1"
    );

    private static final List<String> FONTS_THAT_IS_OK_TO_USE = List.of(
        "font-seems-okay/Noto_Sans_SC/static/NotoSansSC-Regular.ttf",
        "font-seems-okay/Noto_Serif_SC/static/NotoSerifSC-Regular.ttf"
    );

    @Test
    void generateFontBookTest() {
        // get all flying saucer builtin CJK fonts
        logger.info("All builtin CJK fonts:");
        DocumentBuilder.listFontFamily().forEach(logger::info);
        // add fonts
        FONTS_MAY_HAVE_LICENSE_ISSUE.forEach(path -> {
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
        DocumentBuilder builder = new DocumentBuilder();
        builder.loadTemplate("src/test/resources/cjk-fonts.html");
        builder.merge(Map.of(
            "font_families",
            DocumentBuilder.listFontFamily()
                .stream()
                .filter(fontFamily -> !fontFamily.endsWith("-V"))
                .filter(fontFamily -> !List.of("Symbol", "Monospaced", "Dialog", "DialogInput",
                    "ZapfDingbats").contains(fontFamily))
                .sorted()
                .toList()));
        try (FileOutputStream fileOutputStream = new FileOutputStream(
            "font-book-sample.pdf")) {
            builder.save(fileOutputStream);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
    }
}